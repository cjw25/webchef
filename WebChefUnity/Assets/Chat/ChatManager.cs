using System;
using System.Collections;
using System.Collections.Generic;
using System.Globalization;
using TMPro;
using Unity.Netcode;
using UnityEngine;
using UnityEngine.SceneManagement;

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

    // ⭐️ [방법 1 핵심 적용] NGO가 이 오브젝트를 안전하게 스폰 완료했을 때 호출됩니다.
    public override void OnNetworkSpawn()
    {
        base.OnNetworkSpawn();

        // 켜진 상태로 프리팹이 스폰되었으므로, 네트워크 연결 확인이 끝난 지금 바로 안전하게 꺼줍니다.
        // 이 스크립트가 붙은 오브젝트 자체 혹은 필요한 자식 오브젝트를 초기 비활성화합니다.
        HideChatUIOnSpawn();
    }

    private void HideChatUIOnSpawn()
    {
        // 씬 시작 시 말풍선용 Canvas가 강제로 켜져서 방해되지 않도록 즉시 찾아 꺼주는 로직
        foreach (PlayerMove player in GameObject.FindObjectsOfType<PlayerMove>())
        {
            Canvas[] canvases = player.GetComponentsInChildren<Canvas>(true);
            foreach (Canvas canvas in canvases)
            {
                if (canvas.name == "SpeechBubbleCanvas")
                {
                    canvas.gameObject.SetActive(false); // 스폰 확인 후 바로 끄기
                }
            }
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

        // 씬이 새로 로드되었을 때도 혹시 켜져 있을지 모를 말풍선을 다시 숨깁니다.
        HideChatUIOnSpawn();
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
                        // 채팅이 수신되었을 때만 필요한 말풍선 창을 dynamic하게 활성화합니다.
                        canvas.gameObject.SetActive(true);

                        TMP_Text bText = canvas.GetComponentInChildren<TMP_Text>(true);

                        if (bText != null)
                        {
                            bText.text = message;
                            bText.gameObject.SetActive(true);
                            if (bText.transform.parent != null && bText.transform.parent != canvas.transform)
                            {
                                bText.transform.parent.gameObject.SetActive(true);
                            }

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