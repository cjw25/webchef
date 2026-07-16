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

    // 씬 완료 후 호출될 함수
    public void ExecuteReposition()
    {
        if (RoomManager.Instance.targetDoorName == gameObject.name)
        {
            StartCoroutine(RepositionPlayer());
        }
    }

    private IEnumerator RepositionPlayer()
    {
        yield return new WaitForEndOfFrame();
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
            if (localPlayer.TryGetComponent<NetworkTransform>(out var nt))
                nt.Teleport(finalPos, localPlayer.transform.rotation, localPlayer.transform.localScale);
            else
                localPlayer.transform.position = finalPos;

            RoomManager.Instance.targetDoorName = "";
           // RoomManager.Instance.SetAllPlayersPhysicsState(true);
        }
    }

    private void OnTriggerEnter2D(Collider2D collision)
    {
        if (RoomManager.Instance.IsTransferring) return;
        if (collision.CompareTag("Player") && collision.GetComponent<NetworkObject>()?.IsOwner == true)
        {
            RoomManager.Instance.RequestChangeRoom(nextSceneName, targetDoorName);
        }
    }
}