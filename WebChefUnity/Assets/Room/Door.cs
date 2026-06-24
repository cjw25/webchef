using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Door : MonoBehaviour
{
    public string nextSceneName; // 이동할 다음 방 씬 이름
    public string targetDoorName; // 다음 방에 도착했을 때 캐릭터가 서 있을 문의 이름

    private void OnTriggerEnter2D(Collider2D collision)
    {
        // 플레이어가 문에 들어왔다면
        if (collision.CompareTag("Player"))
        {
            // RoomManager에게 다음 방과 문의 이름을 넘겨주며 씬 전환 요청
            RoomManager.Instance.ChangeRoom(nextSceneName, targetDoorName);
        }
        if (collision.CompareTag("Player"))
        {
            // 이 줄을 추가해서 콘솔창에 신호가 오는지 확인합니다!
            Debug.Log("플레이어가 문에 부딪혔음!");

            RoomManager.Instance.ChangeRoom(nextSceneName, targetDoorName);
        }
    }
}
