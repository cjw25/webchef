using UnityEngine;
using TMPro;
using Unity.Netcode;

public class ChatManager : NetworkBehaviour
{
    public static ChatManager Instance;
    public TMP_InputField chatInput;
    public TMP_Text chatWindow; // 화면 전체 채팅 로그용

    private void Awake() => Instance = this;

    // 채팅 입력창에서 엔터를 쳤을 때 호출
    public void OnChatSubmit(string text)
    {
        // 1. 내용이 비어있으면 바로 리턴
        if (string.IsNullOrWhiteSpace(text))
        {
            chatInput.text = ""; // 입력창 초기화
            return;
        }

        // 2. 입력된 텍스트를 변수에 먼저 저장
        string messageToSend = text;

        // 3. 서버로 메시지 전송
        SendChatMessageServerRpc(NetworkManager.Singleton.LocalClientId, messageToSend);

        // 4. 전송 후 입력창 비우기 및 포커스 해제
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
        Debug.Log($"[네트워크] 메시지 수신됨: {message}, 보낸 사람: {senderId}");

        // 1. 전체 채팅 로그창 업데이트
        if (chatWindow != null)
        {
            chatWindow.text += $"\n[유저 {senderId}]: {message}";
        }

        // 2. 네트워크상의 Client 목록에서 senderId를 찾아 정확하게 참조
        if (NetworkManager.Singleton.ConnectedClients.TryGetValue(senderId, out var client))
        {
            // 해당 클라이언트의 PlayerObject를 가져옵니다.
            var playerObject = client.PlayerObject;

            if (playerObject != null)
            {
                var playerMove = playerObject.GetComponent<PlayerMove>();
                if (playerMove != null)
                {
                    Debug.Log($"[성공] {senderId}번 유저의 말풍선 함수를 직접 호출합니다.");
                    playerMove.DisplaySpeechBubble(message);
                }
                else
                {
                    Debug.LogError($"[에러] {senderId}번 유저의 PlayerObject에 PlayerMove 스크립트가 없습니다!");
                }
            }
            else
            {
                Debug.LogError($"[에러] {senderId}번 유저의 PlayerObject가 null입니다!");
            }
        }
        else
        {
            Debug.LogError($"[에러] 네트워크 상에서 {senderId}번 클라이언트를 찾을 수 없습니다.");
        }
    }
    public bool IsTyping()
    {
        // chatInput이 있고, 현재 키보드 입력을 받고 있는 상태인지 확인
        if (chatInput == null) return false;
        return chatInput.isFocused;
    }
    void Update()
    {
        if (Input.GetKeyDown(KeyCode.Return))
        {
            Debug.Log("엔터 키 입력 감지됨!");

            if (chatInput != null)
            {
                Debug.Log($"입력창 텍스트: {chatInput.text}"); // 텍스트가 비어있는지 확인

                // 여기서 직접 함수를 호출해 봅니다.
                OnChatSubmit(chatInput.text);
            }
            else
            {
                Debug.LogError("ChatInput 컴포넌트가 연결되지 않았습니다!");
            }

           
        }
    }
}