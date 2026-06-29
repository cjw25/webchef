using UnityEngine;
using Unity.Netcode;

public class NetworkManagerDuplicatorDestroyer : MonoBehaviour
{
    private void Awake()
    {
        // 하이어라키에 이미 NetworkManager를 가진 오브젝트가 2개 이상 존재한다면
        NetworkManager[] managers = FindObjectsOfType<NetworkManager>();

        if (managers.Length > 1)
        {
            // 만약 내가 방금 생성된 가짜(중복된 녀석)라면 나 자신을 즉시 파괴한다!
            if (NetworkManager.Singleton != null && NetworkManager.Singleton.gameObject != this.gameObject)
            {
                Debug.Log($"💥 [중복 제거] 중복된 {gameObject.name}를 파괴하여 증식을 막았습니다.");
                Destroy(gameObject);
            }
        }
    }
}