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

    private void Start()
    {
        isTransferring = false;
        if (NetworkManager.Singleton != null && NetworkManager.Singleton.SceneManager != null)
        {
            NetworkManager.Singleton.SceneManager.OnLoadComplete += HandleAllClientsSceneLoaded;
        }
    }

    private void OnDestroy()
    {
        if (NetworkManager.Singleton != null && NetworkManager.Singleton.SceneManager != null)
        {
            NetworkManager.Singleton.SceneManager.OnLoadComplete -= HandleAllClientsSceneLoaded;
        }
    }

    public void RequestChangeRoom(string sceneName, string targetDoorName)
    {
        if (isTransferring) return;

        isTransferring = true;
        this.targetDoorName = targetDoorName;

        // 씬 이동 전 모든 캐릭터 물리 정지
        SetAllPlayersPhysicsState(false);

        if (NetworkManager.Singleton != null && NetworkManager.Singleton.IsServer)
            NetworkManager.Singleton.SceneManager.LoadScene(sceneName, LoadSceneMode.Single);
        else
            SceneManager.LoadScene(sceneName);
    }

    private void HandleAllClientsSceneLoaded(ulong clientId, string sceneName, LoadSceneMode loadSceneMode)
    {
        StartCoroutine(AllPlayersPhysicsActiveRoutine());
    }

    private IEnumerator AllPlayersPhysicsActiveRoutine()
    {
        yield return new WaitForSecondsRealtime(0.05f);
        yield return new WaitForFixedUpdate();

        // 물리 복구
        SetAllPlayersPhysicsState(true);
    }

    public void ClearTransferLock()
    {
        isTransferring = false;
        SetAllPlayersPhysicsState(true);
    }

    private void SetAllPlayersPhysicsState(bool isActive)
    {
        if (NetworkManager.Singleton == null) return;

        // PlayerMove 컴포넌트가 모든 플레이어 캐릭터에 들어있다고 가정
        PlayerMove[] allPlayers = FindObjectsByType<PlayerMove>(FindObjectsSortMode.None);

        foreach (PlayerMove player in allPlayers)
        {
            Rigidbody2D rb = player.GetComponent<Rigidbody2D>();
            Collider2D col = player.GetComponent<Collider2D>();

            if (rb != null)
            {
                if (!isActive)
                {
                    rb.velocity = Vector2.zero;
                    rb.angularVelocity = 0f;
                    rb.bodyType = RigidbodyType2D.Kinematic;
                }
                else
                {
                    // 본인 캐릭터만 Dynamic, 나머지는 동기화 방지를 위해 Kinematic 유지
                    rb.bodyType = player.IsOwner ? RigidbodyType2D.Dynamic : RigidbodyType2D.Kinematic;
                    rb.velocity = Vector2.zero;
                }
            }
            if (col != null) col.enabled = isActive;
        }
    }
}