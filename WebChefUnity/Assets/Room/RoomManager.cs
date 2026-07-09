using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.SceneManagement;
using Unity.Netcode;

public class RoomManager : MonoBehaviour
{
    public static RoomManager Instance { get; private set; }

    [Header("다음 방에서 플레이어가 도착할 문 이름")]
    public string targetDoorName;

    // 🔒 무한 와리가리 및 관통 차단용 전역 자물쇠
    public bool isTransferring { get; private set; } = false;

    private void Awake()
    {
        if (transform.parent != null) transform.SetParent(null);

        if (Instance != null && Instance != this)
        {
            Destroy(gameObject);
            return;
        }
        Instance = this;
        DontDestroyOnLoad(gameObject);
    }

    private void Start()
    {
        isTransferring = false;

        // ★ [멀티플레이 핵심] 서버가 씬 로드를 완벽히 마쳤을 때 실행할 이벤트 연결
        if (NetworkManager.Singleton != null && NetworkManager.Singleton.SceneManager != null)
        {
            NetworkManager.Singleton.SceneManager.OnLoadComplete += HandleAllClientsSceneLoaded;
        }
    }

    private void OnDestroy()
    {
        // 메모리 누수 방지를 위한 이벤트 해제
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

        // 씬 이동 직전, '모든' 플레이어의 물리를 안전하게 꺼줍니다.
        SetAllPlayersPhysicsState(false);

        if (NetworkManager.Singleton != null && NetworkManager.Singleton.IsServer)
        {
            // Netcode 전용 씬 로더 구동 (서버가 명령하면 클라이언트들도 같이 이동)
            NetworkManager.Singleton.SceneManager.LoadScene(sceneName, LoadSceneMode.Single);
        }
        else if (NetworkManager.Singleton == null || !NetworkManager.Singleton.IsClient)
        {
            // 싱글플레이 환경 백업용
            SceneManager.LoadScene(sceneName);
        }
    }

    // ★ [새로운 핵심 함수] Netcode가 "자, 모든 유저가 새 씬에 완전히 도착했어!"라고 알려주는 시점입니다.
    private void HandleAllClientsSceneLoaded(ulong clientId, string sceneName, LoadSceneMode loadSceneMode)
    {
        // 0.4초 동안 불안하게 기다리는 대신, 유니티 네트워크 시스템의 공식 완료 신호를 받아 처리합니다.
        StartCoroutine(AllPlayersPhysicsActiveRoutine());
    }

    private IEnumerator AllPlayersPhysicsActiveRoutine()
    {
        // 새 씬에서 캐릭터들의 OnNetworkSpawn()과 좌표 이동(텔레포트)이 안전하게 끝날 때까지 아주 잠깐 대기
        yield return new WaitForSecondsRealtime(0.05f);
        yield return new WaitForFixedUpdate();

        // 이제 모든 플레이어의 물리를 원상복구(Dynamic) 시켜줍니다.
        SetAllPlayersPhysicsState(true);
    }

    public void ClearTransferLock()
    {
        this.targetDoorName = "";
        isTransferring = false;
        Debug.Log("🔓 [자물쇠 완전 해제] 플레이어가 안전지대로 나갔으므로 다음 이동이 가능합니다.");
    }

    // ★ [수정된 함수] 내 캐릭터(LocalClient)만 챙기던 것에서, 접속된 '모든 캐릭터'를 제어하도록 변경
    private void SetAllPlayersPhysicsState(bool isActive)
    {
        if (NetworkManager.Singleton == null) return;

        // 현재 씬에 태어나 있는 모든 PlayerMove(캐릭터들) 오브젝트를 싹 다 긁어모읍니다.
        PlayerMove[] allPlayers = FindObjectsByType<PlayerMove>(FindObjectsSortMode.None);

        foreach (PlayerMove player in allPlayers)
        {
            // 내가 소유한 캐릭터가 아니더라도, 일단 독립 서버/클라이언트 물리 공간에서 꼬이지 않도록 일괄 제어합니다.
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
                    // 활성화할 때, 내 캐릭터가 아니라면(타인) 물리를 Kinematic으로 유지하여 Netcode의 좌표 동기화를 방해하지 않게 합니다.
                    if (player.IsOwner)
                    {
                        rb.bodyType = RigidbodyType2D.Dynamic;
                    }
                    else
                    {
                        rb.bodyType = RigidbodyType2D.Kinematic;
                    }
                    rb.velocity = Vector2.zero;
                }
            }

            if (col != null) col.enabled = isActive;
        }
    }
}