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

        if (Input.GetKeyDown(KeyCode.Return) || Input.GetKeyDown(KeyCode.KeypadEnter))
        {
            if (chatInput != null && !chatInput.isFocused)
            {
                StartCoroutine(ActivateChatInputDeferred());
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

    IEnumerator ActivateChatInputDeferred()
    {
        yield return null;
        if (chatInput != null) chatInput.ActivateInputField();
    }

    void OnChatSubmit(string text)
    {
        if (string.IsNullOrEmpty(text.Trim())) return;
        chatInput.text = "";

        // ★ [멀티플레이 개조] 내가 쓴 글을 내 화면에 바로 띄우지 않고 서버(RPC)로 전송합니다.
        // 유저 이름 대신 넷코드 고유의 ClientId를 함께 전달합니다.
        ulong myClientId = NetworkManager.Singleton.LocalClientId;
        SendChatMessageServerRpc(myClientId, text);

        ResetFocus();
    }

    // ★ [멀티플레이 핵심 - ServerRpc] 
    // 클라이언트가 엔터를 치면 이 함수를 통해 서버 컴퓨터로 채팅 데이터가 먼저 수집됩니다.
    [ServerRpc(RequireOwnership = false)]
    private void SendChatMessageServerRpc(ulong senderClientId, string message)
    {
        // 서버가 전 세계 20명의 유저(Client)들에게 이 채팅 내용을 똑같이 뿌려줍니다.
        ReceiveChatMessageClientRpc(senderClientId, message);
    }

    // ★ [멀티플레이 핵심 - ClientRpc]
    // 서버가 신호를 주면 방에 있는 20명 플레이어들의 컴퓨터에서 동시에 이 함수가 실행됩니다.
    [ClientRpc]
    private void ReceiveChatMessageClientRpc(ulong senderClientId, string message)
    {
        // 1. 전체 채팅창 UI 갱신 연산
        string formattedMessage = $"[유저 {senderClientId}]: {message}";
        chatHistory.Add(formattedMessage);
        UpdateChatWindowText(formattedMessage);

        // 2. ★ [가장 중요] 방 안의 모든 플레이어 중 "진짜 이 채팅을 친 주인"을 정확하게 조준합니다.
        foreach (PlayerMove player in GameObject.FindObjectsOfType<PlayerMove>())
        {
            NetworkObject netObj = player.GetComponent<NetworkObject>();
            // 넷코드 ID 검증을 거쳐 일치하는 타겟을 정밀 타격합니다.
            if (netObj != null && netObj.OwnerClientId == senderClientId)
            {
                // 해당 플레이어 머리 위에 붙어있는 말풍선 캔버스를 찾습니다.
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
                            // 이전 타이머 코루틴이 돌고 있다면 정지 연산 처리 후 새로 구동
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