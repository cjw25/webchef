using System.Collections;
using System.Collections.Generic;
using Unity.Services.Authentication;
using Unity.Services.Core;
using UnityEngine;

public class NetworkLauncher : MonoBehaviour
{




    public async void OnClickHostButton()
    {
        Debug.Log("호스트 시작 버튼 클릭");
        try
        {
            // 1. 유니티 서비스 초기화 보장
            if (UnityServices.State != ServicesInitializationState.Initialized)
            {
                await UnityServices.InitializeAsync();
            }

            // 2. 익명 로그인 보장
            if (!AuthenticationService.Instance.IsSignedIn)
            {
                await AuthenticationService.Instance.SignInAnonymouslyAsync();
            }

            // 3. 이제 Relay 생성
            string joinCode = await RelayManager.Instance.CreateGame(2);
            Debug.Log($"준비 완료, 생성 조인 코드 : {joinCode}");
        }
        catch (System.Exception e)
        {
            Debug.LogError($"[치명적 에러] 호스트 시작 실패: {e.Message}\n{e.StackTrace}");
        }
    }

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
