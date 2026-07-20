using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.SceneManagement;
using Unity.Netcode;

public class RoomManager : MonoBehaviour
{
    public static RoomManager Instance { get; private set; }
    public string targetDoorName;
    public bool IsTransferring { get; private set; } = false;

    private void Awake()
    {
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
        IsTransferring = false;

        // ★ 서버가 씬 로드를 완벽히 마쳤을 때 실행할 이벤트 연결
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

    /// <summary>
    /// 방 이동 요청 (중복 정의된 메서드를 하나로 깔끔하게 통합)
    /// </summary>
    public void RequestChangeRoom(string sceneName, string targetDoorName)
    {
        if (IsTransferring) return;

        IsTransferring = true;
        this.targetDoorName = targetDoorName;

        // 씬 이동 직전, 모든 플레이어의 물리를 안전하게 꺼줍니다.
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

    // ★ Netcode가 "모든 유저가 새 씬에 완전히 도착했어!"라고 알려주는 신호 처리
    private void HandleAllClientsSceneLoaded(ulong clientId, string sceneName, LoadSceneMode loadSceneMode)
    {
        StartCoroutine(AllPlayersPhysicsActiveRoutine());
    }

    private IEnumerator AllPlayersPhysicsActiveRoutine()
    {
        // 새 씬에서 캐릭터들의 온네트워크 스폰 및 좌표 이동이 끝날 때까지 대기
        yield return new WaitForSecondsRealtime(0.05f);
        yield return new WaitForFixedUpdate();

        // 이제 모든 플레이어의 물리를 원상복구(Dynamic) 시켜줍니다.
        SetAllPlayersPhysicsState(true);
    }

    public void ClearTransferLock()
    {
        IsTransferring = false;
        SetAllPlayersPhysicsState(true); // 문 밖으로 완벽히 나갔을 때 물리 복구
    }

    /// <summary>
    /// 로컬 및 모든 원격 플레이어의 물리/콜라이더 컴포넌트를 일괄 제어하는 함수 (오타 완벽 수정)
    /// </summary>
    private void SetAllPlayersPhysicsState(bool isActive)
    {
        if (NetworkManager.Singleton == null)
        {
            // 싱글플레이 예외 처리 로직
            PlayerMove singlePlayer = FindFirstObjectByType<PlayerMove>();
            if (singlePlayer != null)
            {
                Rigidbody2D rb = singlePlayer.GetComponent<Rigidbody2D>();
                Collider2D col = singlePlayer.GetComponent<Collider2D>();
                if (rb != null) rb.bodyType = isActive ? RigidbodyType2D.Dynamic : RigidbodyType2D.Kinematic;
                if (col != null) col.enabled = isActive;
            }
            return;
        }

        // 현재 씬에 배치된 모든 PlayerMove 오브젝트를 싹 긁어모읍니다.
        PlayerMove[] allPlayers = FindObjectsByType<PlayerMove>(FindObjectsSortMode.None);

        foreach (PlayerMove pMove in allPlayers)
        {
            Rigidbody2D rb = pMove.GetComponent<Rigidbody2D>();
            Collider2D col = pMove.GetComponent<Collider2D>();

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
                    // 활성화할 때, 내 캐릭터가 아니라면(타인) 물리를 Kinematic으로 유지하여 
                    // Netcode의 좌표 동기화(네트워크 패킷)를 물리 연산이 방해하지 않게 보호합니다.
                    if (pMove.IsOwner)
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

            if (col != null)
            {
                col.enabled = isActive;
            }
        }
    }
}