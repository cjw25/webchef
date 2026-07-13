using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.SceneManagement;
using Unity.Netcode;

public class RoomManager : MonoBehaviour
{
    public static RoomManager Instance { get; private set; }
    public string targetDoorName;
    public bool isTransferring { get; private set; } = false;

    private void Awake()
    {
        if (Instance != null && Instance != this) { Destroy(gameObject); return; }
        Instance = this;
        DontDestroyOnLoad(gameObject);
    }

    public void RequestChangeRoom(string sceneName, string targetDoorName)
    {
        if (isTransferring) return;
        this.targetDoorName = targetDoorName;
        StartCoroutine(ChangeRoomRoutine(sceneName));
    }

    private IEnumerator ChangeRoomRoutine(string sceneName)
    {
        isTransferring = true;
        SetPlayerPhysicsState(false); // 이동 시작 시 물리 정지

        if (NetworkManager.Singleton != null && NetworkManager.Singleton.IsServer)
            NetworkManager.Singleton.SceneManager.LoadScene(sceneName, LoadSceneMode.Single);
        else
            SceneManager.LoadScene(sceneName);

        yield break;
    }

    public void ClearTransferLock()
    {
        isTransferring = false;
        SetPlayerPhysicsState(true); // 문 밖으로 완벽히 나갔을 때 물리 복구
    }

    private void SetPlayerPhysicsState(bool isActive)
    {
        var player = NetworkManager.Singleton?.LocalClient?.PlayerObject?.gameObject;
        if (player == null) return;

        Rigidbody2D rb = player.GetComponent<Rigidbody2D>();
        if (rb != null)
        {
            rb.bodyType = isActive ? RigidbodyType2D.Dynamic : RigidbodyType2D.Kinematic;
            rb.velocity = Vector2.zero;
        }
        if (player.TryGetComponent<Collider2D>(out var col)) col.enabled = isActive;
    }
}