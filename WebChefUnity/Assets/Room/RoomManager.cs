using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using TMPro;

public class RoomManager : MonoBehaviour
{
    [Header("방 레이어 등록")]
    public GameObject room1;
    public GameObject room2;

    [Header("캐릭터 스폰 위치")]
    public Transform playerTransform;
    public Vector2 room1SpawnPoint = new Vector2(0, 0);
    public Vector2 room2SpawnPoint = new Vector2(0, 0);

    void Start()
    {
        ChangeRoom(1);
    }

    void Update()
    {
        // [핵심 수정] 만약 현재 플레이어가 채팅창에 타이핑 중이라면 숫자키 입력을 무시합니다!
        if (UnityEngine.EventSystems.EventSystem.current != null &&
            UnityEngine.EventSystems.EventSystem.current.currentSelectedGameObject != null)
        {
            GameObject currentSelection = UnityEngine.EventSystems.EventSystem.current.currentSelectedGameObject;
            if (currentSelection.GetComponent<TMP_InputField>() != null)
            {
                return; // 채팅 중일 땐 아래 방 변경 코드 실행 안 함
            }
        }

        // 숫자키 1, 2번으로 방 변경
        if (Input.GetKeyDown(KeyCode.Alpha1)) ChangeRoom(1);
        if (Input.GetKeyDown(KeyCode.Alpha2)) ChangeRoom(2);
    }

    public void ChangeRoom(int roomNumber)
    {
        if (roomNumber == 1)
        {
            room1.SetActive(true);
            room2.SetActive(false);
            if (playerTransform != null) playerTransform.position = room1SpawnPoint;
        }
        else if (roomNumber == 2)
        {
            room1.SetActive(false);
            room2.SetActive(true);
            if (playerTransform != null) playerTransform.position = room2SpawnPoint;
        }
    }
}
