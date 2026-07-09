using System.Collections;
using System.Collections.Generic;
using Unity.Services.Authentication;
using Unity.Services.Core;
using UnityEngine;

public class NetworkLauncher : MonoBehaviour
{
    public async void OnClickJoinButton(string inputJoinCode)
    {
        Debug.Log("입장 버튼 클릭");
        try
        {
            await NetworkAuthManager.Instance.InitializeAuth();

            await RelayManager.Instance.JoinGame(inputJoinCode);
            Debug.Log("입장 완료");
        }
        catch (System.Exception e)
        {
            Debug.LogError($"입장 중 오류 발생: {e.Message}");
        }
        
    }
}
