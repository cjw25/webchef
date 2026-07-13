using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using Unity.Netcode;
using Unity.Netcode.Transports.UTP;
using Unity.Networking.Transport.Relay;
using Unity.Services.Authentication;
using Unity.Services.Core;
using Unity.Services.Lobbies;
using Unity.Services.Lobbies.Models;
using Unity.Services.Relay;
using Unity.Services.Relay.Models;
using UnityEngine;

public class RelayManager : MonoBehaviour
{
    public static RelayManager Instance { get; private set; }

    // UI에서 초기화 완료 타이밍을 캐치할 수 있도록 이벤트 제공
    public event Action OnAuthenticationComplete;
    public bool IsAuthInitialized { get; private set; } = false;

    private Lobby _hostLobby;
    private Lobby _joinedLobby;
    private float _heartbeatTimer;
    private const string RELAY_KEY = "RelayJoinCode";

    private void Awake()
    {
        if (Instance != null && Instance != this) Destroy(gameObject);
        else Instance = this;
    }

    private async void Start()
    {
        try
        {
            await UnityServices.InitializeAsync();
            if (!AuthenticationService.Instance.IsSignedIn)
            {
                await AuthenticationService.Instance.SignInAnonymouslyAsync();
                Debug.Log($"로그인 완료 (유저 ID: {AuthenticationService.Instance.PlayerId})");
            }
            IsAuthInitialized = true;
            OnAuthenticationComplete?.Invoke(); // UI 버튼들을 깨워주는 신호 발송
        }
        catch (Exception e)
        {
            Debug.LogError($"UGS 서비스 초기화 실패: {e.Message}");
        }
    }

    private void Update()
    {
        HandleLobbyHeartbeat();
    }

    private async void HandleLobbyHeartbeat()
    {
        if (_hostLobby == null) return;

        _heartbeatTimer -= Time.deltaTime;
        if (_heartbeatTimer <= 0f)
        {
            _heartbeatTimer = 15f;
            try
            {
                await LobbyService.Instance.SendHeartbeatPingAsync(_hostLobby.Id);
            }
            catch (LobbyServiceException e)
            {
                Debug.LogWarning($"로비 하트비트 실패 (방이 터졌을 수 있음): {e.Message}");
            }
        }
    }

    /// <summary>
    /// UI 롤백을 위해 성공 여부를 bool 타입으로 반환하도록 개선
    /// </summary>
    public async Task<bool> CreateLobby(string lobbyName, int maxPlayers)
    {
        try
        {
            Allocation allocation = await RelayService.Instance.CreateAllocationAsync(maxPlayers - 1);
            string relayJoinCode = await RelayService.Instance.GetJoinCodeAsync(allocation.AllocationId);

            var unityTransport = NetworkManager.Singleton.GetComponent<UnityTransport>();
            unityTransport.SetRelayServerData(new RelayServerData(allocation, "udp"));

            CreateLobbyOptions options = new CreateLobbyOptions
            {
                IsPrivate = false,
                Data = new Dictionary<string, DataObject>
                {
                    { RELAY_KEY, new DataObject(DataObject.VisibilityOptions.Member, relayJoinCode) }
                }
            };

            Lobby lobby = await LobbyService.Instance.CreateLobbyAsync(lobbyName, maxPlayers, options);
            _hostLobby = lobby;
            _joinedLobby = lobby;

            Debug.Log($"로비 생성 완료: {lobby.Name} (코드: {relayJoinCode})");

            NetworkManager.Singleton.StartHost();
            return true;
        }
        catch (Exception e)
        {
            Debug.LogError($"로비 생성 단계 실패: {e.Message}");
            return false;
        }
    }

    public async Task<List<Lobby>> QueryLobbies()
    {
        try
        {
            QueryLobbiesOptions options = new QueryLobbiesOptions
            {
                Filters = new List<QueryFilter>
                {
                    new QueryFilter(QueryFilter.FieldOptions.AvailableSlots, "0", QueryFilter.OpOptions.GT)
                }
            };

            QueryResponse response = await LobbyService.Instance.QueryLobbiesAsync(options);
            return response.Results;
        }
        catch (LobbyServiceException e)
        {
            Debug.LogError($"로비 조회 실패: {e.Message}");
            return null;
        }
    }

    /// <summary>
    /// UI 롤백을 위해 성공 여부를 bool 타입으로 반환하도록 개선
    /// </summary>
    public async Task<bool> JoinLobby(Lobby lobby)
    {
        try
        {
            _joinedLobby = await LobbyService.Instance.JoinLobbyByIdAsync(lobby.Id);

            if (_joinedLobby.Data == null || !_joinedLobby.Data.ContainsKey(RELAY_KEY))
            {
                Debug.LogError("로비 데이터에 Relay 정보가 유실되었습니다.");
                return false;
            }

            string relayJoinCode = _joinedLobby.Data[RELAY_KEY].Value;
            Debug.Log($"로비 입장 성공. 릴레이 코드로 접속 시도: {relayJoinCode}");

            JoinAllocation joinAllocation = await RelayService.Instance.JoinAllocationAsync(relayJoinCode);

            var unityTransport = NetworkManager.Singleton.GetComponent<UnityTransport>();
            unityTransport.SetRelayServerData(new RelayServerData(joinAllocation, "udp"));

            NetworkManager.Singleton.StartClient();
            return true;
        }
        catch (Exception e)
        {
            Debug.LogError($"로비 입장 단계 실패: {e.Message}");
            return false;
        }
    }
}