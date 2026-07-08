using Unity.Netcode;
using Unity.Netcode.Transports.UTP;
using Unity.Services.Relay;
using Unity.Services.Relay.Models;
using UnityEngine;
using System.Threading.Tasks;
using System.IO;

public class RelayManager : MonoBehaviour
{
    public static RelayManager Instance { get; private set; }
    private UnityTransport transport;

    private void Awake()
    {
        if (Instance == null)
        {
            Instance = this;
            DontDestroyOnLoad(gameObject);
            transport = GetComponent<UnityTransport>();
        }
        else
        {
            Destroy(gameObject);
        }
    }

    public async Task<string> CreateGame(int maxPlayers)
    {
        // 1. 릴레이 할당 생성
        Allocation allocation = await RelayService.Instance.CreateAllocationAsync(maxPlayers);
        var relayServerData = new Unity.Networking.Transport.Relay.RelayServerData(allocation, "dtls");
        transport.SetRelayServerData(relayServerData);

#if UNITY_SERVER
        // [전용 서버 모드] 서버 빌드일 때만 실행
        if (NetworkManager.Singleton.StartServer())
        {
            string joinCode = await RelayService.Instance.GetJoinCodeAsync(allocation.AllocationId);

            // 서버 실행 파일(.exe)이 있는 폴더에 저장
            string filePath = Path.Combine(System.AppDomain.CurrentDomain.BaseDirectory, "server_join_code.txt");
            File.WriteAllText(filePath, joinCode);

            Debug.Log($"[서버] 파일 저장 성공: {filePath}");
            return joinCode;
        }
        else
        {
            Debug.LogError("[서버] 시작 실패");
            return null;
        }
#else
        // [호스트 모드] 에디터 및 일반 클라이언트 테스트용
        if (NetworkManager.Singleton.StartHost())
        {
            string joinCode = await RelayService.Instance.GetJoinCodeAsync(allocation.AllocationId);
            
            // 에디터에서는 'Application.persistentDataPath'를 써야 권한 문제가 없습니다!
            string filePath = Path.Combine(Application.persistentDataPath, "server_join_code.txt");
            File.WriteAllText(filePath, joinCode);
            
            Debug.Log($"[호스트] 에디터 모드 파일 저장: {filePath}");
            return joinCode;
        }
        else
        {
            Debug.LogError("[호스트] 시작 실패");
            return null;
        }
#endif
    }

    public async Task JoinGame(string joinCode)
    {
        JoinAllocation joinAllocation = await RelayService.Instance.JoinAllocationAsync(joinCode);
        var relayServerData = new Unity.Networking.Transport.Relay.RelayServerData(joinAllocation, "dtls");
        transport.SetRelayServerData(relayServerData);

        if (NetworkManager.Singleton.StartClient())
        {
            Debug.Log("[클라이언트] 접속 성공!");
        }
        else
        {
            Debug.LogError("[클라이언트] 접속 실패");
        }
    }
}