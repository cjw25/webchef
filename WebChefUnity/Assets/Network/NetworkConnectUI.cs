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
        createButton.onClick.AddListener(async () => {
            string code = await RelayManager.Instance.CreateRelay(4);
            if (code != null)
            {
                codeText.text = $"规 内靛: {code}";
                // 规 积己 饶 UI 见扁扁 殿 饶贸府
            }
        });

        joinButton.onClick.AddListener(async () => {
            string code = joinCodeInputField.text;
            if (!string.IsNullOrEmpty(code))
            {
                bool success = await RelayManager.Instance.JoinRelay(code);
                if (success) codeText.text = "立加 己傍!";
            }
        });
    }
}