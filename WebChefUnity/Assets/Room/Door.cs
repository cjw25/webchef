using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.SceneManagement;

public class Door : MonoBehaviour
{
    [Header("이동할 씬 이름")]
    public string nextSceneName;

    [Header("다음 방에서 플레이어가 스폰될 문 이름")]
    public string targetDoorName;

    // ★ [20년 차 치트키] 씬 전체에서 딱 하나의 문 전환 프로세스만 작동하도록 보장하는 static 락 장치
    private static bool isLockedGlobal = false;

    private void Start()
    {
        // 씬이 새로 로드되면 강제로 0.2초 동안 이 문이 재발동되지 않도록 락을 걸어둡니다.
        // 새 방에 도착하자마자 문이 또 밟히는 버그를 원천 차단합니다.
        StartCoroutine(SpawnSafetyRoutine());
    }

    private IEnumerator SpawnSafetyRoutine()
    {
        isLockedGlobal = true;
        yield return new WaitForSecondsRealtime(0.2f); // 도착 후 0.2초간 문 무력화
        isLockedGlobal = false;
    }

    private void OnTriggerEnter2D(Collider2D collision)
    {
        if (collision.CompareTag("Player"))
        {
            // 전역 락이 걸려있다면(이미 이동 중이거나 방금 막 스폰되었다면) 무조건 무시!
            if (isLockedGlobal) return;

            Debug.Log($"[Door] {gameObject.name} 작동 시작 -> {nextSceneName}으로 이동합니다.");

            // 문 작동 즉시 전역 락을 잠가버립니다. (이제 어떤 문도 작동 안 됨)
            isLockedGlobal = true;

            // 목적지 문 이름 저장
            if (RoomManager.Instance != null)
            {
                RoomManager.Instance.targetDoorName = targetDoorName;
            }

            // 플레이어의 물리와 속도를 완벽히 정지시켜 안전하게 넘깁니다.
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

            // 즉시 다음 씬 로드
            SceneManager.LoadScene(nextSceneName);
        }
    }
}