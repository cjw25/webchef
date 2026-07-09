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
    public static PlayerMove Instance;

    private bool isFrozen = false;

    private float clientPingTimer = 0f;
    public override void OnNetworkSpawn()
    {
        rb = GetComponent<Rigidbody2D>();
        playerCollider = GetComponent<Collider2D>();

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
            // [중요 변경점]
            // 서버 권한 이동에서는 서버가 타인 컴퓨터의 물리(Rigidbody)도 제어해야 하므로,
            // 호스트/독립 서버가 아닌 '순수 클라이언트' 화면에서만 타인의 물리를 꺼줍니다.
            if (rb != null)
            {
                rb.bodyType = RigidbodyType2D.Kinematic;
                rb.velocity = Vector2.zero;
            }
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
        if (!IsOwner) return;

        isFrozen = true;
        moveInput = Vector2.zero;

        // 내 로컬에서 멈춘 뒤, 서버에게도 즉시 멈추라고 입력값을 초기화하여 전달합니다.
        MoveServerRpc(Vector2.zero);
    }

    private void HandleNewSceneSetup(Scene scene, LoadSceneMode mode)
    {
        if (!IsOwner) return;

        this.enabled = true;

        if (rb != null)
        {
            rb.velocity = Vector2.zero;
        }

        // 텔레포트 중에는 잠시 서버 연산을 멈추기 위해 이동 불가 처리
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

            // 내 화면의 좌표를 옮기면서, 서버에게도 내 위치를 강제로 동기화하라고 RPC를 보냅니다.
            transform.position = spawnPosition;
            TeleportServerRpc(spawnPosition);

            RoomManager.Instance.targetDoorName = "";
        }
    }

    // [서버 전용 RPC] 텔레포트 시 서버에 있는 내 캐릭터 오브젝트 위치도 강제 강제 정렬합니다.
    [ServerRpc]
    private void TeleportServerRpc(Vector3 newPosition)
    {
        transform.position = newPosition;
    }

    void Update()
    {
        if (!IsOwner) return;

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
                MoveServerRpc(Vector2.zero); // 멈췄다는 사실을 서버에 알림
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

        // 입력값에 변화가 생겼을 때만 서버에 네트워크 패킷(RPC)을 보냅니다. (매 프레임 전송 방지/최적화)
        if (moveInput != prevInput)
        {
            MoveServerRpc(moveInput);
        }
    }

    void FixedUpdate()
    {
        // 서버 권한 이동에서는 FixedUpdate에서 클라이언트가 직접 이동하지 않습니다.
        // 클라이언트의 화면은 오직 NetworkTransform이 전송해 주는 서버의 위치를 받아와서 그려집니다.
    }

    // ★ [1번 방식의 핵심] 클라이언트의 입력값을 받아 "독립 서버"에서 물리 이동을 실행하는 함수
    [ServerRpc]
    private void MoveServerRpc(Vector2 inputDirection)
    {
        // 이 안의 코드는 오직 독립 서버 컴퓨터 내부에서만 실행됩니다.
        if (rb != null)
        {
            rb.velocity = inputDirection * moveSpeed;
        }
    }

    [ServerRpc]
    private void KeepAliveServerRpc()
    {
        Debug.Log($"[서버 수신] 클라이언트 {OwnerClientId}번의 신호 송신 중...");
    }
}