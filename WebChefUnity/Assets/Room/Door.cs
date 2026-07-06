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

    [Header("다음 방에서 플레이어가 스폰될 문 이름")]
    public string targetDoorName;

    [Header("★ 플레이어가 튕겨져 나올 방향")]
    public SpawnDirection spawnDirection = SpawnDirection.Right;

    [Header("★ 문에서 얼마나 멀리 떨어질지 거리")]
    public float spawnDistance = 2.0f;

    private bool isProcessing = false;

    private void Start()
    {
        isProcessing = false;

        // 새 씬이 켜질 때, 내가 룸 매니저에 등록된 도착지 문인지 확인하고 사출 실행
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
        if (isProcessing) return;
        if (RoomManager.Instance != null && RoomManager.Instance.isTransferring) return;

        if (collision.CompareTag("Player"))
        {
            NetworkObject netObj = collision.GetComponent<NetworkObject>();

            if (netObj != null && netObj.IsOwner)
            {
                isProcessing = true;
                ulong localClientId = NetworkManager.Singleton.LocalClientId;

                Rigidbody2D playerRb = collision.GetComponent<Rigidbody2D>();
                if (playerRb != null) playerRb.velocity = Vector2.zero;

                if (RoomManager.Instance != null)
                {
                    RoomManager.Instance.RequestChangeRoom(nextSceneName, targetDoorName, localClientId);
                }
                else
                {
                    isProcessing = false;
                }
            }
        }
    }

    private IEnumerator CheckAndRepositionLocalPlayer()
    {
        // 씬 로딩 후 동기화 대기
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

                // NetworkTransform 우회 텔레포트
                if (player.TryGetComponent<NetworkTransform>(out var netTransform))
                {
                    netTransform.Teleport(finalSpawnPos, player.transform.rotation, player.transform.localScale);
                }
                else
                {
                    player.transform.position = finalSpawnPos;
                }
                break;
            }
        }
    }
}