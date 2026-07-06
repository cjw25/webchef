using System.Collections;
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
        if (Instance != null && Instance != this)
        {
            Destroy(gameObject);
            return;
        }
        Instance = this;

        // 🔑 [주석 해제] 이제 하이어라키 최상위에 둘 것이므로 씬이 바뀌어도 파괴되지 않게 방어합니다.
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

        NetworkManager.Singleton.SceneManager.LoadScene(sceneName, LoadSceneMode.Single);

        // 💡 씬이 완전히 로드되고 목적지 문(Door.cs)이 플레이어 위치를 밀어낼 때까지 대기
        yield return new WaitForSeconds(0.6f);

        if (NetworkManager.Singleton.LocalClient != null && NetworkManager.Singleton.LocalClient.PlayerObject != null)
        {
            GameObject player = NetworkManager.Singleton.LocalClient.PlayerObject.gameObject;

            Rigidbody2D rb = player.GetComponent<Rigidbody2D>();
            Collider2D col = player.GetComponent<Collider2D>();

            if (rb != null) rb.simulated = true;
            if (col != null) col.enabled = true;
        }

        targetDoorName = "";

        // 걸어나갈 시간 확보 후 자물쇠 해제 (와리가리 원천 차단)
        yield return new WaitForSeconds(1.0f);
        isTransferring = false;
        Debug.Log("🔓 [이동 완료] 모든 데이터 초기화 및 문 잠금 해제.");
    }
}