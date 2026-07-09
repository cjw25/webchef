using Unity.Netcode;
using Unity.Netcode.Transports.UTP;
using Unity.Services.Core;
using Unity.Services.Authentication;
using Unity.Services.Relay;
using Unity.Services.Relay.Models;
using UnityEngine;
using System.Threading.Tasks;
using System.IO;

[RequireComponent(typeof(UnityTransport))]
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

    private async void Start()
    {
        // 전용 서버 빌드라면 화면이 켜지자마자 자동으로 가동 프로세스를 시작합니다.
#if UNITY_SERVER
        Debug.Log("[전용 서버 감지] 자동으로 검증 서버 가동 프로세스를 시작합니다.");
        await AutoStartServer();
#else
        Debug.Log("[클라이언트 모드] 플레이어가 직접 버튼을 눌러 접속할 때까지 대기합니다.");
#endif
    }

    /// <summary>
    /// 전용 서버용 자동 초기화 및 방 생성 프로세스
    /// </summary>
    private async Task AutoStartServer()
    {
        try
        {
            // 1. 유니티 코어 서비스 초기화
            if (UnityServices.State != ServicesInitializationState.Initialized)
            {
                Debug.Log("[서버 초기화] 1/3 유니티 서비스 초기화 중...");
                await UnityServices.InitializeAsync();
            }

            // 2. 익명 인증 로그인
            if (!AuthenticationService.Instance.IsSignedIn)
            {
                Debug.Log("[서버 초기화] 2/3 유니티 익명 인증 로그인 중...");
                await AuthenticationService.Instance.SignInAnonymouslyAsync();
            }
            Debug.Log($"[서버 초기화] 인증 성공! 고유 서버 ID: {AuthenticationService.Instance.PlayerId}");

            // 3. Relay 생성 및 서버 가동
            Debug.Log("[서버 초기화] 3/3 Relay 방 생성 및 가동 시작...");
            string code = await CreateGame(4);

            if (!string.IsNullOrEmpty(code))
            {
                Debug.Log($"[서버 초기화 완료] ★ 서버가 성공적으로 작동 중입니다! 코드: {code}");
            }
            else
            {
                Debug.LogError("[서버 초기화 실패] CreateGame이 빈 코드를 반환했습니다.");
            }
        }
        catch (System.Exception e)
        {
            Debug.LogError($"[서버 자동 시작 중 치명적 에러] {e.Message}\n{e.StackTrace}");
        }
    }

    public async Task<string> CreateGame(int maxPlayers)
    {
        try
        {
            // 1. 릴레이 할당 생성 (무료 서버 안에서 릴레이 통로를 개설)
            Allocation allocation = await RelayService.Instance.CreateAllocationAsync(maxPlayers);
            var relayServerData = new Unity.Networking.Transport.Relay.RelayServerData(allocation, "dtls");
            transport.SetRelayServerData(relayServerData);

#if UNITY_SERVER
            // 2. [무료 서버 빌드] 화면 없이 백그라운드에서 검증 로직만 수행하는 서버 시작
            if (NetworkManager.Singleton.StartServer())
            {
                string joinCode = await RelayService.Instance.GetJoinCodeAsync(allocation.AllocationId);

                // 오라클이나 유니티 호스팅 같은 리눅스/윈도우 서버 환경에서 안전하게 경로 확보
                string filePath = Path.Combine(System.AppDomain.CurrentDomain.BaseDirectory, "server_join_code.txt");
                await File.WriteAllTextAsync(filePath, joinCode);

                Debug.Log($"[전용 서버 가동 성공] 검증 서버가 시작되었습니다. 코드: {joinCode}");
                RelayHeartbeatManager heartbeat = gameObject.AddComponent<RelayHeartbeatManager>();
            heartbeat.StartHeartbeat(allocation.AllocationId.ToString());
                return joinCode;
            }
            else
            {
                Debug.LogError("[전용 서버] NetworkManager.Singleton.StartServer() 실패");
                return null;
            }
#else
            // 3. [일반 유저 빌드] 에디터 및 클라이언트 환경에서는 이 함수가 직접 구동되지 않으므로 가이드 로그만 출력
            Debug.Log("[클라이언트 모드] 클라이언트는 CreateGame을 직접 호출하지 않습니다. 조인 코드를 이용해 접속하세요.");
            return null;
#endif
        }
        catch (System.Exception e)
        {
            Debug.LogError($"[서버 생성 실패] {e.Message}");
            return null;
        }
    }

    public async Task JoinGame(string joinCode)
    {
        try
        {
            // [★ 수정 안전장치] 입력받은 문자열의 앞뒤 공백을 자르고 강제로 대문자로 치환해 오류 방지
            string cleanJoinCode = joinCode.Trim().ToUpper();

            if (string.IsNullOrEmpty(cleanJoinCode))
            {
                Debug.LogError("[클라이언트 접속 실패] 입력된 조인 코드가 비어있습니다.");
                return;
            }

            Debug.Log($"[릴레이 접속] 입력된 코드 검증 완료: '{cleanJoinCode}'로 릴레이 방 접속 시도...");
            JoinAllocation joinAllocation = await RelayService.Instance.JoinAllocationAsync(cleanJoinCode);
            var relayServerData = new Unity.Networking.Transport.Relay.RelayServerData(joinAllocation, "dtls");
            transport.SetRelayServerData(relayServerData);

            Debug.Log($"현재 NetworkManager 작동 상태: {NetworkManager.Singleton.IsListening}");
            Debug.Log($"등록된 플레이어 프리랩 존재 여부: {NetworkManager.Singleton.NetworkConfig.PlayerPrefab != null}");

            if (NetworkManager.Singleton.StartClient())
            {
                Debug.Log("[클라이언트] NGO 엔진 시작 성공! 서버에 최종 연결 중...");
            }
            else
            {
                Debug.LogError("[클라이언트] NGO 엔진 시작 실패 (NetworkManager가 이미 가동 중이거나 설정 오류)");
            }
        }
        catch (System.Exception e)
        {
            Debug.LogError($"[클라이언트 접속 예외] {e.Message}");
        }
    }
}