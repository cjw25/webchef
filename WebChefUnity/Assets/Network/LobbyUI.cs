using System.Collections.Generic;
using Unity.Services.Lobbies.Models;
using UnityEngine;
using UnityEngine.UI;
using TMPro;

public class LobbyUI : MonoBehaviour
{
    [Header("Create Room")]
    [SerializeField] private TMP_InputField roomNameInput;
    [SerializeField] private Button createRoomButton;

    [Header("Room List")]
    [SerializeField] private Button refreshButton;
    [SerializeField] private Transform roomListContainer; // Scroll View -> Viewport -> Content 오브젝트 지정
    [SerializeField] private GameObject roomEntryPrefab;  // 방 목록 한 칸을 나타낼 버튼 프리팹

    private void Start()
    {
        // 버튼 이벤트 바인딩
        createRoomButton.onClick.AddListener(OnCreateRoomClicked);
        refreshButton.onClick.AddListener(RefreshRoomList);

        // 씬이 시작될 때 자동으로 방 목록을 한 번 불러옵니다.
        RefreshRoomList();
    }

    /// <summary>
    /// 방 만들기 버튼 클릭 시 호출
    /// </summary>
    private async void OnCreateRoomClicked()
    {
        string roomName = roomNameInput.text.Trim();

        // 방 이름을 입력하지 않았다면 기본 이름으로 설정
        if (string.IsNullOrEmpty(roomName)) roomName = "즐거운 멀티방";

        createRoomButton.interactable = false;
        refreshButton.interactable = false;

        // ⭐️ RelayManager 싱글톤을 호출하도록 완전히 매칭했습니다.
        await RelayManager.Instance.CreateLobby(roomName, 4);

        // 방 생성이 완료되어 NGO가 호스트로 시작되면 UI 창을 닫습니다.
        gameObject.SetActive(false);
    }

    /// <summary>
    /// 새로고침 버튼 클릭 시 호출
    /// </summary>
    private async void RefreshRoomList()
    {
        refreshButton.interactable = false;

        // 기존 UI 목록에 남아있던 방 버튼들을 깨끗하게 지웁니다.
        foreach (Transform child in roomListContainer)
        {
            Destroy(child.gameObject);
        }

        // ⭐️ RelayManager를 통해 현재 열려있는 로비 목록을 서버에서 가져옵니다.
        List<Lobby> lobbies = await RelayManager.Instance.QueryLobbies();

        if (lobbies == null)
        {
            refreshButton.interactable = true;
            return;
        }

        // 검색된 방 개수만큼 UI 화면에 생성합니다.
        foreach (Lobby lobby in lobbies)
        {
            CreateRoomEntryUI(lobby);
        }

        refreshButton.interactable = true;
    }

    /// <summary>
    /// 방 정보를 바탕으로 스크롤뷰 내부에 버튼을 생성합니다.
    /// </summary>
    private void CreateRoomEntryUI(Lobby lobby)
    {
        // 스크롤뷰 Content 자식으로 프리팹 생성
        GameObject entry = Instantiate(roomEntryPrefab, roomListContainer);

        // 프리팹 내부의 Text 컴포넌트를 찾아 방 이름과 인원수를 적어줍니다.
        TMP_Text text = entry.GetComponentInChildren<TMP_Text>();
        if (text != null)
        {
            text.text = $"{lobby.Name} ({lobby.Players.Count} / {lobby.MaxPlayers})";
        }

        // 이 버튼을 클릭하면 해당 로비에 자동으로 입장하도록 설정
        Button button = entry.GetComponent<Button>();
        if (button != null)
        {
            button.onClick.AddListener(async () =>
            {
                button.interactable = false;
                refreshButton.interactable = false;

                // ⭐️ RelayManager를 통해 선택한 로비 및 릴레이 서버로 접속합니다.
                await RelayManager.Instance.JoinLobby(lobby);

                // 접속에 성공하여 클라이언트로 시작되면 UI 창을 닫습니다.
                gameObject.SetActive(false);
            });
        }
    }
}