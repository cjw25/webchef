using Unity.Netcode;
using Unity.Netcode.Transports.UTP;
using Unity.Services.Relay;
using Unity.Services.Relay.Models;
using UnityEngine;
using System.Threading.Tasks;

public class RelayManager : MonoBehaviour
{
    // 외부에서 참조할 수 있게 인스턴스화하거나, 싱글톤으로 만듭니다.
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

    public void SetRelayData(Allocation allocation)
    {
        var relayServerData = new Unity.Networking.Transport.Relay.RelayServerData(allocation, "dtls");
        transport.SetRelayServerData(relayServerData);
    }

    public async Task<string> CreateGame(int maxPlayers)
    {
        Allocation allocation = await RelayService.Instance.CreateAllocationAsync(maxPlayers);
        var relayServerData = new Unity.Networking.Transport.Relay.RelayServerData(allocation, "dtls");
        transport.SetRelayServerData(relayServerData);
        //NetworkManager.Singleton.StartHost();
        return await RelayService.Instance.GetJoinCodeAsync(allocation.AllocationId);
    }

    public async Task JoinGame(string joinCode)
    {
        JoinAllocation joinAllocation = await RelayService.Instance.JoinAllocationAsync(joinCode);
        var relayServerData = new Unity.Networking.Transport.Relay.RelayServerData(joinAllocation, "dtls");
        transport.SetRelayServerData(relayServerData);
        NetworkManager.Singleton.StartClient();
    }
}