using System.Collections;
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

    private void Start()
    {
        if (NetworkManager.Singleton?.SceneManager != null)
            NetworkManager.Singleton.SceneManager.OnSceneEvent += OnNetworkSceneEvent;

        // 시작할 때도 체크 (첫 씬 로딩 등을 위해)
        TriggerRepositionCheck();
    }

    private void OnDestroy()
    {
        if (NetworkManager.Singleton?.SceneManager != null)
            NetworkManager.Singleton.SceneManager.OnSceneEvent -= OnNetworkSceneEvent;
    }

    private void OnNetworkSceneEvent(SceneEvent sceneEvent)
    {
        if (sceneEvent.SceneEventType == SceneEventType.LoadEventCompleted)
            TriggerRepositionCheck();
    }

    private void TriggerRepositionCheck()
    {
        if (RoomManager.Instance != null && RoomManager.Instance.targetDoorName == gameObject.name)
        {
            StartCoroutine(TeleportLocalPlayer());
        }
    }

    private IEnumerator TeleportLocalPlayer()
    {
        // 서버 동기화가 완료될 때까지 충분히 대기
        yield return new WaitForSeconds(0.6f);

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

            // 텔레포트 수행
            if (localPlayer.TryGetComponent<NetworkTransform>(out var netTransform))
                netTransform.Teleport(finalPos, localPlayer.transform.rotation, localPlayer.transform.localScale);
            else
                localPlayer.transform.position = finalPos;

            // 위치 이동 완료 후 RoomManager 정보 초기화
            RoomManager.Instance.targetDoorName = "";
            Debug.Log($"[Door] {gameObject.name}로 플레이어 이동 완료");
            PlayerManager.OnBoxCollider2D();
        }
    }

    private void OnTriggerEnter2D(Collider2D collision)
    {
        if (RoomManager.Instance.isTransferring) return;
        if (collision.CompareTag("Player") && collision.GetComponent<NetworkObject>()?.IsOwner == true)
        {
            RoomManager.Instance.RequestChangeRoom(nextSceneName, targetDoorName);
        }
    }
}