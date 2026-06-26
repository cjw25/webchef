using System.Collections;
using UnityEngine;
using UnityEngine.SceneManagement;
using Unity.Netcode;

public class RoomManager : MonoBehaviour
{
    public static RoomManager Instance { get; private set; }

    [Header("다음 방에서 플레이어가 도착할 문 이름")]
    public string targetDoorName;

    // 🔒 무한 와리가리 연쇄 반응을 차단하는 전역 마스터 키 자물쇠
    public bool isTransferring { get; private set; } = false;

    private void Awake()
    {
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
        if (isTransferring) return; // 이미 방 이동 프로세스가 진행 중이면 완전 씹음

        this.targetDoorName = targetDoorName;
        StartCoroutine(ChangeRoomRoutine(sceneName));
    }

    private IEnumerator ChangeRoomRoutine(string sceneName)
    {
        isTransferring = true; // 🚨 자물쇠 꽉 잠금! (새 방 문 트리거 가동 방지)

        // 넷코드 씬 전환 명령 실행
        NetworkManager.Singleton.SceneManager.LoadScene(sceneName, LoadSceneMode.Single);

        // 💡 [초핵심] 새 방으로 넘어가서 플레이어가 스폰되고 문이 바깥 평지로 밀어낼 때까지 
        // 넉넉하게 2초간 문 자물쇠 상태를 해제하지 않고 유지합니다.
        yield return new WaitForSeconds(2.0f);

        if (NetworkManager.Singleton.LocalClient != null && NetworkManager.Singleton.LocalClient.PlayerObject != null)
        {
            GameObject player = NetworkManager.Singleton.LocalClient.PlayerObject.gameObject;
            Rigidbody2D rb = player.GetComponent<Rigidbody2D>();
            Collider2D col = player.GetComponent<Collider2D>();

            if (rb != null) rb.simulated = true;
            if (col != null) col.enabled = true;
        }

        // 목적지 데이터 리셋 및 와리가리 방지 락 해제
        targetDoorName = "";
        isTransferring = false;
        Debug.Log("🔓 [와리가리 락 해제] 이제 플레이어가 다른 문으로 안전하게 이동할 수 있습니다.");
    }

    private void Update()
    {
        if (Input.GetKeyDown(KeyCode.H))
        {
            if (NetworkManager.Singleton != null)
            {
                NetworkManager.Singleton.StartHost();
                Debug.Log("🚀 [강제 실행] 키보드로 호스트를 시작했습니다!");
            }
        }
    }
}