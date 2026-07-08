using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using Unity.Netcode;
using Unity.Netcode.Components;

public class Door : MonoBehaviour
{
    [Header("이동할 씬 이름")]
    public string nextSceneName;

    [Header("다음 방에서 플레이어가 스폰될 문 이름")]
    public string targetDoorName;

    [Header("★ 문에서 방 안쪽으로 밀어낼 거리")]
    public float spawnDistance = 2.5f;

    private Collider2D doorCollider;
    private static bool globalTransferLock = false;

    private void Awake()
    {
        doorCollider = GetComponent<Collider2D>();
    }

    private void Start()
    {
        globalTransferLock = false;

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
        if (globalTransferLock) return;
        if (RoomManager.Instance != null && RoomManager.Instance.isTransferring) return;

        if (collision.CompareTag("Player"))
        {
            NetworkObject netObj = collision.GetComponent<NetworkObject>();

            if (netObj != null && netObj.IsOwner)
            {
                globalTransferLock = true;
                if (doorCollider != null) doorCollider.enabled = false;

                Rigidbody2D playerRb = collision.GetComponent<Rigidbody2D>();
                if (playerRb != null) playerRb.velocity = Vector2.zero;

                if (RoomManager.Instance != null)
                {
                    RoomManager.Instance.RequestChangeRoom(nextSceneName, targetDoorName, NetworkManager.Singleton.LocalClientId);
                }
                else
                {
                    globalTransferLock = false;
                    if (doorCollider != null) doorCollider.enabled = true;
                }
            }
        }
    }

    private IEnumerator CheckAndRepositionLocalPlayer()
    {
        if (doorCollider != null) doorCollider.enabled = false;
        globalTransferLock = true;

        // 씬 전환 후 넷코드 정렬 대기
        yield return new WaitForSeconds(0.2f);

        GameObject localPlayer = null;
        foreach (GameObject player in GameObject.FindGameObjectsWithTag("Player"))
        {
            NetworkObject netObj = player.GetComponent<NetworkObject>();
            if (netObj != null && netObj.IsOwner)
            {
                localPlayer = player;
                break;
            }
        }

        if (localPlayer != null)
        {
            // 💡 [지옥의 핑퐁 버그 수정 핵심 연산]
            // 문의 위치에서 맵 중심(0, 0)을 바라보는 방향 벡터를 구합니다.
            // 이렇게 하면 문이 상하좌우 어디에 있든 상관없이 '무조건 방 안쪽 안전한 맨바닥' 방향이 계산됩니다.
            Vector3 doorPos = transform.position;
            Vector3 centerDirection = (Vector3.zero - doorPos).normalized;

            // X축 이동량이 더 크면 좌우 문, Y축 이동량이 더 크면 상하 문으로 판단하여 정밀 보정합니다.
            Vector3 offset = Vector3.zero;
            if (Mathf.Abs(centerDirection.x) > Mathf.Abs(centerDirection.y))
            {
                // 좌우 이동 (X축으로만 밀어내기)
                offset = new Vector3(Mathf.Sign(centerDirection.x) * spawnDistance, 0, 0);
            }
            else
            {
                // 상하 이동 (Y축으로만 밀어내기)
                offset = new Vector3(0, Mathf.Sign(centerDirection.y) * spawnDistance, 0);
            }

            Vector3 finalSpawnPos = doorPos + offset;

            Rigidbody2D rb = localPlayer.GetComponent<Rigidbody2D>();
            if (rb != null)
            {
                rb.velocity = Vector2.zero;
                rb.angularVelocity = 0f;
            }

            if (localPlayer.TryGetComponent<NetworkTransform>(out var netTransform))
            {
                netTransform.enabled = false;
                localPlayer.transform.position = finalSpawnPos;
                netTransform.Teleport(finalSpawnPos, localPlayer.transform.rotation, localPlayer.transform.localScale);

                yield return new WaitForFixedUpdate();
                netTransform.enabled = true;
            }
            else
            {
                localPlayer.transform.position = finalSpawnPos;
            }
        }

        // 플레이어가 안전한 맨바닥에 정착했으므로 0.3초 뒤 문을 활성화합니다.
        yield return new WaitForSeconds(0.3f);
        if (doorCollider != null) doorCollider.enabled = true;
        globalTransferLock = false;
    }
}