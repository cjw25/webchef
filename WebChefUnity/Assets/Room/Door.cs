using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using Unity.Netcode;

public class Door : MonoBehaviour
{
    // 인스펙터에서 마우스로 고를 수 있는 방향 종류들
    public enum SpawnDirection { Right, Left, Up, Down }

    [Header("이동할 씬 이름")]
    public string nextSceneName;

    [Header("다음 방에서 플레이어가 스폰될 문 이름")]
    public string targetDoorName;

    [Header("★ 플레이어가 튕겨져 나올 방향")]
    public SpawnDirection spawnDirection = SpawnDirection.Right;

    [Header("★ 문에서 얼마나 멀리 떨어질지 거리")]
    public float spawnDistance = 1.5f;

    private bool isProcessing = false;

    private void Start()
    {
        isProcessing = false;
        StartCoroutine(CheckAndRepositionLocalPlayer());
    }

    private void OnTriggerEnter2D(Collider2D collision)
    {
        if (isProcessing) return;

        if (collision.CompareTag("Player"))
        {
            NetworkObject netObj = collision.GetComponent<NetworkObject>();

            if (netObj != null && netObj.IsOwner)
            {
                isProcessing = true;
                ulong localClientId = NetworkManager.Singleton.LocalClientId;

                Debug.Log($"[Door] {gameObject.name} -> {nextSceneName} 이동 시작!");

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
                    Debug.LogError("[Door] RoomManager가 없습니다!");
                    isProcessing = false;
                }
            }
        }
    }

    private IEnumerator CheckAndRepositionLocalPlayer()
    {
        yield return null;
        yield return null;
        yield return null;

        if (gameObject.name == targetDoorName || gameObject.name.Contains(targetDoorName))
        {
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

                    player.transform.position = transform.position + offset;
                    Debug.Log($"🚀 [스폰 완료] 캐릭터가 문의 {spawnDirection} 방향으로 {spawnDistance}만큼 떨어져 소환되었습니다.");
                    break;
                }
            }
        }
    }
}