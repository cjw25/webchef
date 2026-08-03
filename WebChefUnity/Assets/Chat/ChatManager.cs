using UnityEngine;
using UnityEngine.UI;
using TMPro;
using Unity.Netcode;

public class ChatManager : NetworkBehaviour
{
    public static ChatManager Instance;

    [Header("UI 연결")]
    public InputField chatInput;
    public Transform contentTransform;
    public GameObject chatMessagePrefab;

    private void Awake()
    {
        Instance = this;
    }

    private void Start()
    {
        if (chatInput != null)
        {
            chatInput.onEndEdit.AddListener(OnChatSubmit);
        }
    }

    public void OnChatSubmit(string text)
    {
        Debug.Log(text);

        if (string.IsNullOrWhiteSpace(text))
        {
            if (chatInput != null) chatInput.text = "";
            return;
        }

        SendChatMessageServerRpc(NetworkManager.Singleton.LocalClientId, text);

        if (chatInput != null)
        {
            chatInput.text = "";
            chatInput.DeactivateInputField();
        }
    }

    [ServerRpc(RequireOwnership = false)]
    private void SendChatMessageServerRpc(ulong senderId, string message)
    {
        ReceiveChatMessageClientRpc(senderId, message);
    }

    [ClientRpc]
    private void ReceiveChatMessageClientRpc(ulong senderId, string message)
    {
        Debug.Log($"[네트워크] 수신됨 - 유저: {senderId}, 내용: {message}");

        if (contentTransform != null && chatMessagePrefab != null)
        {
            GameObject newMsg = Instantiate(chatMessagePrefab, contentTransform);

            RectTransform rect = newMsg.GetComponent<RectTransform>();
            rect.localScale = Vector3.one;
            rect.localPosition = Vector3.zero;

            newMsg.GetComponent<TMP_Text>().text = $"[유저 {senderId}]: {message}";
        }

        if (NetworkManager.Singleton.ConnectedClients.TryGetValue(senderId, out var client))
        {
            if (client.PlayerObject != null && client.PlayerObject.TryGetComponent<PlayerMove>(out var playerMove))
            {
                playerMove.DisplaySpeechBubble(message);
            }
        }
    }

    public bool IsTyping() => chatInput != null && chatInput.isFocused;
}