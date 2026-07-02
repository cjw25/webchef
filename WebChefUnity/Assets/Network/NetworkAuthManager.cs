using Unity.Services.Core;
using Unity.Services.Authentication;
using UnityEngine;
using System.Threading.Tasks;

public class NetworkAuthManager : MonoBehaviour
{

    public static NetworkAuthManager Instance { get; private set; }
    public bool IsInitialized { get; private set; } = false;

    public string PlayerId => AuthenticationService.Instance.PlayerId;

    private void Awake()
    {
        if(Instance == null)
        {
            Instance = this;
            DontDestroyOnLoad(gameObject);
        }
        else
        {
            Destroy(gameObject);
        }
    }
    public async Task InitializeAuth()
    {
        if(IsInitialized)
        {
            return;
        }
        await UnityServices.InitializeAsync();
        await AuthenticationService.Instance.SignInAnonymouslyAsync();

        IsInitialized = true;
        Debug.Log("로그인 완료!");
    }
}