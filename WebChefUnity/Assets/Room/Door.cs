using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using Unity.Netcode;
using Unity.Netcode.Components;

public class Door : MonoBehaviour
{
    public enum SpawnDirection { Right, Left, Up, Down }

    [Header("이동할 씬 이름")]
    public string nextSceneName;

    [Header("다음 방에서 내가 도착할 문 이름")]
    public string targetDoorName;

    [Header("★ 플레이어가 튕겨져 나올 방향")]
    public SpawnDirection spawnDirection = SpawnDirection.Right;

    [Header("★ 문에서 얼마나 멀리 떨어질지 거리")]
    public float spawnDistance = 2.5f;

    private bool isProcessing = false;

    private void Start()
    {
        isProcessing = false;

        // 💡 새 씬이 열릴 때 내 룸 매니저에 등록된 targetDoorName이 나랑 일치하면 튕겨내기 실행
        if (RoomManager.Instance != null)
        {
            if (gameObject.name == RoomManager.Instance.targetDoorName || gameObject.name.Contains(RoomManager.Instance.targetDoorName))
            {
                StartCoroutine(CheckAndRepositionLocalPlayer());
            }
        }
    }

    private void OnTriggerEnter2D(Collider2D collision)
    {
        // 🔒 [와리가리 지옥 봉쇄] 룸 매니저가 방 이동 프로세스 중(자물쇠 잠금)이라면 트리거를 완전히 무시합니다!
        if (RoomManager.Instance != null && RoomManager.Instance.isTransferring)
        {
            return;
        }

        if (isProcessing) return;

        if (collision.CompareTag("Player"))
        {
            NetworkObject netObj = collision.GetComponent<NetworkObject>();

            // 내 화면의 내 캐릭터가 문에 닿으면 즉시 방 이동 요청
            if (netObj != null && netObj.IsOwner)
            {
                isProcessing = true;
                ulong localClientId = NetworkManager.Singleton.LocalClientId;

                Debug.Log($"🚨 [단계 1 성공] {gameObject.name} 문에 내 플레이어가 정상 충돌함! 씬 이동 요청을 보냅니다.");

                Rigidbody2D playerRb = collision.GetComponent<Rigidbody2D>();
                if (playerRb != null)
                {
                    playerRb.velocity = Vector2.zero; // 진입 가속도 즉시 제어
                }

                if (RoomManager.Instance != null)
                {
                    RoomManager.Instance.RequestChangeRoom(nextSceneName, targetDoorName, localClientId);
                }
                else
                {
                    Debug.LogError("[Door] RoomManager가 없어 이동 실패!");
                    isProcessing = false;
                }
            }
        }
    }

    private IEnumerator CheckAndRepositionLocalPlayer()
    {
        // 씬이 변경되고 넷코드가 캐릭터 동기화를 마칠 때까지 5프레임 안전하게 대기
        yield return null; yield return null; yield return null; yield return null; yield return null;

        foreach (GameObject player in GameObject.FindGameObjectsWithTag("Player"))
        {
            NetworkObject netObj = player.GetComponent<NetworkObject>();
            if (netObj != null && netObj.IsOwner)
            {
                Vector3 offset = Vector3.zero;

                switch (spawnDirection)
                {
                    case SpawnDirection.Right: offset = new Vector3(spawnDistance, 0, 0); break;
                    case SpawnDirection.Left: offset = new Vector3(-spawnDistance, 0, 0); break;
                    case SpawnDirection.Up: offset = new Vector3(0, spawnDistance, 0); break;
                    case SpawnDirection.Down: offset = new Vector3(0, -spawnDistance, 0); break;
                }

                Vector3 finalSpawnPos = transform.position + offset;

                Rigidbody2D rb = player.GetComponent<Rigidbody2D>();
                if (rb != null)
                {
                    rb.velocity = Vector2.zero;
                    rb.angularVelocity = 0f;
                }

                // ⚡ Netcode의 NetworkTransform 강제 좌표 통제 권한 우회 텔레포트
                if (player.TryGetComponent<NetworkTransform>(out var netTransform))
                {
                    netTransform.Teleport(finalSpawnPos, player.transform.rotation, player.transform.localScale);
                }
                else
                {
                    player.transform.position = finalSpawnPos;
                }

                Debug.Log($"🔥 [텔레포트 완료] 캐릭터가 {gameObject.name} 문 기준 {spawnDirection} 방향 평지로 사출되었습니다!");
                break;
            }
        }
    }
}