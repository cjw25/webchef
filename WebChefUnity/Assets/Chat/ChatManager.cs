using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using TMPro;
using UnityEngine.SceneManagement;
using Unity.Netcode; // ★ 유니티 넷코드 라이브러리 추가

// ★ 멀티플레이어 통신(RPC)을 위해 NetworkBehaviour를 상속받습니다.
public class ChatManager : NetworkBehaviour
{
    [Header("전체 채팅창 UI")]
    public TMP_InputField chatInput;
    public TMP_Text chatWindow;

    [Header("채팅 환경 설정")]
    public float chatDisplayTime = 5f;

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
            Destroy(gameObject);
        }
    }

    public override void OnDestroy()
    {
        // 넷코드 자체의 내장 OnDestroy 시스템을 먼저 한 번 실행해 주는 안전장치
        base.OnDestroy();

        if (Instance == this)
        {
            SceneManager.sceneLoaded -= OnSceneLoaded;
        }
    }

    private void OnSceneLoaded(Scene scene, LoadSceneMode mode)
    {
        if (Instance != this) return;

        // 1. 새 방에 있는 입력창(InputField)을 새로 찾아 완벽하게 연결합니다.
        TMP_InputField newInputField = GameObject.FindObjectOfType<TMP_InputField>(true);
        if (newInputField != null)
        {
            chatInput = newInputField;
            chatInput.onEndEdit.RemoveAllListeners();
            chatInput.onSubmit.RemoveAllListeners();
            chatInput.onSubmit.AddListener(OnChatSubmit);
        }

        // 2. 새 방의 대화창(Text)을 자동으로 새로 고칩니다.
        TMP_Text[] allTexts = GameObject.FindObjectsOfType<TMP_Text>(true);
        foreach (TMP_Text t in allTexts)
        {
            if (t.gameObject.name.Contains("Chat") && t.gameObject.name != "BubbleText")
            {
                chatWindow = t;
                break;
            }
        }

        ResetFocus();
    }

    void Start()
    {
        if (Instance != this) return;
        if (chatWindow != null) chatWindow.text = "";

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

        // 💡 [버그 수정 1] 채팅창이 이미 켜져있는 상태라면 Update의 엔터 처리는 완전히 무시합니다.
        // UI 시스템 내부의 엔터 전송 기능(OnChatSubmit)과 프레임 충돌을 막기 위함입니다.
        if (chatInput != null && chatInput.isFocused)
        {
            if (Input.GetKeyDown(KeyCode.Escape))
            {
                chatInput.text = "";
                ResetFocus();
            }
            return; // 채팅창이 활성화되어 있을 땐 아래의 "채팅창 켜기" 코드로 절대 안 내려갑니다.
        }

        // 💡 [버그 수정 2] 채팅창이 확실하게 꺼져있을 때 '엔터'를 눌러야만 채팅창이 활성화됩니다.
        if (Input.GetKeyDown(KeyCode.Return) || Input.GetKeyDown(KeyCode.KeypadEnter))
        {
            if (chatInput != null)
            {
                StartCoroutine(ActivateChatInputDeferred());
            }
        }
    }

    IEnumerator ActivateChatInputDeferred()
    {
        yield return null;
        if (chatInput != null) chatInput.ActivateInputField();
    }

    void OnChatSubmit(string text)
    {
        // 💡 [버그 수정 3] 채팅창 포커스가 없는 상태(백그라운드 움직임 입력 등)에서 호출되면 즉시 차단합니다.
        if (chatInput == null || !chatInput.isFocused) return;

        if (string.IsNullOrEmpty(text.Trim())) return;
        chatInput.text = "";

        // ★ [멀티플레이 개조] 내가 쓴 글을 내 화면에 바로 띄우지 않고 서버(RPC)로 전송합니다.
        ulong myClientId = NetworkManager.Singleton.LocalClientId;
        SendChatMessageServerRpc(myClientId, text);

        ResetFocus();
    }

    // ★ [멀티플레이 핵심 - ServerRpc] 
    [ServerRpc(RequireOwnership = false)]
    private void SendChatMessageServerRpc(ulong senderClientId, string message)
    {
        ReceiveChatMessageClientRpc(senderClientId, message);
    }

    // ★ [멀티플레이 핵심 - ClientRpc]
    [ClientRpc]
    private void ReceiveChatMessageClientRpc(ulong senderClientId, string message)
    {
        string formattedMessage = $"[유저 {senderClientId}]: {message}";
        chatHistory.Add(formattedMessage);
        UpdateChatWindowText(formattedMessage);

        foreach (PlayerMove player in GameObject.FindObjectsOfType<PlayerMove>())
        {
            NetworkObject netObj = player.GetComponent<NetworkObject>();
            if (netObj != null && netObj.OwnerClientId == senderClientId)
            {
                Canvas[] canvases = player.GetComponentsInChildren<Canvas>(true);
                foreach (Canvas canvas in canvases)
                {
                    if (canvas.name == "SpeechBubbleCanvas")
                    {
                        GameObject bubbleObj = canvas.gameObject;
                        TMP_Text bText = canvas.GetComponentInChildren<TMP_Text>(true);

                        if (bText != null) bText.text = message;
                        if (bubbleObj != null)
                        {
                            bubbleObj.SetActive(true);
                            ChatBubbleTimeout timeoutScript = bubbleObj.GetComponent<ChatBubbleTimeout>();
                            if (timeoutScript == null) timeoutScript = bubbleObj.AddComponent<ChatBubbleTimeout>();
                            timeoutScript.TriggerHide(3f);
                        }
                        break;
                    }
                }
                break;
            }
        }

        StartCoroutine(RemoveChatAfterDelay(formattedMessage, chatDisplayTime));
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
        if (string.IsNullOrEmpty(chatWindow.text)) chatWindow.text = newEntry;
        else chatWindow.text += "\n" + newEntry;
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
}

// ★ 멀티플레이어 말풍선 개별 타이머 소멸 처리를 위한 도우미 클래스
public class ChatBubbleTimeout : MonoBehaviour
{
    private Coroutine currentCoroutine;

    public void TriggerHide(float delay)
    {
        if (currentCoroutine != null) StopCoroutine(currentCoroutine);
        currentCoroutine = StartCoroutine(HideRoutine(delay));
    }

    private IEnumerator HideRoutine(float delay)
    {
        yield return new WaitForSeconds(delay);
        gameObject.SetActive(false);
    }
}