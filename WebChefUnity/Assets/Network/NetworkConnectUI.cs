using System.Collections.Generic;
using Unity.Services.Lobbies.Models;
using UnityEngine;
using UnityEngine.UI;
using TMPro;

public class NetworkConnectUI : MonoBehaviour
{
    [SerializeField] private Button createButton;
    [SerializeField] private Button joinButton; // 👈 이제 이 버튼은 '방 목록 새로고침' 또는 '빠른 입장' 등으로 쓸 수 있습니다.
    [SerializeField] private TMP_InputField joinCodeInputField; // 👈 방 이름을 입력하는 칸으로 활용합니다.
    [SerializeField] private TMP_Text codeText; // 👈 상태 메시지를 출력하는 텍스트로 활용합니다.

    private void Start()
    {
        // 1. 방 만들기 버튼 이벤트
        createButton.onClick.AddListener(async () => {
            string roomName = joinCodeInputField.text.Trim();
            if (string.IsNullOrEmpty(roomName)) roomName = "즐거운 멀티방";

            codeText.text = "로비 및 릴레이 생성 중...";
            createButton.interactable = false;

            // ⭐️ 핵심 수정: CreateRelay 대신 로비와 연동되는 CreateLobby를 호출합니다.
            await RelayManager.Instance.CreateLobby(roomName, 4);

            codeText.text = "방 생성 성공! 게임 시작.";
            gameObject.SetActive(false); // 접속 성공 시 UI 창 닫기
        });

        // 2. 방 입장 버튼 이벤트 (가장 먼저 개설된 방에 자동으로 찾아 들어가는 '빠른 입장' 로직으로 수정)
        joinButton.onClick.AddListener(async () => {
            codeText.text = "열려있는 방 찾는 중...";
            joinButton.interactable = false;

            // 현재 열려있는 모든 로비 목록을 가져옵니다.
            List<Lobby> lobbies = await RelayManager.Instance.QueryLobbies();

            if (lobbies != null && lobbies.Count > 0)
            {
                codeText.text = $"방 발견! [{lobbies[0].Name}] 입장 중...";

                // ⭐️ 핵심 수정: 가장 첫 번째로 검색된 방에 JoinLobby로 입장합니다.
                await RelayManager.Instance.JoinLobby(lobbies[0]);

                gameObject.SetActive(false);
            }
            else
            {
                codeText.text = "현재 열려있는 방이 없습니다.";
                joinButton.interactable = true;
            }
        });
    }
}