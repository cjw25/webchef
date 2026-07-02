using System.Collections;
using System.Collections.Generic;
using Unity.Services.Core;
using UnityEngine;

public class NetworkLuncher : MonoBehaviour
{
    
    public async void OnClickHostButton()
    {
        Debug.Log("호스트 시작 버튼 클릭");
        try
        {
            await NetworkAuthManager.Instance.InitializeAuth();

            if(UnityServices.State != ServicesInitializationState.Initialized)
            {
                Debug.LogError("Unity Services 초기화 실패");
                return;
            }
            string joinCode = await RelayManager.Instance.CreateGame(2);
            Debug.Log($"준비 완료, 생성 조인 코드  : {joinCode}");
        }
        catch(System.Exception e)
        {
            Debug.LogError($"호스트 시작 중 오류 발생: {e.Message}");
        }
    }

    public async void OnClickJoinButton(string inputJoinCode)
    {
        Debug.Log("입장 버튼 클릭");

        await NetworkAuthManager.Instance.InitializeAuth();

        await RelayManager.Instance.JoinGame(inputJoinCode);

        Debug.Log("입장 완료");
    }
}
