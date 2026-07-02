using System.Collections;
using System.Collections.Generic;
using Unity.Netcode; // ★ 유니티 넷코드 라이브러리 추가
using Unity.Services.Core;
using UnityEngine;
using UnityEngine.SceneManagement;

// ★ 멀티플레이어용 서버 씬 관리 및 RPC 통신을 위해 NetworkBehaviour를 상속받습니다.
public class RoomManager : NetworkBehaviour
{
    private static RoomManager _instance;

    public static RoomManager Instance
    {
        get
        {
            if (_instance == null)
            {
                _instance = FindObjectOfType<RoomManager>();

                if (_instance == null)
                {
                    GameObject go = new GameObject("RoomManager");
                    _instance = go.AddComponent<RoomManager>();
                    DontDestroyOnLoad(go);
                }
            }
            return _instance;
        }
    }

    [Header("이동 데이터")]
    public string targetDoorName;

    private void Awake()
    {
        transform.SetParent(null);

        if (_instance == null)
        {
            _instance = this;
            DontDestroyOnLoad(gameObject);
        }
        else if (_instance != this)
        {
            Destroy(gameObject);
        }
    }

    public override void OnNetworkSpawn()
    {
        if(UnityServices.State != ServicesInitializationState.Initialized)
        {
            return;
        }
        // 서버인 경우에만 넷코드의 씬 동기화 이벤트에 내 함수를 결합합니다.
        if (IsServer)
        {
            NetworkManager.Singleton.SceneManager.OnLoadComplete += OnMultiplayerSceneLoadComplete;
        }
    }

    public override void OnNetworkDespawn()
    {
        if (IsServer && NetworkManager.Singleton != null && NetworkManager.Singleton.SceneManager != null)
        {
            NetworkManager.Singleton.SceneManager.OnLoadComplete -= OnMultiplayerSceneLoadComplete;
        }
    }

    // ★ [멀티플레이 핵심 기능] 클라이언트가 문을 밟았을 때 서버에게 방 이동을 요청하는 함수
    public void RequestChangeRoom(string sceneName, string doorName, ulong clientId)
    {
        targetDoorName = doorName;

        // 만약 내가 방장(Server)이라면 즉시 씬을 전환합니다.
        if (IsServer)
        {
            NetworkManager.Singleton.SceneManager.LoadScene(sceneName, LoadSceneMode.Single);
        }
        else
        {
            // 내가 손님(Client)이라면 서버에게 씬을 바꿔달라고 네트워크 원격 요청을 보냅니다.
            RequestChangeRoomServerRpc(sceneName, doorName, clientId);
        }
    }

    // 클라이언트가 호출하면 서버 컴퓨터에서 실행되는 네트워크 안전장치
    [ServerRpc(RequireOwnership = false)]
    private void RequestChangeRoomServerRpc(string sceneName, string doorName, ulong clientId)
    {
        targetDoorName = doorName;
        // 서버가 전권을 쥐고 모든 클라이언트의 씬을 동기화하여 전환시킵니다.
        NetworkManager.Singleton.SceneManager.LoadScene(sceneName, LoadSceneMode.Single);
    }

    // ★ [멀티플레이 핵심 개조] 씬 로드가 끝난 후 서버가 플레이어들의 위치를 정밀 동기화시키는 함수
    private void OnMultiplayerSceneLoadComplete(ulong clientId, string sceneName, LoadSceneMode loadSceneMode)
    {
        if (string.IsNullOrEmpty(targetDoorName)) return;

        // 새 방에서 목적지 문을 정확하게 찾아냅니다.
        GameObject targetDoor = GameObject.Find(targetDoorName);

        if (targetDoor != null)
        {
            // 방에 진입한 특정 '클라이언트 ID'를 가진 플레이어 오브젝트만 쏙 골라내어 조준합니다.
            NetworkObject playerNetObj = NetworkManager.Singleton.ConnectedClients[clientId].PlayerObject;

            if (playerNetObj != null)
            {
                // 1. 해당 플레이어의 물리와 스크립트를 정지시킵니다.
                Rigidbody2D playerRb = playerNetObj.GetComponent<Rigidbody2D>();
                Collider2D playerCol = playerNetObj.GetComponent<Collider2D>();
                PlayerMove playerMove = playerNetObj.GetComponent<PlayerMove>();

                if (playerRb != null) { playerRb.velocity = Vector2.zero; playerRb.simulated = false; }
                if (playerCol != null) playerCol.enabled = false;
                if (playerMove != null) playerMove.enabled = false;

                // 2. 문의 살짝 아래 안전 구역 스폰 좌표 계산
                Vector3 spawnPosition = targetDoor.transform.position + (Vector3.down * 1.5f);

                // 3. 서버가 강제로 해당 플레이어의 트랜스폼 위치를 덮어씌워 20명 화면 전체에 동기화합니다.
                playerNetObj.transform.position = spawnPosition;

                // 4. 네트워크 오차를 세척하기 위해 코루틴으로 안전하게 깨웁니다.
                StartCoroutine(SafeActivationRoutineMultiplayer(playerRb, playerCol, playerMove));
            }
        }
    }

    private IEnumerator SafeActivationRoutineMultiplayer(Rigidbody2D rb, Collider2D col, PlayerMove move)
    {
        // 전 세계 인터넷 가속도가 완벽히 좌표를 인지할 때까지 1프레임 대기
        yield return new WaitForEndOfFrame();

        // 안전 구역에 안착했으므로 물리, 콜라이더, 스크립트를 다시 가동합니다.
        if (rb != null) rb.simulated = true;
        if (col != null) col.enabled = true;
        if (move != null) move.enabled = true;

        // 목적지 데이터 초기화
        targetDoorName = "";
    }
    void Update()
    {
        // 게임 중에 키보드 'H' 키를 누르면 강제로 호스트(서버)를 시작합니다.
        //if (Input.GetKeyDown(KeyCode.H))
        //{
        //    if (NetworkManager.Singleton != null)
        //    {
        //        NetworkManager.Singleton.StartHost();
        //        Debug.Log("🚀 [강제 실행] 키보드로 호스트를 시작했습니다!");
        //    }
        //}
    }
}