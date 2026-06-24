using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.SceneManagement;

public class RoomManager : MonoBehaviour
{
    void Start()
    {
        if (RoomManager.Instance != null && !string.IsNullOrEmpty(RoomManager.Instance.targetDoorName))
        {
            GameObject targetDoor = GameObject.Find(RoomManager.Instance.targetDoorName);

            if (targetDoor != null)
            {
                transform.position = targetDoor.transform.position;

                // ★ [이 코드가 꼭 들어가야 합니다!] 
                // 순간이동을 완료했으므로, RoomManager에게 이동이 완전히 끝났음을 알려줍니다.
                RoomManager.Instance.targetDoorName = "";
            }
        }
    }
    private static RoomManager _instance;

    public static RoomManager Instance
    {
        get
        {
            // 만약 다른 스크립트가 나를 불렀는데 내가 맵에 없다면?
            if (_instance == null)
            {
                // 스스로 유니티 에디터 안에 오브젝트를 만들고 컴포넌트를 붙여서 자동 탄생시킵니다!
                GameObject go = new GameObject("RoomManager");
                _instance = go.AddComponent<RoomManager>();
                DontDestroyOnLoad(go);
            }
            return _instance;
        }
    }

    public string targetDoorName;

    private void Awake()
    {
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

    public void ChangeRoom(string sceneName, string doorName)
    {
        targetDoorName = doorName;
        SceneManager.LoadScene(sceneName);
    }
}
