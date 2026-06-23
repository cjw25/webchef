using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using TMPro;

public class ChatManager : MonoBehaviour
{
    [Header("전체 채팅창 UI")]
    public TMP_InputField chatInput;
    public TMP_Text chatWindow;

    [Header("캐릭터 머리 위 말풍선 UI")]
    public GameObject speechBubbleObject;
    public TMP_Text bubbleText;

    [Header("채팅 환경 설정")]
    public float chatDisplayTime = 5f; // 채팅이 화면에 유지될 시간 (5초)

    private Coroutine bubbleCoroutine;
    // 전체 채팅 내역을 저장해둘 리스트
    private List<string> chatHistory = new List<string>();

    void Start()
    {
        if (chatWindow != null) chatWindow.text = "";
        if (speechBubbleObject != null) speechBubbleObject.SetActive(false);

        // [1. 렉 방지 치트키] 시작하자마자 주요 한글을 미리 뇌에 주입해 둡니다.
        if (chatWindow != null && chatWindow.font != null)
        {
            chatWindow.font.HasCharacters("ㄱ<<<ㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ가나다라마바사아자차카타파하따짜째씁");
        }

        if (chatInput != null)
        {
            // [2. 중복 방지] 기존에 남아있을 수 있는 리스너를 완전히 청소한 뒤 오직 엔터(onSubmit)만 등록합니다.
            chatInput.onEndEdit.RemoveAllListeners();
            chatInput.onSubmit.RemoveAllListeners();
            chatInput.onSubmit.AddListener(OnChatSubmit);
        }
    }

    void OnChatSubmit(string text)
    {
        // 1. 이미 입력창이 비어있거나 공백이면 즉시 무시 (중복 전송 완벽 차단)
        if (string.IsNullOrEmpty(text.Trim())) return;

        // 2. 글을 보내자마자 바로 입력창 내용부터 비워버립니다.
        chatInput.text = "";

        // 3. 진짜 전송 처리는 딱 '한 번'만 진행합니다.
        SendChatMessage(text);

        // 4. 입력창 포커스 해제
        ResetFocus();
    }

    public void OnReceiveChatMessage(string senderName, string message)
    {
        string formattedMessage = $"[{senderName}]: {message}";
        chatHistory.Add(formattedMessage);

        // 실시간 매개변수를 넘겨주어 과부하를 최소화합니다.
        UpdateChatWindowText(formattedMessage);

        if (speechBubbleObject != null)
        {
            ShowSpeechBubble(message);
        }

        StartCoroutine(RemoveChatAfterDelay(formattedMessage, chatDisplayTime));
    }

    void SendChatMessage(string message)
    {
        string formattedMessage = $"[일촌]: {message}";
        chatHistory.Add(formattedMessage);

        // [3. 렉 박멸] 전체 리스트를 조립하지 않고 새 메시지만 즉시 덧붙여 연산량을 줄입니다.
        UpdateChatWindowText(formattedMessage);

        if (speechBubbleObject != null)
        {
            ShowSpeechBubble(message);
        }

        StartCoroutine(RemoveChatAfterDelay(formattedMessage, chatDisplayTime));
    }

    // 5초 뒤에 특정 채팅을 지워주는 타이머 함수
    IEnumerator RemoveChatAfterDelay(string messageToRemove, float delay)
    {
        yield return new WaitForSeconds(delay);

        if (chatHistory.Contains(messageToRemove))
        {
            chatHistory.Remove(messageToRemove);

            // 오래된 글이 지워져서 새로고침할 때만 1회 정렬해 줍니다.
            if (chatWindow != null)
            {
                chatWindow.text = string.Join("\n", chatHistory);
            }
        }
    }

    // 리스트에 저장된 대화 내용을 최적화하여 화면에 그려주는 함수
    void UpdateChatWindowText(string newEntry)
    {
        if (chatWindow == null) return;

        // 텍스트가 아예 비어있으면 첫 줄로 넣고, 있으면 줄바꿈 후 이어붙여 렉을 없앱니다.
        if (string.IsNullOrEmpty(chatWindow.text))
        {
            chatWindow.text = newEntry;
        }
        else
        {
            chatWindow.text += "\n" + newEntry;
        }
    }

    void Update()
    {
        if (Input.GetKeyDown(KeyCode.Return) || Input.GetKeyDown(KeyCode.KeypadEnter))
        {
            if (chatInput != null && !chatInput.isFocused)
            {
                chatInput.ActivateInputField();
            }
        }

        if (Input.GetKeyDown(KeyCode.Escape))
        {
            if (chatInput != null && chatInput.isFocused)
            {
                chatInput.text = "";
                ResetFocus();
            }
        }
    }

    void ResetFocus()
    {
        if (chatInput != null) chatInput.DeactivateInputField();
        if (UnityEngine.EventSystems.EventSystem.current != null)
        {
            UnityEngine.EventSystems.EventSystem.current.SetSelectedGameObject(null);
        }
    }

    public bool IsTyping()
    {
        if (chatInput == null) return false;
        return chatInput.isFocused;
    }

    void ShowSpeechBubble(string message)
    {
        if (bubbleCoroutine != null) StopCoroutine(bubbleCoroutine);
        if (bubbleText != null) bubbleText.text = message;
        if (speechBubbleObject != null) speechBubbleObject.SetActive(true);
        bubbleCoroutine = StartCoroutine(HideBubbleAfterDelay(3f));
    }

    IEnumerator HideBubbleAfterDelay(float delay)
    {
        yield return new WaitForSeconds(delay);
        if (speechBubbleObject != null) speechBubbleObject.SetActive(false);
    }
}