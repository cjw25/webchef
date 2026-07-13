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
<<<<<<< HEAD
    private Animator animator; // 애니메이터 컴포넌트 변수
=======
    public static PlayerMove Instance;
>>>>>>> edc6820196073a4098899b2e5bc0e7ec88c42f62

    private bool isFrozen = false;

    private float clientPingTimer = 0f;
    public override void OnNetworkSpawn()
    {
        rb = GetComponent<Rigidbody2D>();
        playerCollider = GetComponent<Collider2D>();
        animator = GetComponent<Animator>(); // 내 오브젝트의 Animator 가져오기

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
            // 서버 권한 이동에서는 서버가 타인 컴퓨터의 물리(Rigidbody)도 제어해야 하므로,
            // 호스트/독립 서버가 아닌 '순수 클라이언트' 화면에서만 타인의 물리를 꺼줍니다.
            if (rb != null)
            {
                rb.bodyType = RigidbodyType2D.Kinematic;
                rb.velocity = Vector2.zero;
            }
        }
    }

    // [싱글플레이 대응용] 넷코드 서버 없이 혼자 시작(싱글 플레이)했을 때를 위한 안전장치
    private void Start()
    {
        // 만약 멀티플레이 룸이 아닌 상태(NetworkManager가 작동 안 함)라면 일반 로컬 세팅을 해줍니다.
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
        // 멀티플레이 상태일 때만 IsOwner 체크를 하고, 싱글플레이면 통과시킵니다.
        if (NetworkManager.Singleton != null && NetworkManager.Singleton.IsListening && !IsOwner) return;

        isFrozen = true;
        moveInput = Vector2.zero;

        UpdateAnimation(Vector2.zero); // 애니메이션 멈춤(정면 보기)

        if (NetworkManager.Singleton != null && NetworkManager.Singleton.IsListening)
        {
            MoveServerRpc(Vector2.zero); // 서버에게도 즉시 멈추라고 전달
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
        // 멀티플레이 중인데 내가 조종하는 캐릭터가 아니라면 조작 무시
        if (NetworkManager.Singleton != null && NetworkManager.Singleton.IsListening && !IsOwner) return;

<<<<<<< HEAD
        // 대화 중이거나 채팅 중일 때 멈춤 처리
=======
        clientPingTimer += Time.deltaTime;
        if (clientPingTimer >= 20f)
        {
            clientPingTimer = 0f;
            KeepAliveServerRpc();
        }
>>>>>>> edc6820196073a4098899b2e5bc0e7ec88c42f62
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

        // [내 화면 애니메이션 연동] 키보드를 누르는 순간 즉시 내 화면 캐릭터를 다이렉트로 재생시킵니다.
        UpdateAnimation(moveInput);

        // 입력값에 변화가 생겼을 때 처리
        if (moveInput != prevInput)
        {
            if (NetworkManager.Singleton != null && NetworkManager.Singleton.IsListening)
            {
                // 멀티플레이 상태면 서버에 패킷 전송
                MoveServerRpc(moveInput);
            }
        }
    }
    void FixedUpdate()
    {
        // 1. 내 캐릭터이거나, 싱글플레이인 경우에만 물리 이동 처리
        if (rb != null && !isFrozen && (IsOwner || NetworkManager.Singleton == null || !NetworkManager.Singleton.IsListening))
        {
            // [핵심 변경] MovePosition 대신 velocity를 사용하여 물리 엔진이 
            // 충돌(Collision)을 직접 계산하게 합니다. 
            // 이렇게 하면 벽(Collider)을 만났을 때 물리 엔진이 자동으로 속도를 0으로 막습니다.

            Vector2 targetVelocity = moveInput * moveSpeed;

            // 물리 엔진이 충돌을 정상적으로 계산할 수 있도록 속도를 설정합니다.
            rb.velocity = targetVelocity;

            // 만약 벽을 뚫는 증상이 계속된다면, 아래 주석을 해제하여 
            // 물리적 위치 보정을 추가할 수 있습니다. (네트워크 위치와 충돌 시)
            // rb.MovePosition(rb.position + targetVelocity * Time.fixedDeltaTime);
        }
    }

    // ★ [애니메이터 다이렉트 제어 함수] 복잡한 조건 없이 코드가 직접 상태를 플레이합니다.
    // 마지막으로 바라보던 방향의 애니메이션 이름을 기억하는 상자 (함수 밖에 선언해도 되지만 안전하게 static이나 변수로 두거나, 기존 재생중인걸 활용합니다)
    private string lastAnimation = "Player_Down";

    // ★ [상하좌우 멈춤 구현] 가만히 있을 때 직전 방향을 바라보며 멈추는 함수
    private void UpdateAnimation(Vector2 input)
    {
        if (animator == null) return;

        // 1. 키보드 방향키 입력이 있을 때 (움직이는 중)
        if (input != Vector2.zero)
        {
            // 애니메이션 재생 속도를 정상(1)으로 작동시킵니다.
            animator.speed = 1f;

            // 대각선 이동 시 하나만 선택되도록 우선순위를 둡니다.
            if (input.x > 0) lastAnimation = "Player_Right";
            else if (input.x < 0) lastAnimation = "Player_Left";
            else if (input.y > 0) lastAnimation = "Player_Up";
            else if (input.y < 0) lastAnimation = "Player_Down";

            // 결정된 방향의 애니메이션을 재생합니다.
            animator.Play(lastAnimation);
        }
        // 2. 키보드에서 손을 떼었을 때 (가만히 멈춤)
        else
        {
            // 마지막으로 걷던 방향 애니메이션의 '첫 번째 프레임(서 있는 모습)' 상태로 강제 고정합니다.
            // Play("애니메이션이름", 레이어번호, 재생위치 0f~1f)
            animator.Play(lastAnimation, 0, 0f);

            // 재생 속도를 0으로 만들어 발이 움직이지 않게 완전히 얼려버립니다!
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

        // 서버 권한 이동 시 다른 유저들 화면에도 내 애니메이션 상태가 바로 켜지도록 명령합니다.
        UpdateAnimationClientRpc(inputDirection);
    }

    [ClientRpc]
    private void UpdateAnimationClientRpc(Vector2 inputDirection)
    {
        // 내 캐릭터가 아닌 다른 사람 화면의 캐릭터들의 팔다리를 움직여 줍니다.
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