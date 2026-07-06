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
        //DontDestroyOnLoad(gameObject);  //최상위 부모만 적용이 가능함
    }

    private void Start()
    {
        isTransferring = false;
    }

    // Door에서 넘겨받은 요청으로 방 전환 Coroutine을 실행하는 함수
    public void RequestChangeRoom(string sceneName, string targetDoorName, ulong clientId)
    {
        if (isTransferring) return; // 이미 이동 중이면 무시

        this.targetDoorName = targetDoorName;
        StartCoroutine(ChangeRoomRoutine(sceneName, clientId));
    }

    private IEnumerator ChangeRoomRoutine(string sceneName, ulong clientId)
    {
        isTransferring = true; // 문 철통 잠금!

        // 넷코드 공식 씬 로더 가동
        NetworkManager.Singleton.SceneManager.LoadScene(sceneName, LoadSceneMode.Single);

        // 💡 씬이 완전히 로드되고 목적지 문(Door.cs)이 플레이어 위치를 밀어낼 때까지 넉넉하게 대기합니다.
        yield return new WaitForSeconds(0.6f);

        // 내 화면의 로컬 플레이어를 찾아옵니다.
        if (NetworkManager.Singleton.LocalClient != null && NetworkManager.Singleton.LocalClient.PlayerObject != null)
        {
            GameObject player = NetworkManager.Singleton.LocalClient.PlayerObject.gameObject;

            Rigidbody2D rb = player.GetComponent<Rigidbody2D>();
            Collider2D col = player.GetComponent<Collider2D>();

            // 💡 [원래 코드 복원] 안전 구역에 안착했으므로 물리, 콜라이더 등을 다시 가동합니다.
            if (rb != null) rb.simulated = true;
            if (col != null) col.enabled = true;

            // 만약 플레이어 이동 스크립트 이름이 PlayerMovement 등이라면 아래 주석을 풀고 맞춰주세요.
            // var move = player.GetComponent<PlayerMovement>();
            // if (move != null) move.enabled = true;
        }

        // 목적지 데이터 초기화 및 자물쇠 해제
        targetDoorName = "";

        // 걸어나갈 시간 조금 더 확보 후 쿨타임 해제
        yield return new WaitForSeconds(1.0f);
        isTransferring = false;
        Debug.Log("🔓 [이동 완료] 모든 데이터 초기화 및 문 잠금 해제.");
    }

    private void Update()
    {
        // 게임 중에 키보드 'H' 키를 누르면 강제로 호스트(서버)를 시작합니다.
        //if (Input.GetKeyDown(KeyCode.H))
        //{
        //    if (NetworkManager.Singleton != null)
        //    {
        //        NetworkManager.Singleton.StartHost();
        //        Debug.Log("🚀 [강제 실행] 키보드로 호스트를 시작했습니다!");
        //    }
        //}
    }
}