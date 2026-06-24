using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class CameraFollow : MonoBehaviour
{
    private Transform targetPlayer; // 추적할 플레이어의 위치
    public float xySpeed = 5f;      // 카메라가 플레이어를 따라가는 속도

    void Update()
    {
        // ★ 핵심: 플레이어 타겟이 비어있다면, 현재 방에 존재하는 진짜 플레이어를 실시간으로 찾아냅니다!
        if (targetPlayer == null)
        {
            GameObject playerObj = GameObject.FindGameObjectWithTag("Player");
            if (playerObj != null)
            {
                targetPlayer = playerObj.transform;
            }
        }

        // 플레이어를 찾았다면 카메라 위치를 플레이어 좌표로 부드럽게 이동시킵니다.
        if (targetPlayer != null)
        {
            Vector3 targetPosition = new Vector3(targetPlayer.position.x, targetPlayer.position.y, transform.position.z);
            // 부드러운 이동 처리
            transform.position = Vector3.Lerp(transform.position, targetPosition, xySpeed * Time.deltaTime);
        }
    }
}
