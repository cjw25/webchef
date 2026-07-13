using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using Unity.Netcode;

public class NPCSpawner : NetworkBehaviour
{
    [Header("소환할 NPC 프리팹")]
    public GameObject npcPrefab;

    [Header("NPC가 태어날 위치")]
    public Transform spawnPoint;

    // 게임 시작 시 혹은 특정 버튼을 눌렀을 때 호출할 함수
    public void SpawnNPC()
    {
        // 💡 오직 서버(호스트)일 때만 생성 로직을 실행하도록 방어합니다.
        if (!IsServer) return;

        if (npcPrefab != null)
        {
            // 1. 서버 호스트가 물리적인 오브젝트를 먼저 생성합니다.
            GameObject npcInstance = Instantiate(npcPrefab, spawnPoint.position, Quaternion.identity);

            // 2. NetworkObject 컴포넌트를 가져옵니다.
            NetworkObject netObj = npcInstance.GetComponent<NetworkObject>();

            if (netObj != null)
            {
                // 3. ★ 핵심: Spawn()을 호출하는 순간, 접속해 있는 모든 클라이언트 화면에 이 NPC가 복제되어 나타납니다.
                netObj.Spawn();
                Debug.Log("🤖 네트워크 NPC가 서버 권한으로 모든 화면에 정상 소환되었습니다.");
            }
        }
    }
}