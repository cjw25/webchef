using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using Unity.Netcode;

public class Door : MonoBehaviour
{
    [Header("이동할 씬 이름")]
    public string nextSceneName;

    [Header("다음 방에서 플레이어가 스폰될 문 이름")]
    public string targetDoorName;

    [Header("★ 텔레포트 안전 거리 (문에서 얼마나 떨어져 소환될지)")]
    public Vector3 spawnOffset = new Vector3(1.5f, 0f, 0f); // 기본값: 오른쪽으로 1.5만큼 떨어짐

    private bool isProcessing = false;

    private void Start()
    {
        isProcessing = false;
        // 💡 [핵심] 다음 방에 도착했을 때, 이 문이 방금 막 넘어온 내 캐릭터를 안전지대로 강제 이송시킵니다.
        StartCoroutine(CheckAndRepositionLocalPlayer());
    }

    private void OnTriggerEnter2D(Collider2D collision)
    {
        if (isProcessing) return;

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
                    playerRb.velocity = Vector2.zero;
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
        // 씬이 로드되고 네트워크 오브젝트들이 완벽히 정돈될 때까지 3프레임 대기
        yield return null;
        yield return null;
        yield return null;

        // 이 문이 다음 방의 '도착지 문'인지 이름으로 확인
        if (gameObject.name == targetDoorName || gameObject.name.Contains(targetDoorName))
        {
            // 현재 맵에 존재하는 모든 플레이어 중 "내 화면의 주인(Owner)"인 캐릭터를 찾습니다.
            foreach (GameObject player in GameObject.FindGameObjectsWithTag("Player"))
            {
                NetworkObject netObj = player.GetComponent<NetworkObject>();
                if (netObj != null && netObj.IsOwner)
                {
                    // 🎯 찾았다! 룸매니저가 문 한가운데 떨군 내 캐릭을 안전거리(오프셋) 밖으로 강제 이송시킵니다.
                    player.transform.position = transform.position + spawnOffset;
                    Debug.Log($"🚀 [버그 박멸] 내 캐릭터를 문에서 {spawnOffset} 만큼 안전하게 떨어뜨렸습니다!");
                    break;
                }
            }
        }
    }
}