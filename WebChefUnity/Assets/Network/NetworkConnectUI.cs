using System.Collections.Generic;
using Unity.Services.Lobbies.Models;
using UnityEngine;
using UnityEngine.UI;
using TMPro;

public class NetworkConnectUI : MonoBehaviour
{
    [SerializeField] private Button createButton;
    [SerializeField] private Button joinButton;
    [SerializeField] private TMP_InputField joinCodeInputField;
    [SerializeField] private TMP_Text codeText;

    private void Start()
    {
        // 💡 예외 대응: 매니저가 완전히 초기화될 때까지 버튼 비활성화
        if (RelayManager.Instance != null && !RelayManager.Instance.IsAuthInitialized)
        {
            createButton.interactable = false;
            joinButton.interactable = false;
            codeText.text = "네트워크 서비스 초기화 중...";
            RelayManager.Instance.OnAuthenticationComplete += EnableUIButtons;
        }
        else
        {
            EnableUIButtons();
        }

        // 1. 방 만들기 버튼 이벤트
        createButton.onClick.AddListener(async () => {
            string roomName = joinCodeInputField.text.Trim();
            if (string.IsNullOrEmpty(roomName)) roomName = "즐거운 멀티방";

            codeText.text = "로비 및 릴레이 생성 중...";
            SetUIInteractable(false);

            bool success = await RelayManager.Instance.CreateLobby(roomName, 4);

            if (success)
            {
                codeText.text = "방 생성 성공! 게임 시작.";
                gameObject.SetActive(false);
            }
            else
            {
                codeText.text = "<color=red>방 생성 실패. 다시 시도하세요.</color>";
                SetUIInteractable(true); // 에러 발생 시 UI 다시 조작 가능하게 롤백
            }
        });

        // 2. 빠른 입장 버튼 이벤트
        joinButton.onClick.AddListener(async () => {
            codeText.text = "열려있는 방 찾는 중...";
            SetUIInteractable(false);

            List<Lobby> lobbies = await RelayManager.Instance.QueryLobbies();

            if (lobbies != null && lobbies.Count > 0)
            {
                codeText.text = $"방 발견! [{lobbies[0].Name}] 입장 중...";

                bool success = await RelayManager.Instance.JoinLobby(lobbies[0]);

                if (success)
                {
                    gameObject.SetActive(false);
                }
                else
                {
                    codeText.text = "<color=red>방 입장에 실패했습니다.</color>";
                    SetUIInteractable(true); // 롤백
                }
            }
            else
            {
                codeText.text = "현재 열려있는 방이 없습니다.";
                SetUIInteractable(true); // 롤백
            }
        });
    }

    private void EnableUIButtons()
    {
        if (RelayManager.Instance != null)
            RelayManager.Instance.OnAuthenticationComplete -= EnableUIButtons; // 메모리 누수 방지 해제

        createButton.interactable = true;
        joinButton.interactable = true;
        codeText.text = "접속 대기 중...";
    }

    private void SetUIInteractable(bool state)
    {
        createButton.interactable = state;
        joinButton.interactable = state;
        joinCodeInputField.interactable = state;
    }
}