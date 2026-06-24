using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class PlayerMove : MonoBehaviour
{
    [Header("이동 설정")]
    public float moveSpeed = 5f;

    private Rigidbody2D rb;
    private Vector2 moveInput;

    // ★ 중복 생성 방지를 위한 플레이어 싱글톤 인스턴스
    public static PlayerMove Instance;

    private void Awake()
    {
        // [방역 핵심] 이미 다른 방에서 넘어와 생존해 있는 진짜 플레이어가 있다면
        if (Instance == null)
        {
            Instance = this;
            DontDestroyOnLoad(gameObject); // 이 오리지널 플레이어만 파괴되지 않고 살아남음
        }
        else
        {
            // 방이 바뀌면서 새로 만들어진 가짜(중복) 플레이어는 즉시 자폭시킵니다.
            Destroy(gameObject);
            return;
        }
    }

    void Start()
    {
        rb = GetComponent<Rigidbody2D>();
        rb.gravityScale = 0f;
        rb.constraints = RigidbodyConstraints2D.FreezeRotation;

        // ★ 방에 새로 입장하자마자 문 위치로 이동 처리
        HandleRoomTeleport();

        // 방에 새로 입장하자마자 내 머리 위 말풍선 글자 싹 비우기
        TMPro.TextMeshProUGUI bubbleText = GetComponentInChildren<TMPro.TextMeshProUGUI>();
        if (bubbleText != null)
        {
            bubbleText.text = "";
        }
    }

    void Update()
    {
        // ★ 핵심 안전장치: 지금 채팅 매니저가 켜져 있고 유저가 타이핑 중이라면 움직임 완전 차단
        if (ChatManager.Instance != null && ChatManager.Instance.IsTyping())
        {
            moveInput = Vector2.zero;
            if (rb != null)
            {
                rb.velocity = Vector2.zero; // 채팅 칠 때 미끄러지는 현상 방지
            }
            return; // 아래의 WASD 이동 입력을 무시하고 빠져나감
        }

        // 키보드 WASD / 방향키 입력 받기
        moveInput.x = Input.GetAxisRaw("Horizontal");
        moveInput.y = Input.GetAxisRaw("Vertical");

        // 대각선 이동 속도 보정 (대각선으로 갈 때 빨라지는 현상 방지)
        if (moveInput.sqrMagnitude > 1)
        {
            moveInput.Normalize();
        }
    }

    void FixedUpdate()
    {
        // 중복 파괴 연산 중에 컴포넌트 참조 에러 방지용 안전 필터
        if (rb != null)
        {
            rb.velocity = moveInput * moveSpeed;
        }
    }

    // 문 위치를 찾아 부드럽게 순간이동시키는 함수 (중복 코드 정리)
    private void HandleRoomTeleport()
    {
        if (RoomManager.Instance != null && !string.IsNullOrEmpty(RoomManager.Instance.targetDoorName))
        {
            // 하이어라키에서 타겟 문 찾기
            GameObject targetDoor = GameObject.Find(RoomManager.Instance.targetDoorName);

            if (targetDoor != null)
            {
                if (rb != null)
                {
                    rb.velocity = Vector2.zero;
                    rb.simulated = false; // 물리 잠시 끄기
                }

                // 문 위치에서 살짝 아래로 안전 스폰 (벽 끼임 방지)
                Vector3 spawnPosition = targetDoor.transform.position + (Vector3.down * 1.2f);
                transform.position = spawnPosition;

                if (rb != null)
                {
                    rb.simulated = true; // 물리 켜기
                }

                // 순간이동 성공했으므로 문 이름 비우기
                RoomManager.Instance.targetDoorName = "";
            }
            else
            {
                // [방어벽 코드] 문 이름은 전송됐는데 만약 오브젝트를 못 찾았다면? 
                // 씬에 존재하는 '아무 문'이나 하나 잡아서 그 근처로 강제 이동시켜 끼임을 방지합니다.
                GameObject anyDoor = GameObject.FindWithTag("Door") ?? GameObject.Find("Door_To_Room2") ?? GameObject.Find("Door_To_Room1");
                if (anyDoor != null)
                {
                    transform.position = anyDoor.transform.position + (Vector3.down * 1.2f);
                }
            }
        }
    }
}