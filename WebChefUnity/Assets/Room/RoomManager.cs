using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.SceneManagement;
using Unity.Netcode;

public class RoomManager : MonoBehaviour
{
    public static RoomManager Instance { get; private set; }

    [Header("다음 방에서 플레이어가 도착할 문 이름")]
    public string targetDoorName;

    // 🔒 무한 와리가리 방지용 자물쇠
    public bool isTransferring { get; private set; } = false;

    private void Awake()
    {
        // 💡 [DontDestroy 에러 수정] 부모가 있다면 끊어내어 에러를 방지합니다.
        if (transform.parent != null)
        {
            transform.SetParent(null);
        }

        if (Instance != null && Instance != this)
        {
            Destroy(gameObject);
            return;
        }
        Instance = this;
        DontDestroyOnLoad(gameObject);
    }

    private void Start()
    {
        isTransferring = false;
    }

    public void RequestChangeRoom(string sceneName, string targetDoorName, ulong clientId)
    {
        if (isTransferring) return;

        this.targetDoorName = targetDoorName;
        StartCoroutine(ChangeRoomRoutine(sceneName, clientId));
    }

    private IEnumerator ChangeRoomRoutine(string sceneName, ulong clientId)
    {
        isTransferring = true; // 문 철통 잠금!

        // 💡 [네트워크 상태 체크] 호스트/서버 상태가 정상일 때만 넷코드 씬 로드를 사용하고, 아닐 땐 일반 로드를 씁니다.
        if (NetworkManager.Singleton != null && NetworkManager.Singleton.IsServer)
        {
            NetworkManager.Singleton.SceneManager.LoadScene(sceneName, LoadSceneMode.Single);
        }
        else if (NetworkManager.Singleton == null || !NetworkManager.Singleton.IsClient)
        {
            // 멀티플레이 네트워크가 안 켜져 있을 때를 대비한 싱글플레이 안전장치
            SceneManager.LoadScene(sceneName);
        }

        // 💡 [치명적 타이밍 버그 수정] 
        // 기존의 0.6초 고정 대기를 삭제했습니다! 이제 씬이 로드되자마자 
        // Door.cs의 CheckAndRepositionLocalPlayer()가 즉시 실행되어 위치를 밀어냅니다.
        yield return null;

        // 💡 플레이어가 완벽하게 문 밖 안전지대(3.5 거리)로 탈출해서 걸어나갈 수 있도록 
        // 문 자물쇠(isTransferring)를 충분히(1.5초) 유지해 줍니다.
        yield return new WaitForSeconds(1.5f);

        targetDoorName = "";
        isTransferring = false;
        Debug.Log("🔓 [이동 완료] 모든 데이터 초기화 및 문 잠금 해제.");
    }
}