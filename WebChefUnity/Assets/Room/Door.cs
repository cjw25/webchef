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
    public float spawnDistance = 3.5f;

    private Collider2D doorCollider;
    private bool isSpawnedHere = false; // 내가 이번에 이 문을 통해 태어났는가?

    private void Awake()
    {
        doorCollider = GetComponent<Collider2D>();
    }

    private void Start()
    {
        if (NetworkManager.Singleton != null && NetworkManager.Singleton.SceneManager != null)
        {
            NetworkManager.Singleton.SceneManager.OnSceneEvent += OnNetworkSceneEvent;
        }

        TriggerRepositionCheck();
    }

    private void OnDestroy()
    {
        if (NetworkManager.Singleton != null && NetworkManager.Singleton.SceneManager != null)
        {
            NetworkManager.Singleton.SceneManager.OnSceneEvent -= OnNetworkSceneEvent;
        }
    }

    private void OnNetworkSceneEvent(SceneEvent sceneEvent)
    {
        if (sceneEvent.SceneEventType == SceneEventType.LoadEventCompleted)
        {
            TriggerRepositionCheck();
        }
    }

    private void TriggerRepositionCheck()
    {
        if (RoomManager.Instance != null && !string.IsNullOrEmpty(RoomManager.Instance.targetDoorName))
        {
            if (gameObject.name == RoomManager.Instance.targetDoorName || gameObject.name.Contains(RoomManager.Instance.targetDoorName))
            {
                StopAllCoroutines();
                StartCoroutine(CheckAndRepositionLocalPlayer());
            }
        }
    }

    private void OnTriggerEnter2D(Collider2D collision)
    {
        // 💡 룸매니저가 이동 중이거나, 내가 방금 스폰되어 탈출 중인 문이라면 작동 금지
        if (RoomManager.Instance != null && RoomManager.Instance.isTransferring) return;
        if (isSpawnedHere) return;

        if (collision.CompareTag("Player"))
        {
            NetworkObject netObj = collision.GetComponent<NetworkObject>();
            if (netObj != null && netObj.IsOwner)
            {
                if (RoomManager.Instance != null)
                {
                    RoomManager.Instance.RequestChangeRoom(nextSceneName, targetDoorName);
                }
            }
        }
    }

    // 💡 [문 관통 버그 해결 핵심] 
    // 플레이어가 스폰된 후, 문 콜라이더 영역을 '완전히 걸어서 빠져나갔을 때' 비로소 자물쇠를 완전히 해제합니다.
    private void OnTriggerExit2D(Collider2D collision)
    {
        if (collision.CompareTag("Player"))
        {
            NetworkObject netObj = collision.GetComponent<NetworkObject>();
            if (netObj != null && netObj.IsOwner)
            {
                if (isSpawnedHere)
                {
                    isSpawnedHere = false;
                    if (RoomManager.Instance != null)
                    {
                        RoomManager.Instance.ClearTransferLock(); // 룸매니저 자물쇠도 해제 신호 송신
                    }
                    Debug.Log("🔒 플레이어가 문을 완전히 탈출함. 문 정상 활성화!");
                }
            }
        }
    }

    private IEnumerator CheckAndRepositionLocalPlayer()
    {
        // 도착하자마자 이 문은 센서 체크 대상에서 제외 (자물쇠 작동)
        isSpawnedHere = true;
        if (doorCollider != null) doorCollider.enabled = true; // 트리거는 켜두어야 탈출(Exit)을 감지합니다.

        yield return new WaitForFixedUpdate();
        yield return new WaitForFixedUpdate();

        GameObject localPlayer = null;
        if (NetworkManager.Singleton != null && NetworkManager.Singleton.LocalClient != null)
        {
            var playerObj = NetworkManager.Singleton.LocalClient.PlayerObject;
            if (playerObj != null) localPlayer = playerObj.gameObject;
        }

        if (localPlayer != null)
        {
            Vector2 offset = Vector2.zero;
            switch (spawnDirection)
            {
                case SpawnDirection.Right: offset = Vector2.right * spawnDistance; break;
                case SpawnDirection.Left: offset = Vector2.left * spawnDistance; break;
                case SpawnDirection.Up: offset = Vector2.up * spawnDistance; break;
                case SpawnDirection.Down: offset = Vector2.down * spawnDistance; break;
            }

            Vector3 finalSpawnPos = transform.position + new Vector3(offset.x, offset.y, 0);

            Rigidbody2D rb = localPlayer.GetComponent<Rigidbody2D>();
            if (rb != null)
            {
                rb.velocity = Vector2.zero;
                rb.angularVelocity = 0f;
            }

            if (localPlayer.TryGetComponent<NetworkTransform>(out var netTransform))
            {
                netTransform.Interpolate = false;
                netTransform.enabled = false;

                localPlayer.transform.position = finalSpawnPos;
                netTransform.Teleport(finalSpawnPos, localPlayer.transform.rotation, localPlayer.transform.localScale);

                yield return new WaitForFixedUpdate();

                netTransform.enabled = true;
                netTransform.Interpolate = true;
            }
            else
            {
                localPlayer.transform.position = finalSpawnPos;
            }
        }
    }
}