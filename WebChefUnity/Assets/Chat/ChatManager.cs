using UnityEngine;
using TMPro;
using Unity.Netcode;

public class ChatManager : NetworkBehaviour
{
    public static ChatManager Instance;

    [Header("UI 연결")]
    public TMP_InputField chatInput;
    public Transform contentTransform;
    public GameObject chatMessagePrefab;

    private void Awake() => Instance = this;

    // 엔터키를 쳤을 때 유니티 UI가 호출할 함수
    public void OnChatSubmit(string text)
    {
        Debug.Log(text);
        // 입력값이 없거나 공백만 있는 경우 무시
        if (string.IsNullOrWhiteSpace(text))
        {
            chatInput.text = "";
            return;
        }

        // 서버로 전송
        SendChatMessageServerRpc(NetworkManager.Singleton.LocalClientId, text);

        // 입력창 초기화
        chatInput.text = "";
        chatInput.DeactivateInputField();
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

        // 1. 전체 채팅창 UI 업데이트
        if (contentTransform != null && chatMessagePrefab != null)
        {
            GameObject newMsg = Instantiate(chatMessagePrefab, contentTransform);

            // --- 위치 강제 보정 로직 추가 ---
            RectTransform rect = newMsg.GetComponent<RectTransform>();
            rect.localScale = Vector3.one;
            rect.localPosition = Vector3.zero; // 부모 Content의 좌측 상단으로 강제 이동
                                               // ------------------------------

            newMsg.GetComponent<TMP_Text>().text = $"[유저 {senderId}]: {message}";
        }

        // 2. 말풍선 표시
        if (NetworkManager.Singleton.ConnectedClients.TryGetValue(senderId, out var client))
        {
            if (client.PlayerObject != null && client.PlayerObject.TryGetComponent<PlayerMove>(out var playerMove))
            {
                // PlayerMove 내부의 로직 호출
                playerMove.DisplaySpeechBubble(message);
            }
            else
            {
                Debug.LogError($"[에러] {senderId}번 유저의 PlayerObject가 null이거나 PlayerMove 컴포넌트가 없습니다.");
            }
        }
    }

    public bool IsTyping() => chatInput != null && chatInput.isFocused;
}