using System; // ★ using 지시문은 함수 내부가 아니라 여기에 있어야 합니다.
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using TMPro;
using UnityEngine.SceneManagement;
using Unity.Netcode;

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
        base.OnDestroy();

        if (Instance == this)
        {
            SceneManager.sceneLoaded -= OnSceneLoaded;
        }
    }

    private void OnSceneLoaded(Scene scene, LoadSceneMode mode)
    {
        if (Instance != this) return;

        TMP_InputField newInputField = GameObject.FindObjectOfType<TMP_InputField>(true);
        if (newInputField != null)
        {
            chatInput = newInputField;
            chatInput.onEndEdit.RemoveAllListeners();
            chatInput.onSubmit.RemoveAllListeners();
            chatInput.onSubmit.AddListener(OnChatSubmit);
        }

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

        if (chatInput != null && chatInput.isFocused)
        {
            if (Input.GetKeyDown(KeyCode.Escape))
            {
                chatInput.text = "";
                ResetFocus();
            }
            return;
        }

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
        // ★ 함수 내부에 있던 `using System;` 구문을 완전히 삭제했습니다.
        yield return null;
        if (chatInput != null) chatInput.ActivateInputField();
    }

    void OnChatSubmit(string text)
    {
        if (chatInput == null || !chatInput.isFocused) return;

        if (string.IsNullOrEmpty(text.Trim())) return;
        chatInput.text = "";

        ulong myClientId = NetworkManager.Singleton.LocalClientId;
        SendChatMessageServerRpc(myClientId, text);

        ResetFocus();
    }

    [ServerRpc(RequireOwnership = false)]
    private void SendChatMessageServerRpc(ulong senderClientId, string message)
    {
        ReceiveChatMessageClientRpc(senderClientId, message);
    }

    [ClientRpc]
    private void ReceiveChatMessageClientRpc(ulong senderClientId, string message)
    {
        string formattedMessage = $"[유저 {senderClientId}]: {message}";
        chatHistory.Add(formattedMessage);
        UpdateChatWindowText(formattedMessage);

        // 맵에 있는 모든 플레이어를 순회하며 채팅을 보낸 대상을 찾습니다.
        foreach (PlayerMove player in GameObject.FindObjectsOfType<PlayerMove>())
        {
            NetworkObject netObj = player.GetComponent<NetworkObject>();
            if (netObj != null && netObj.OwnerClientId == senderClientId)
            {
                // 플레이어의 자식 캔버스를 검색합니다.
                Canvas[] canvases = player.GetComponentsInChildren<Canvas>(true);
                foreach (Canvas canvas in canvases)
                {
                    if (canvas.name == "SpeechBubbleCanvas")
                    {
                        // 1. 최상단 부모인 캔버스를 활성화합니다.
                        canvas.gameObject.SetActive(true);

                        // 2. 플레이어 머리 위에 생성한 말풍선 텍스트(BubbleText)를 직접 찾아 할당합니다.
                        // 이미지 상 구조상 자식에 바로 BubbleText가 있을 때 안전하게 들고 오기 위함입니다.
                        TMP_Text bText = canvas.GetComponentInChildren<TMP_Text>(true);

                        if (bText != null)
                        {
                            bText.text = message;

                            // 3. 글자가 든 오브젝트나 부모 패널을 활성화합니다.
                            bText.gameObject.SetActive(true);
                            if (bText.transform.parent != null && bText.transform.parent != canvas.transform)
                            {
                                bText.transform.parent.gameObject.SetActive(true);
                            }

                            // 4. 타이머 컴포넌트를 동작시켜 3초 뒤 꺼지게 만듭니다.
                            GameObject targetTimerObj = bText.transform.parent != null ? bText.transform.parent.gameObject : bText.gameObject;

                            ChatBubbleTimeout timeoutScript = targetTimerObj.GetComponent<ChatBubbleTimeout>();
                            if (timeoutScript == null) timeoutScript = targetTimerObj.AddComponent<ChatBubbleTimeout>();

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