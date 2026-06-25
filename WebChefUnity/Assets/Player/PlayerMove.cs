using System.Collections;
using System.Collections.Generic;
using Unity.Netcode; // ★ 멀티플레이를 위해 반드시 추가해야 합니다!
using UnityEngine;
using UnityEngine.SceneManagement;

// ★ MonoBehaviour 대신 NetworkBehaviour를 상속받아야 네트워크 연산이 가능합니다.
public class PlayerMove : NetworkBehaviour
{
    [Header("이동 설정")]
    public float moveSpeed = 5f;

    private Rigidbody2D rb;
    private Collider2D playerCollider;
    private Vector2 moveInput;

    // ★ 문에 비비면서 위로 파고드는 것을 막는 상태 플래그 (멀티에서도 그대로 유지)
    private bool isFrozen = false;

    // 싱글톤(Instance)은 내 화면의 '나 자신(LocalPlayer)'을 쉽게 찾기 위한 용도로만 재정의합니다.
    public static PlayerMove Instance;

    private void Awake()
    {
        // 멀티플레이어는 오브젝트가 여러 개 생성되므로 Awake에서 DontDestroyOnLoad를 성급히 걸지 않고,
        // OnNetworkSpawn(네트워크에 본인이 등록될 때)에서 내 캐릭터인 경우에만 초기화하도록 변경합니다.
    }

    // ★ [멀티플레이 핵심] 서버/클라이언트 네트워크에 이 캐릭터가 탄생할 때 실행되는 함수
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

        // 내 컴퓨터가 조종하는 '내 캐릭터'일 때만 작동하는 규칙
        if (IsOwner)
        {
            Instance = this;
            DontDestroyOnLoad(gameObject);
            SceneManager.sceneLoaded += HandleNewSceneSetup;

            ExecuteTeleportProcess();
        }
        else
        {
            // ★ 타인의 캐릭터라면 내 화면에서 물리 충돌 연산이 엉키지 않도록 리지드바디 물리 시뮬레이션을 잠시 꺼둡니다.
            // 타인 캐릭터의 위치는 Netcode(NetworkTransform 등)가 서버를 통해 자동으로 움직여줍니다.
            if (rb != null) rb.simulated = false;
        }
    }

    public override void OnNetworkDespawn()
    {
        if (IsOwner)
        {
            SceneManager.sceneLoaded -= HandleNewSceneSetup;
        }
    }

    // ★ [문 스크립트 전용 연동 함수] 문에 닿는 그 한 프레임에 손발을 완전히 묶어버립니다. (내 캐릭터만)
    public void FreezeMovement()
    {
        if (!IsOwner) return;

        isFrozen = true;
        moveInput = Vector2.zero;
        if (rb != null)
        {
            rb.velocity = Vector2.zero;
            rb.simulated = false; // 리지드바디 물리를 정지시켜 물리적 이동을 차단합니다.
        }
    }

    private void HandleNewSceneSetup(Scene scene, LoadSceneMode mode)
    {
        if (!IsOwner) return;

        // ★ 문에서 꺼버린 나 자신(스크립트)을 새 방에 왔으니 다시 깨웁니다!
        this.enabled = true;

        // 새 방에 오자마자 물리와 콜라이더를 꺼서 파고듦 및 잔상 충돌을 차단합니다.
        if (rb != null)
        {
            rb.simulated = false;
            rb.velocity = Vector2.zero;
        }
        if (playerCollider != null)
        {
            playerCollider.enabled = false;
        }

        StartCoroutine(TeleportDelayRoutine());
    }

    private IEnumerator TeleportDelayRoutine()
    {
        // 씬 내부 오브젝트가 완벽히 정돈될 때까지 0.05초 대기
        yield return new WaitForSecondsRealtime(0.05f);

        // 위치 이동 실행
        ExecuteTeleportProcess();

        // 유니티가 변동된 좌표를 완벽히 인지하도록 1프레임 더 대기
        yield return new WaitForEndOfFrame();

        // ★ 순간이동이 완벽히 끝났으므로 이동 제약을 풀어줍니다.
        isFrozen = false;

        // 이제 문 영역에서 완전히 벗어났으므로 안전하게 복구
        if (rb != null) rb.simulated = true;
        if (playerCollider != null) playerCollider.enabled = true;

        // 말풍선 초기화
        TMPro.TextMeshProUGUI bubbleText = GetComponentInChildren<TMPro.TextMeshProUGUI>();
        if (bubbleText != null) bubbleText.text = "";
    }

    private void ExecuteTeleportProcess()
    {
        if (RoomManager.Instance == null || string.IsNullOrEmpty(RoomManager.Instance.targetDoorName)) return;

        GameObject targetDoor = GameObject.Find(RoomManager.Instance.targetDoorName);

        if (targetDoor != null)
        {
            // 문 아래로 확실하게 떨어진 지점에 안착시켜 끼임을 차단합니다.
            Vector3 spawnPosition = targetDoor.transform.position + (Vector3.down * 1.5f);
            transform.position = spawnPosition;

            // 데이터 사용 완료 후 리셋
            RoomManager.Instance.targetDoorName = "";
        }
    }

    void Update()
    {
        // ★ [멀티 핵심] 내가 소유한 내 캐릭터가 아니라면 키보드 입력을 완전히 무시합니다.
        if (!IsOwner) return;

        // ★ 마비 상태이거나 채팅 창 입력 중이면 모든 키보드 입력을 차단하고 정지시킵니다.
        if (isFrozen || (ChatManager.Instance != null && ChatManager.Instance.IsTyping()))
        {
            moveInput = Vector2.zero;
            if (rb != null) rb.velocity = Vector2.zero;
            return;
        }

        moveInput.x = Input.GetAxisRaw("Horizontal");
        moveInput.y = Input.GetAxisRaw("Vertical");

        if (moveInput.sqrMagnitude > 1)
        {
            moveInput.Normalize();
        }
    }

    void FixedUpdate()
    {
        // ★ 내가 조종하는 캐릭터이면서, 얼어붙은 상태가 아닐 때만 물리적 가속도를 부여합니다.
        if (IsOwner && !isFrozen && rb != null && rb.simulated)
        {
            rb.velocity = moveInput * moveSpeed;
        }
    }
}