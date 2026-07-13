using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using TMPro; // TextMeshPro를 사용할 경우 필수

public class DialogueManager : MonoBehaviour
{
    public static DialogueManager Instance { get; private set; }

    [Header("UI 연결")]
    public GameObject dialogueGroup; // 2단계에서 만든 DialogueBox (통째로 껐다 켤 오브젝트)
    public TextMeshProUGUI nameText;
    public TextMeshProUGUI dialogueText;

    private Queue<NPC_Dialogue.DialogueData> dialogueQueue; // 대사를 담아둘 큐
    private bool isDialogueActive = false;

    private void Awake()
    {
        if (Instance != null && Instance != this)
        {
            Destroy(gameObject);
            return;
        }
        Instance = this;
        dialogueQueue = new Queue<NPC_Dialogue.DialogueData>();
    }

    private void Start()
    {
        EndDialogue(); // 시작할 때는 대사창을 꺼둡니다.
    }

    private void Update()
    {
        // 대사창이 켜져 있는 상태에서 마우스 좌클릭이나 Enter를 누르면 다음 대사로 넘어갑니다.
        if (isDialogueActive && (Input.GetMouseButtonDown(0) || Input.GetKeyDown(KeyCode.Return)))
        {
            DisplayNextSentence();
        }
    }

    public void StartDialogue(NPC_Dialogue.DialogueData[] dialogues)
    {
        isDialogueActive = true;
        dialogueGroup.SetActive(true);

        dialogueQueue.Clear();

        // 큐에 대사들을 순서대로 집어넣습니다.
        foreach (var dialogue in dialogues)
        {
            dialogueQueue.Enqueue(dialogue);
        }

        DisplayNextSentence();
    }

    public void DisplayNextSentence()
    {
        // 더 이상 보여줄 대사가 없다면 대화를 종료합니다.
        if (dialogueQueue.Count == 0)
        {
            EndDialogue();
            return;
        }

        // 큐에서 대사를 하나 꺼내와서 UI에 텍스트를 뿌려줍니다.
        NPC_Dialogue.DialogueData currentDialogue = dialogueQueue.Dequeue();
        nameText.text = currentDialogue.speakerName;
        dialogueText.text = currentDialogue.sentence;
    }

    public void EndDialogue()
    {
        isDialogueActive = false;
        dialogueGroup.SetActive(false);
    }
}