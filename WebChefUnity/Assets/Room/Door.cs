using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using Unity.Netcode;
using Unity.Netcode.Components;

public class Door : MonoBehaviour
{
    public enum SpawnDirection { Right, Left, Up, Down }
    public string nextSceneName;
    public string targetDoorName;
    public SpawnDirection spawnDirection = SpawnDirection.Right;
    public float spawnDistance = 3.5f;

    private Collider2D doorCollider;
    private bool isSpawnedHere = false;

    private void Awake() => doorCollider = GetComponent<Collider2D>();

    private void Start()
    {
        if (NetworkManager.Singleton?.SceneManager != null)
            NetworkManager.Singleton.SceneManager.OnSceneEvent += OnNetworkSceneEvent;
        TriggerRepositionCheck();
    }

    private void OnDestroy()
    {
        if (NetworkManager.Singleton?.SceneManager != null)
            NetworkManager.Singleton.SceneManager.OnSceneEvent -= OnNetworkSceneEvent;
    }

    private void OnNetworkSceneEvent(SceneEvent sceneEvent)
    {
        if (sceneEvent.SceneEventType == SceneEventType.LoadEventCompleted) TriggerRepositionCheck();
    }

    private void TriggerRepositionCheck()
    {
        if (RoomManager.Instance != null && !string.IsNullOrEmpty(RoomManager.Instance.targetDoorName))
        {
            if (gameObject.name == RoomManager.Instance.targetDoorName)
            {
                StopAllCoroutines();
                StartCoroutine(CheckAndRepositionLocalPlayer());
            }
        }
    }

    private void OnTriggerEnter2D(Collider2D collision)
    {
        if (RoomManager.Instance == null || RoomManager.Instance.isTransferring || isSpawnedHere) return;
        if (collision.CompareTag("Player") && collision.GetComponent<NetworkObject>()?.IsOwner == true)
        {
            RoomManager.Instance.RequestChangeRoom(nextSceneName, targetDoorName);
        }
    }

    private void OnTriggerExit2D(Collider2D collision)
    {
        if (isSpawnedHere && collision.CompareTag("Player") && collision.GetComponent<NetworkObject>()?.IsOwner == true)
        {
            isSpawnedHere = false;
            RoomManager.Instance?.ClearTransferLock();
        }
    }

    private IEnumerator CheckAndRepositionLocalPlayer()
    {
        isSpawnedHere = true;
        if (doorCollider != null) doorCollider.enabled = true;
        yield return null;

        var localPlayer = NetworkManager.Singleton?.LocalClient?.PlayerObject?.gameObject;
        if (localPlayer != null)
        {
            Vector3 offset = spawnDirection switch
            {
                SpawnDirection.Right => Vector3.right * spawnDistance,
                SpawnDirection.Left => Vector3.left * spawnDistance,
                SpawnDirection.Up => Vector3.up * spawnDistance,
                _ => Vector3.down * spawnDistance
            };

            Vector3 finalPos = transform.position + offset;
            var rb = localPlayer.GetComponent<Rigidbody2D>();

            // 💡 물리 초기화: Teleport 전후로 물리 엔진을 잠시 재우는 방식 적용
            if (rb != null) { rb.Sleep(); rb.velocity = Vector2.zero; }

            if (localPlayer.TryGetComponent<NetworkTransform>(out var netTransform))
                netTransform.Teleport(finalPos, localPlayer.transform.rotation, localPlayer.transform.localScale);
            else
                localPlayer.transform.position = finalPos;

            if (rb != null) rb.WakeUp();
            if (RoomManager.Instance != null) RoomManager.Instance.targetDoorName = "";
        }
    }
}