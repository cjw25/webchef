using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using Unity.Netcode; // ★ 유니티 넷코드 라이브러리 추가

public class Door : MonoBehaviour
{
    [Header("이동할 씬 이름")]
    public string nextSceneName;

    [Header("다음 방에서 플레이어가 스폰될 문 이름")]
    public string targetDoorName;

    // ★ [멀티플레이 핵심 개조]
    // static 전체 락을 없애고, 현재 '이 문을 통과 중인 플레이어'만 독립적으로 락을 겁니다.
    // 이렇게 해야 플레이어 A가 문을 탈 때, 저 멀리 있는 플레이어 B가 다른 문을 정상적으로 이용할 수 있습니다.
    private HashSet<ulong> transitioningPlayers = new HashSet<ulong>();

    private void Start()
    {
        transitioningPlayers.Clear();
    }

    private void OnTriggerEnter2D(Collider2D collision)
    {
        if (collision.CompareTag("Player"))
        {
            // 플레이어에게 붙은 네트워크 오브젝트 컴포넌트를 가져옵니다.
            NetworkObject netObj = collision.GetComponent<NetworkObject>();

            // ★ 내 화면에서 내가 조종하는 '내 캐릭터'가 부딪힌 경우에만 방 이동을 시작합니다.
            if (netObj != null && netObj.IsOwner)
            {
                ulong localClientId = NetworkManager.Singleton.LocalClientId;

                // 이미 이 문을 통과 처리 중인 유저라면 중복 실행 방지
                if (transitioningPlayers.Contains(localClientId)) return;
                transitioningPlayers.Add(localClientId);

                Debug.Log($"[Door] {gameObject.name} 멀티플레이어 이송 시작 (유저 ID: {localClientId})");

                // 목적지 저장
                if (RoomManager.Instance != null)
                {
                    RoomManager.Instance.RequestChangeRoom(nextSceneName, targetDoorName, localClientId);
                }

                // ★ [통과 버그 원천 종결 치트키 - 멀티 버전]
                // 내 캐릭터의 이동 스크립트를 꺼서 로딩 도중 W키 파고들기를 막습니다.
                PlayerMove playerMove = collision.GetComponent<PlayerMove>();
                if (playerMove != null)
                {
                    playerMove.enabled = false;
                }

                // 속도와 물리, 콜라이더를 꺼서 안전하게 고정합니다.
                Rigidbody2D playerRb = collision.GetComponent<Rigidbody2D>();
                Collider2D playerCol = collision.GetComponent<Collider2D>();

                if (playerRb != null)
                {
                    playerRb.velocity = Vector2.zero;
                    playerRb.simulated = false;
                }
                if (playerCol != null)
                {
                    playerCol.enabled = false;
                }

                // 멀티플레이어용 안전 씬 로드 코루틴 실행
                StartCoroutine(LoadSceneRoutineMultiplayer());
            }
        }
    }

    private IEnumerator LoadSceneRoutineMultiplayer()
    {
        yield return new WaitForEndOfFrame();

        // ★ [멀티플레이 핵심] 
        // 호스트(서버 방장)가 문을 밟았다면 서버 싱글톤을 통해 네트워크 동기화 씬 로드를 실행합니다.
        // 이렇게 하면 접속해 있는 다른 클라이언트 유저들도 다 함께 다음 방으로 이동하게 됩니다.
        if (NetworkManager.Singleton.IsServer)
        {
            NetworkManager.Singleton.SceneManager.LoadScene(nextSceneName, UnityEngine.SceneManagement.LoadSceneMode.Single);
        }
        else
        {
            // 만약 내가 방장이 아니라 일반 클라이언트(손님) 유저라면, 
            // 서버에게 "저 문 밟았으니 방 이동 시켜주세요"라고 요청하는 방식으로 구동됩니다.
            // (이 부분은 뒤이어 수정할 멀티용 RoomManager가 자동으로 처리해 줄 예정입니다)
            Debug.Log("[Door] 클라이언트 유저가 문을 밟아 서버의 씬 전환을 기다립니다.");
        }
    }
}