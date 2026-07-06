using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using Unity.Netcode;
using Unity.Netcode.Components; // ★ NetworkTransform 사용을 위해 추가

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
    public float spawnDistance = 2.5f; // 조금 넉넉하게 2.5f 추천합니다!

    private bool isProcessing = false;

    private void Start()
    {
        isProcessing = false;

        // 💡 [핵심] 다음 방에 도착했을 때, 이 문이 방금 막 넘어온 내 캐릭터를 안전지대로 강제 이송시킵니다.
        if (RoomManager.Instance != null)
        {
            // 현재 방에 도달한 목적지 문이 나인지 매칭 확인 후 실행
            if (gameObject.name == RoomManager.Instance.targetDoorName || gameObject.name.Contains(RoomManager.Instance.targetDoorName))
            {
                StartCoroutine(CheckAndRepositionLocalPlayer());
            }
        }
    }

    private void OnTriggerEnter2D(Collider2D collision)
    {
        if (isProcessing) return;

        // 🔒 룸 매니저가 쿨타임(락) 상태라면 작동을 철저히 거부합니다.
        if (RoomManager.Instance != null && RoomManager.Instance.isTransferring) return;

        if (collision.CompareTag("Player"))
        {
            NetworkObject netObj = collision.GetComponent<NetworkObject>();

            // 내 화면의 내 캐릭터가 문에 닿으면 즉시 방 이동 요청
            if (netObj != null && netObj.IsOwner)
            {
                isProcessing = true;
                ulong localClientId = NetworkManager.Singleton.LocalClientId;

                Debug.Log($"[Door] {gameObject.name} 즉시 이동 요청! (유저 ID: {localClientId})");

                Rigidbody2D playerRb = collision.GetComponent<Rigidbody2D>();
                if (playerRb != null)
                {
                    playerRb.velocity = Vector2.zero; // 문으로 들어가던 속도 멈춤
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

    // 💡 다음 방의 씬이 켜지면서 이 문이 생성될 때 자동으로 실행되는 버그 박멸 루틴
    private IEnumerator CheckAndRepositionLocalPlayer()
    {
        // 씬이 로드되고 네트워크 오브젝트들이 완전히 정돈될 때까지 5프레임 넉넉히 대기
        yield return null;
        yield return null;
        yield return null;
        yield return null;
        yield return null;

        // 현재 맵에 존재하는 모든 플레이어 중 "내 화면의 주인(Owner)"인 캐릭터를 찾습니다.
        foreach (GameObject player in GameObject.FindGameObjectsWithTag("Player"))
        {
            NetworkObject netObj = player.GetComponent<NetworkObject>();
            if (netObj != null && netObj.IsOwner)
            {
                // 🎯 선택한 방향에 따라 좌표 오프셋을 똑똑하게 계산합니다.
                Vector3 offset = Vector3.zero;

                switch (spawnDirection)
                {
                    case SpawnDirection.Right: offset = new Vector3(spawnDistance, 0, 0); break;
                    case SpawnDirection.Left: offset = new Vector3(-spawnDistance, 0, 0); break;
                    case SpawnDirection.Up: offset = new Vector3(0, spawnDistance, 0); break;
                    case SpawnDirection.Down: offset = new Vector3(0, -spawnDistance, 0); break;
                }

                Vector3 finalSpawnPos = transform.position + offset;

                // 물리 가속도 완전 초기화 및 링킹 오류 방지
                Rigidbody2D rb = player.GetComponent<Rigidbody2D>();
                if (rb != null)
                {
                    rb.velocity = Vector2.zero;
                    rb.angularVelocity = 0f;
                }

                // ⚡ [중요 안전장치] NetworkTransform이 좌표를 강제로 뺏어가는 넷코드 억까 부수기
                if (player.TryGetComponent<NetworkTransform>(out var netTransform))
                {
                    netTransform.Teleport(finalSpawnPos, player.transform.rotation, player.transform.localScale);
                }
                else
                {
                    player.transform.position = finalSpawnPos;
                }

                Debug.Log($"🚀 [스폰 완료] 캐릭터가 {gameObject.name}의 {spawnDirection} 방향으로 {spawnDistance}만큼 떨어져 소환되었습니다! ({finalSpawnPos})");
                break;
            }
        }
    }
}