using System.Collections;
using System.Collections.Generic;
using Unity.Netcode;
using UnityEngine;
using UnityEngine.SceneManagement;

public class PlayerMove : NetworkBehaviour
{
    [Header("이동 설정")]
    public float moveSpeed = 5f;

    private Rigidbody2D rb;
    private Collider2D playerCollider;
    private Vector2 moveInput;
    private Animator animator;
    public static PlayerMove Instance;

    private bool isFrozen = false;
    private float clientPingTimer = 0f;
    private string lastAnimation = "Player_Down"; // 애니메이션 직전 방향 기억 상자

    public override void OnNetworkSpawn()
    {
        rb = GetComponent<Rigidbody2D>();
        playerCollider = GetComponent<Collider2D>();
        animator = GetComponent<Animator>();

        if (rb != null)
        {
            rb.gravityScale = 0f;
            rb.constraints = RigidbodyConstraints2D.FreezeRotation;
        }

        isFrozen = false;

        if (IsOwner)
        {
            Instance = this;
            DontDestroyOnLoad(gameObject);
            SceneManager.sceneLoaded += HandleNewSceneSetup;

            ExecuteTeleportProcess();
        }
        else
        {
            // 호스트/서버가 아닌 '순수 클라이언트' 화면에서만 타인의 물리를 고정시킵니다.
            if (rb != null)
            {
                rb.bodyType = RigidbodyType2D.Kinematic;
                rb.velocity = Vector2.zero;
            }
        }
    }

    private void Start()
    {
        // [싱글플레이 대응] 넷코드 서버 없이 혼자 시작했을 때를 위한 안전장치
        if (NetworkManager.Singleton == null || !NetworkManager.Singleton.IsListening)
        {
            rb = GetComponent<Rigidbody2D>();
            playerCollider = GetComponent<Collider2D>();
            animator = GetComponent<Animator>();

            if (rb != null)
            {
                rb.gravityScale = 0f;
                rb.constraints = RigidbodyConstraints2D.FreezeRotation;
            }
            Instance = this;
            DontDestroyOnLoad(gameObject);
        }
    }

    public override void OnNetworkDespawn()
    {
        if (IsOwner)
        {
            SceneManager.sceneLoaded -= HandleNewSceneSetup;
        }
    }

    public void FreezeMovement()
    {
        if (NetworkManager.Singleton != null && NetworkManager.Singleton.IsListening && !IsOwner) return;

        isFrozen = true;
        moveInput = Vector2.zero;

        UpdateAnimation(Vector2.zero);

        if (NetworkManager.Singleton != null && NetworkManager.Singleton.IsListening)
        {
            MoveServerRpc(Vector2.zero);
        }
        else
        {
            if (rb != null) rb.velocity = Vector2.zero;
        }
    }

    private void HandleNewSceneSetup(Scene scene, LoadSceneMode mode)
    {
        if (NetworkManager.Singleton != null && NetworkManager.Singleton.IsListening && !IsOwner) return;

        this.enabled = true;

        if (rb != null)
        {
            rb.velocity = Vector2.zero;
        }

        isFrozen = true;
        if (playerCollider != null) playerCollider.enabled = false;

        StartCoroutine(TeleportDelayRoutine());
    }

    private IEnumerator TeleportDelayRoutine()
    {
        yield return new WaitForSecondsRealtime(0.05f);

        ExecuteTeleportProcess();

        yield return new WaitForEndOfFrame();

        isFrozen = false;
        if (playerCollider != null) playerCollider.enabled = true;

        TMPro.TextMeshProUGUI bubbleText = GetComponentInChildren<TMPro.TextMeshProUGUI>();
        if (bubbleText != null) bubbleText.text = "";
    }

    private void ExecuteTeleportProcess()
    {
        if (RoomManager.Instance == null || string.IsNullOrEmpty(RoomManager.Instance.targetDoorName)) return;

        GameObject targetDoor = GameObject.Find(RoomManager.Instance.targetDoorName);

        if (targetDoor != null)
        {
            Vector3 spawnPosition = targetDoor.transform.position + (Vector3.down * 1.5f);
            transform.position = spawnPosition;

            if (NetworkManager.Singleton != null && NetworkManager.Singleton.IsListening)
            {
                TeleportServerRpc(spawnPosition);
            }

            RoomManager.Instance.targetDoorName = "";
        }
    }

    [ServerRpc]
    private void TeleportServerRpc(Vector3 newPosition)
    {
        transform.position = newPosition;
    }

    void Update()
    {
        if (NetworkManager.Singleton != null && NetworkManager.Singleton.IsListening && !IsOwner) return;

        clientPingTimer += Time.deltaTime;
        if (clientPingTimer >= 20f)
        {
            clientPingTimer = 0f;
            KeepAliveServerRpc();
        }

        if (isFrozen || (ChatManager.Instance != null && ChatManager.Instance.IsTyping()))
        {
            if (moveInput != Vector2.zero)
            {
                moveInput = Vector2.zero;
                UpdateAnimation(Vector2.zero);

                if (NetworkManager.Singleton != null && NetworkManager.Singleton.IsListening)
                {
                    MoveServerRpc(Vector2.zero);
                }
                else
                {
                    if (rb != null) rb.velocity = Vector2.zero;
                }
            }
            return;
        }

        Vector2 prevInput = moveInput;
        moveInput.x = Input.GetAxisRaw("Horizontal");
        moveInput.y = Input.GetAxisRaw("Vertical");

        if (moveInput.sqrMagnitude > 1)
        {
            moveInput.Normalize();
        }

        UpdateAnimation(moveInput);

        if (moveInput != prevInput)
        {
            if (NetworkManager.Singleton != null && NetworkManager.Singleton.IsListening)
            {
                MoveServerRpc(moveInput);
            }
        }
    }

    void FixedUpdate()
    {
        if (rb != null && !isFrozen && (IsOwner || NetworkManager.Singleton == null || !NetworkManager.Singleton.IsListening))
        {
            Vector2 targetVelocity = moveInput * moveSpeed;
            rb.velocity = targetVelocity;
        }
    }

    /// <summary>
    /// 애니메이터 다이렉트 상하좌우 및 멈춤 제어 제어 함수
    /// </summary>
    private void UpdateAnimation(Vector2 input)
    {
        if (animator == null) return;

        if (input != Vector2.zero)
        {
            animator.speed = 1f;

            if (input.x > 0) lastAnimation = "Player_Right";
            else if (input.x < 0) lastAnimation = "Player_Left";
            else if (input.y > 0) lastAnimation = "Player_Up";
            else if (input.y < 0) lastAnimation = "Player_Down";

            animator.Play(lastAnimation);
        }
        else
        {
            animator.Play(lastAnimation, 0, 0f);
            animator.speed = 0f;
        }
    }

    [ServerRpc]
    private void MoveServerRpc(Vector2 inputDirection)
    {
        if (rb != null)
        {
            rb.velocity = inputDirection * moveSpeed;
        }

        UpdateAnimationClientRpc(inputDirection);
    }

    [ClientRpc]
    private void UpdateAnimationClientRpc(Vector2 inputDirection)
    {
        if (!IsOwner)
        {
            UpdateAnimation(inputDirection);
        }
    }

    [ServerRpc]
    private void KeepAliveServerRpc()
    {
        Debug.Log($"[서버 수신] 클라이언트 {OwnerClientId}번의 신호 송신 중...");
    }
}