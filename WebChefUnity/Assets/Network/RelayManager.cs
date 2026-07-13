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
    // ⭐️ 싱글톤 타입을 RelayManager로 올바르게 수정했습니다.
    public static RelayManager Instance { get; private set; }

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
        await UnityServices.InitializeAsync();
        if (!AuthenticationService.Instance.IsSignedIn)
        {
            await AuthenticationService.Instance.SignInAnonymouslyAsync();
            Debug.Log($"로그인 완료 (유저 ID: {AuthenticationService.Instance.PlayerId})");
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
            await LobbyService.Instance.SendHeartbeatPingAsync(_hostLobby.Id);
        }
    }

    /// <summary>
    /// 방 만들기 (Relay 생성 후 -> 그 코드를 Lobby에 심어서 방 개설)
    /// </summary>
    public async Task CreateLobby(string lobbyName, int maxPlayers)
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
                    {
                        RELAY_KEY, new DataObject(DataObject.VisibilityOptions.Member, relayJoinCode)
                    }
                }
            };

            Lobby lobby = await LobbyService.Instance.CreateLobbyAsync(lobbyName, maxPlayers, options);
            _hostLobby = lobby;
            _joinedLobby = lobby;

            Debug.Log($"로비 생성 완료: {lobby.Name} (코드: {relayJoinCode})");

            NetworkManager.Singleton.StartHost();
        }
        catch (LobbyServiceException e)
        {
            Debug.LogError($"로비 생성 실패: {e.Message}");
        }
    }

    /// <summary>
    /// 방 목록 조회하기
    /// </summary>
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
    /// 방 입장하기 (Lobby 입장 -> 숨겨진 Relay 코드를 꺼내서 NGO 접속)
    /// </summary>
    public async Task JoinLobby(Lobby lobby)
    {
        try
        {
            _joinedLobby = await LobbyService.Instance.JoinLobbyByIdAsync(lobby.Id);

            string relayJoinCode = _joinedLobby.Data[RELAY_KEY].Value;
            Debug.Log($"로비 입장 성공. 릴레이 코드로 접속 시도: {relayJoinCode}");

            JoinAllocation joinAllocation = await RelayService.Instance.JoinAllocationAsync(relayJoinCode);

            var unityTransport = NetworkManager.Singleton.GetComponent<UnityTransport>();
            unityTransport.SetRelayServerData(new RelayServerData(joinAllocation, "udp"));

            NetworkManager.Singleton.StartClient();
        }
        catch (LobbyServiceException e)
        {
            Debug.LogError($"로비 입장 실패: {e.Message}");
        }
    }
}