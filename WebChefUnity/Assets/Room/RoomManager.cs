using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.SceneManagement;

public class RoomManager : MonoBehaviour
{
    private static RoomManager _instance;

    public static RoomManager Instance
    {
        get
        {
            if (_instance == null)
            {
                // 하이어라키에 혹시 이미 있는지 한 번 더 체크
                _instance = FindObjectOfType<RoomManager>();

                if (_instance == null)
                {
                    GameObject go = new GameObject("RoomManager");
                    _instance = go.AddComponent<RoomManager>();
                    DontDestroyOnLoad(go);
                }
            }
            return _instance;
        }
    }

    [Header("이동 데이터")]
    public string targetDoorName;

    private void Awake()
    {
        // 최상위 부모가 없는 상태에서만 DontDestroyOnLoad가 작동하므로 부모 관계를 끊어줍니다.
        transform.SetParent(null);

        if (_instance == null)
        {
            _instance = this;
            DontDestroyOnLoad(gameObject);
        }
        else if (_instance != this)
        {
            Destroy(gameObject);
        }
    }

    // 유니티 공식 씬 로드 완료 이벤트 등록
    private void OnEnable()
    {
        SceneManager.sceneLoaded += OnSceneLoaded;
    }

    private void OnDisable()
    {
        SceneManager.sceneLoaded -= OnSceneLoaded;
    }

    // 씬 로드가 완전히 끝나고 화면이 전환된 '직후'에 실행되는 안전지대 함수
    private void OnSceneLoaded(Scene scene, LoadSceneMode mode)
    {
        if (string.IsNullOrEmpty(targetDoorName)) return;

        // 다음 방에 배치된 타겟 문 오브젝트를 찾습니다.
        GameObject targetDoor = GameObject.Find(targetDoorName);

        if (targetDoor != null)
        {
            // 방에 존재하고 있는 플레이어를 태그나 컴포넌트로 정확히 찾아옵니다.
            GameObject player = GameObject.FindWithTag("Player");
            if (player == null) player = FindObjectOfType<PlayerMove>()?.gameObject;

            if (player != null)
            {
                // ★ [파고듦 및 백턴 원천 차단 핵심 연산]
                // 1. 플레이어의 물리 엔진을 순간적으로 잠재웁니다.
                Rigidbody2D playerRb = player.GetComponent<Rigidbody2D>();
                Collider2D playerCol = player.GetComponent<Collider2D>();

                if (playerRb != null) { playerRb.velocity = Vector2.zero; playerRb.simulated = false; }
                if (playerCol != null) playerCol.enabled = false;

                // 2. 매니저(나 자신)가 아니라 '플레이어'의 위치를 문의 살짝 아래 안전 구역으로 강제 이동시킵니다!
                Vector3 spawnPosition = targetDoor.transform.position + (Vector3.down * 1.5f);
                player.transform.position = spawnPosition;

                // 3. 물리적 잔상 버그가 세척되도록 1프레임 뒤에 콜라이더를 켜주는 코루틴 실행
                StartCoroutine(SafeActivationRoutine(playerRb, playerCol));
            }
        }
    }

    private IEnumerator SafeActivationRoutine(Rigidbody2D rb, Collider2D col)
    {
        // 새로운 씬의 물리 좌표가 완벽하게 동기화될 때까지 정밀 대기
        yield return new WaitForEndOfFrame();

        // 이제 문 충돌 영역을 완벽하게 벗어났으므로 안전하게 플레이어를 깨웁니다.
        if (rb != null) rb.simulated = true;
        if (col != null) col.enabled = true;

        // 이동이 완전히 끝났으므로 목적지 데이터 초기화
        targetDoorName = "";
    }

    public void ChangeRoom(string sceneName, string doorName)
    {
        targetDoorName = doorName;
        SceneManager.LoadScene(sceneName);
    }
}