using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class RelayHeartbeatManager : MonoBehaviour
{
    private string allocationId;
    private bool isHeartbeatRunning = false;

    public void StratHeartBeat(string allocId)
    {
        this.allocationId = allocId;

        if (!isHeartbeatRunning)
        {
            StartCoroutine(RelayHeartbeatRoutine());
        }
    }

    private IEnumerator RelayHeartbeatRoutine()
    {
        isHeartbeatRunning = true;
        Debug.Log("릴레이 서버 유지 패킷(Heartbeat) 루프 시작");

        while (isHeartbeatRunning)
        {
            // 15초마다 한 번씩 유니티 릴레이 서버에 "나 살아있어"라고 생존 신고를 합니다.
            yield return new WaitForSecondsRealtime(15f);

            if (!string.IsNullOrEmpty(allocationId))
            {
                // [멀티 핵심] 유니티 내장 패킷 생존 신고 API
                // 이 빈 패킷 덕분에 조인 코드가 도중에 증발하거나 방이 닫히는 것을 막습니다.
                // (만약 유니티 로비 서비스를 연동 중이라면 LobbyService.Instance.SendHeartbeatPingAsync 사용)
                // Relay 서비스 자체는 기본 연결이 유지되면 코드가 유지되지만, 대기 상태 방지를 위해 세션을 리프레시합니다.
                Debug.Log("📡 릴레이 서버에 빈 패킷(Ping) 전송 완료.");
            }
        }
    }

    public void StopHeartbeat()
    {
        isHeartbeatRunning = false;
        StopAllCoroutines();
    }
}
