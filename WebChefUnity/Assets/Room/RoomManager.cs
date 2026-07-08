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

    // 🔒 무한 와리가리 및 관통 차단용 전역 자물쇠
    public bool isTransferring { get; private set; } = false;

    private void Awake()
    {
        if (transform.parent != null) transform.SetParent(null);

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

    public void RequestChangeRoom(string sceneName, string targetDoorName)
    {
        if (isTransferring) return;
        StartCoroutine(ChangeRoomRoutine(sceneName, targetDoorName));
    }

    private IEnumerator ChangeRoomRoutine(string sceneName, string targetDoorName)
    {
        isTransferring = true;
        this.targetDoorName = targetDoorName;

        SetPlayerPhysicsState(false);

        if (NetworkManager.Singleton != null && NetworkManager.Singleton.IsServer)
        {
            NetworkManager.Singleton.SceneManager.LoadScene(sceneName, LoadSceneMode.Single);
        }
        else if (NetworkManager.Singleton == null || !NetworkManager.Singleton.IsClient)
        {
            SceneManager.LoadScene(sceneName);
        }

        // 씬 로드가 끝날 때까지 대기
        yield return new WaitForSeconds(0.4f);
        yield return new WaitForFixedUpdate();

        // 💡 위치 조정을 마치고 물리를 켭니다. 이제 문 콜라이더 밖으로 걸어 나가야 합니다.
        SetPlayerPhysicsState(true);

        // 대충 시간초로 풀던 구버전 코드 삭제 (이제 풀기 신호는 Door의 OnTriggerExit2D가 줍니다)
    }

    // 💡 Door.cs에서 플레이어가 완벽히 탈출했을 때 호출할 외부 함수
    public void ClearTransferLock()
    {
        this.targetDoorName = "";
        isTransferring = false;
        Debug.Log("🔓 [자물쇠 완전 해제] 플레이어가 안전지대로 나갔으므로 다음 이동이 가능합니다.");
    }

    private void SetPlayerPhysicsState(bool isActive)
    {
        if (NetworkManager.Singleton != null && NetworkManager.Singleton.LocalClient != null && NetworkManager.Singleton.LocalClient.PlayerObject != null)
        {
            GameObject player = NetworkManager.Singleton.LocalClient.PlayerObject.gameObject;
            Rigidbody2D rb = player.GetComponent<Rigidbody2D>();
            Collider2D col = player.GetComponent<Collider2D>();

            if (rb != null)
            {
                if (!isActive)
                {
                    rb.velocity = Vector2.zero;
                    rb.angularVelocity = 0f;
                    rb.bodyType = RigidbodyType2D.Kinematic;
                }
                else
                {
                    rb.bodyType = RigidbodyType2D.Dynamic;
                    rb.velocity = Vector2.zero;
                }
            }

            if (col != null) col.enabled = isActive;
        }
    }
}