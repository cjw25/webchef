using UnityEngine;
using Unity.Netcode;
using Unity.Netcode.Transports.UTP;

public class ServerManager : MonoBehaviour
{
    private void Start()
    {
        // 💡 매우 중요: 빌드된 결과물이 '전용 서버'인지 '일반 플레이어(클라이언트)'인지 구별합니다.
#if DEDICATED_SERVER
        // 만약 유니티 빌드 타겟을 Dedicated Server로 해서 뽑은 파일이라면
        // 게임이 켜지자마자 자동으로 서버를 오픈합니다.
        StartDedicatedServer();
#else
        // 일반 클라이언트 빌드(PC, 모바일 등)나 에디터 상태라면
        // 개발 편의를 위해 키보드 단축키로 서버/클라이언트를 켤 수 있게 처리합니다.
        Debug.Log("[ServerManager] 키보드 단축키 안내 -> S: 서버 열기, C: 클라이언트 접속");
#endif
    }

    private void Update()
    {
        // 유니티 에디터나 일반 PC 빌드에서 테스트할 때 쓰는 키보드 단축키입니다.
#if !DEDICATED_SERVER
        if (Input.GetKeyDown(KeyCode.S))
        {
            StartDedicatedServer();
        }
        if (Input.GetKeyDown(KeyCode.C))
        {
            StartClientConnect();
        }
#endif
    }

    /// <summary>
    /// 오라클 클라우드 혹은 로컬 PC에서 순수 전용 서버를 구동하는 메서드
    /// </summary>
    public void StartDedicatedServer()
    {
        Debug.Log("[ServerManager] 전용 서버 모드로 구동을 시작합니다...");

        // 서버는 어떤 주소로 들어오는 연결이든 전부 받아들여야 하므로 Listen 주소를 0.0.0.0으로 세팅합니다.
        var transport = NetworkManager.Singleton.GetComponent<UnityTransport>();
        transport.SetConnectionData("127.0.0.1", 7777, "0.0.0.0");

        // 순수 서버 모드로 NGO 가동!
        NetworkManager.Singleton.StartServer();
    }

    /// <summary>
    /// 플레이어가 서버에 접속할 때 사용하는 메서드
    /// </summary>
    public void StartClientConnect()
    {
        Debug.Log("[ServerManager] 서버에 접속을 시도합니다...");

        // 💡 로컬 테스트 단계를 넘어 오라클 클라우드에 올린 후에는
        // "127.0.0.1" 대신 실제 오라클 VM의 [공용 IP 주소]를 적어주면 됩니다.
        var transport = NetworkManager.Singleton.GetComponent<UnityTransport>();
        transport.SetConnectionData("127.0.0.1", 7777);

        // 클라이언트 모드로 NGO 가동!
        NetworkManager.Singleton.StartClient();
    }
}