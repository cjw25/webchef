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

    private static bool isLockedGlobal = false;

    private void Start()
    {
        isLockedGlobal = false;
    }

    private void OnTriggerEnter2D(Collider2D collision)
    {
        if (collision.CompareTag("Player"))
        {
            if (isLockedGlobal) return;
            isLockedGlobal = true;

            Debug.Log($"[Door] {gameObject.name} 강제 셧다운 이송 시작");

            // 목적지 저장
            if (RoomManager.Instance != null)
            {
                RoomManager.Instance.targetDoorName = targetDoorName;
            }

            // ★ [통과 버그 원천 종결 치트키]
            // 플레이어의 이동 스크립트 자체를 아예 비활성화(끄기) 시켜버립니다.
            // 이렇게 하면 유니티가 씬을 바꾸는 동안 W키를 아무리 누르고 있어도 
            // Update가 안 돌기 때문에 단 1픽셀도 움직이지 못하고 완벽히 고정됩니다.
            PlayerMove playerMove = collision.GetComponent<PlayerMove>();
            if (playerMove != null)
            {
                playerMove.enabled = false;
            }

            // 속도도 0으로 밀어버립니다.
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

            // 안전하게 유니티가 정돈할 시간을 아주 미세하게 준 뒤 로드합니다.
            StartCoroutine(LoadSceneRoutine());
        }
    }

    private IEnumerator LoadSceneRoutine()
    {
        yield return new WaitForEndOfFrame();
        SceneManager.LoadScene(nextSceneName);
    }
}