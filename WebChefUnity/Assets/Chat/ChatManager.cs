using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using TMPro;
using UnityEngine.SceneManagement;

public class ChatManager : MonoBehaviour
{
    [Header("전체 채팅창 UI")]
    public TMP_InputField chatInput;
    public TMP_Text chatWindow;

    [Header("캐릭터 머리 위 말풍선 UI")]
    public GameObject speechBubbleObject;
    public TMP_Text bubbleText;

    [Header("채팅 환경 설정")]
    public float chatDisplayTime = 5f;

    private Coroutine bubbleCoroutine;
    private List<string> chatHistory = new List<string>();

    public static ChatManager Instance;

    private void Awake()
    {
        if (Instance == null)
        {
            Instance = this;
            DontDestroyOnLoad(gameObject);
            SceneManager.sceneLoaded += OnSceneLoaded;
        }
        else
        {
            // ★ 중복 방역 핵심: 이미 살아남아 넘어온 진짜 매니저가 있다면, 새로 로드된 방의 가짜 매니저는 자폭합니다.
            Destroy(gameObject);
        }
    }

    private void OnDestroy()
    {
        // 싱글톤 본인일 때만 이벤트를 해제하도록 안전장치 추가
        if (Instance == this)
        {
            SceneManager.sceneLoaded -= OnSceneLoaded;
        }
    }

    // 새로운 방에 올 때마다 자동으로 UI와 플레이어를 안전하게 재연결합니다.
    private void OnSceneLoaded(Scene scene, LoadSceneMode mode)
    {
        // 만약 내가 진짜 싱글톤 인스턴스가 아니라면 아래 연결 연산을 수행하지 않고 나갑니다.
        if (Instance != this) return;

        // 1. 방을 이동했으므로 기존 말풍선 UI 상태 리셋
        if (speechBubbleObject != null) speechBubbleObject.SetActive(false);
        if (bubbleText != null) bubbleText.text = "";

        // 2. 새 방에 있는 입력창(InputField)을 하이어라키에서 새로 찾아 완벽하게 연결합니다.
        TMP_InputField newInputField = GameObject.FindObjectOfType<TMP_InputField>(true);
        if (newInputField != null)
        {
            chatInput = newInputField;

            chatInput.onEndEdit.RemoveAllListeners();
            chatInput.onSubmit.RemoveAllListeners();
            chatInput.onSubmit.AddListener(OnChatSubmit);
        }

        // 3. 새 방의 대화창(Text)을 더 안정적으로 추적합니다.
        TMP_Text[] allTexts = GameObject.FindObjectsOfType<TMP_Text>(true);
        foreach (TMP_Text t in allTexts)
        {
            // 오브젝트 이름에 'Chat'이 포함되어 있고, 캐릭터 머리 위 말풍선 텍스트가 아니라면 진짜 대화창입니다.
            if (t.gameObject.name.Contains("Chat") && t.gameObject.name != "BubbleText")
            {
                chatWindow = t;
                break;
            }
        }

        // 4. 새 방으로 넘어온 진짜 플레이어를 추적해 머리 위 말풍선 연결고리를 새로 고칩니다.
        RefreshSpeechBubbleReference();

        // 5. 입력창 포커스 리셋
        ResetFocus();
    }

    void Start()
    {
        if (Instance != this) return;

        if (chatWindow != null) chatWindow.text = "";
        if (speechBubbleObject != null) speechBubbleObject.SetActive(false);

        if (chatInput != null)
        {
            chatInput.onEndEdit.RemoveAllListeners();
            chatInput.onSubmit.RemoveAllListeners();
            chatInput.onSubmit.AddListener(OnChatSubmit);
        }
    }

    void Update()
    {
        if (Instance != this) return;

        // 엔터키 입력 처리
        if (Input.GetKeyDown(KeyCode.Return) || Input.GetKeyDown(KeyCode.KeypadEnter))
        {
            if (chatInput != null && !chatInput.isFocused)
            {
                StartCoroutine(ActivateChatInputDeferred());
            }
        }

        // ESC 입력 처리
        if (Input.GetKeyDown(KeyCode.Escape))
        {
            if (chatInput != null && chatInput.isFocused)
            {
                chatInput.text = "";
                ResetFocus();
            }
        }
    }

    IEnumerator ActivateChatInputDeferred()
    {
        yield return null;
        if (chatInput != null)
        {
            chatInput.ActivateInputField();
        }
    }

    void OnChatSubmit(string text)
    {
        if (string.IsNullOrEmpty(text.Trim())) return;
        chatInput.text = "";
        SendChatMessage(text);
        ResetFocus();
    }

    public void OnReceiveChatMessage(string senderName, string message)
    {
        string formattedMessage = $"[{senderName}]: {message}";
        chatHistory.Add(formattedMessage);
        UpdateChatWindowText(formattedMessage);

        RefreshSpeechBubbleReference();
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
        UpdateChatWindowText(formattedMessage);

        RefreshSpeechBubbleReference();
        if (speechBubbleObject != null)
        {
            ShowSpeechBubble(message);
        }

        StartCoroutine(RemoveChatAfterDelay(formattedMessage, chatDisplayTime));
    }

    void RefreshSpeechBubbleReference()
    {
        GameObject realPlayer = GameObject.FindGameObjectWithTag("Player");
        if (realPlayer != null)
        {
            Canvas[] canvases = realPlayer.GetComponentsInChildren<Canvas>(true);
            foreach (Canvas canvas in canvases)
            {
                if (canvas.name == "SpeechBubbleCanvas")
                {
                    speechBubbleObject = canvas.gameObject;
                    bubbleText = canvas.GetComponentInChildren<TMP_Text>(true);
                    break;
                }
            }
        }
    }

    IEnumerator RemoveChatAfterDelay(string messageToRemove, float delay)
    {
        yield return new WaitForSeconds(delay);
        if (chatHistory.Contains(messageToRemove))
        {
            chatHistory.Remove(messageToRemove);
            if (chatWindow != null)
            {
                chatWindow.text = string.Join("\n", chatHistory);
            }
        }
    }

    void UpdateChatWindowText(string newEntry)
    {
        if (chatWindow == null) return;
        if (string.IsNullOrEmpty(chatWindow.text))
        {
            chatWindow.text = newEntry;
        }
        else
        {
            chatWindow.text += "\n" + newEntry;
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