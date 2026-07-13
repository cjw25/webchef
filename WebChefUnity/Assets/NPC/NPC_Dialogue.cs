using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class NPC_Dialogue : MonoBehaviour
{
    // 💡 struct 대신 class로 변경하여 유니티 인스펙터 호환성을 극대화합니다.
    [System.Serializable]
    public class DialogueData
    {
        public string speakerName; // 말하는 사람 이름
        [TextArea(3, 5)]
        public string sentence;    // 대사 내용
    }

    [Header("NPC 이름")]
    public string npcName = "NPC";

    [Header("NPC 대사 리스트")]
    public DialogueData[] dialogues; // 👈 이제 인스펙터에 무조건 노출됩니다!

    private bool isPlayerNearby = false;

    private void Update()
    {
        if (isPlayerNearby && Input.GetKeyDown(KeyCode.E))
        {
            if (DialogueManager.Instance != null)
            {
                DialogueManager.Instance.StartDialogue(dialogues);
            }
        }
    }

    private void OnTriggerEnter2D(Collider2D collision)
    {
        if (collision.CompareTag("Player"))
        {
            var netObj = collision.GetComponent<Unity.Netcode.NetworkObject>();
            if (netObj != null && netObj.IsOwner)
            {
                isPlayerNearby = true;
                Debug.Log("💬 [E]를 눌러 대화하기");
            }
        }
    }

    private void OnTriggerExit2D(Collider2D collision)
    {
        if (collision.CompareTag("Player"))
        {
            var netObj = collision.GetComponent<Unity.Netcode.NetworkObject>();
            if (netObj != null && netObj.IsOwner)
            {
                isPlayerNearby = false;
                if (DialogueManager.Instance != null)
                {
                    DialogueManager.Instance.EndDialogue();
                }
            }
        }
    }
}