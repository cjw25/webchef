using System.Collections;
using System.Collections.Generic;
using Unity.Netcode;
using UnityEngine;
using UnityEngine.SceneManagement;

public class PlayerMove : NetworkBehaviour
{
    [Header("�̵� ����")]
    public float moveSpeed = 5f;

    private Rigidbody2D rb;
    private Collider2D playerCollider;
    private Vector2 moveInput;

    private Animator animator; // �ִϸ����� ������Ʈ ����

    public static PlayerMove Instance;


    private bool isFrozen = false;

    private float clientPingTimer = 0f;
    public override void OnNetworkSpawn()
    {
        rb = GetComponent<Rigidbody2D>();
        playerCollider = GetComponent<Collider2D>();
        animator = GetComponent<Animator>(); // �� ������Ʈ�� Animator ��������

        if (rb != null)
        {
            rb.gravityScale = 0f;
            rb.constraints = RigidbodyConstraints2D.FreezeRotation;
        }

        isFrozen = false;

        if (IsOwner)
        {
            Instance = this;
            DontDestroyOnLoad(gameObject);
            SceneManager.sceneLoaded += HandleNewSceneSetup;

            ExecuteTeleportProcess();
        }
        else
        {
            // ���� ���� �̵������� ������ Ÿ�� ��ǻ���� ����(Rigidbody)�� �����ؾ� �ϹǷ�,
            // ȣ��Ʈ/���� ������ �ƴ� '���� Ŭ���̾�Ʈ' ȭ�鿡���� Ÿ���� ������ ���ݴϴ�.
            if (rb != null)
            {
                rb.bodyType = RigidbodyType2D.Kinematic;
                rb.velocity = Vector2.zero;
            }
        }
    }

    // [�̱��÷��� ������] ���ڵ� ���� ���� ȥ�� ����(�̱� �÷���)���� ���� ���� ������ġ
    private void Start()
    {
        // ���� ��Ƽ�÷��� ���� �ƴ� ����(NetworkManager�� �۵� �� ��)��� �Ϲ� ���� ������ ���ݴϴ�.
        if (NetworkManager.Singleton == null || !NetworkManager.Singleton.IsListening)
        {
            rb = GetComponent<Rigidbody2D>();
            playerCollider = GetComponent<Collider2D>();
            animator = GetComponent<Animator>();

            if (rb != null)
            {
                rb.gravityScale = 0f;
                rb.constraints = RigidbodyConstraints2D.FreezeRotation;
            }
            Instance = this;
            DontDestroyOnLoad(gameObject);
        }
    }

    public override void OnNetworkDespawn()
    {
        if (IsOwner)
        {
            SceneManager.sceneLoaded -= HandleNewSceneSetup;
        }
    }

    public void FreezeMovement()
    {
        // ��Ƽ�÷��� ������ ���� IsOwner üũ�� �ϰ�, �̱��÷��̸� �����ŵ�ϴ�.
        if (NetworkManager.Singleton != null && NetworkManager.Singleton.IsListening && !IsOwner) return;

        isFrozen = true;
        moveInput = Vector2.zero;

        UpdateAnimation(Vector2.zero); // �ִϸ��̼� ����(���� ����)

        if (NetworkManager.Singleton != null && NetworkManager.Singleton.IsListening)
        {
            MoveServerRpc(Vector2.zero); // �������Ե� ��� ���߶�� ����
        }
        else
        {
            if (rb != null) rb.velocity = Vector2.zero;
        }
    }

    private void HandleNewSceneSetup(Scene scene, LoadSceneMode mode)
    {
        if (NetworkManager.Singleton != null && NetworkManager.Singleton.IsListening && !IsOwner) return;

        this.enabled = true;

        if (rb != null)
        {
            rb.velocity = Vector2.zero;
        }

        isFrozen = true;
        if (playerCollider != null) playerCollider.enabled = false;

        StartCoroutine(TeleportDelayRoutine());
    }

    private IEnumerator TeleportDelayRoutine()
    {
        yield return new WaitForSecondsRealtime(0.05f);

        ExecuteTeleportProcess();

        yield return new WaitForEndOfFrame();

        isFrozen = false;
        if (playerCollider != null) playerCollider.enabled = true;

        TMPro.TextMeshProUGUI bubbleText = GetComponentInChildren<TMPro.TextMeshProUGUI>();
        if (bubbleText != null) bubbleText.text = "";
    }

    private void ExecuteTeleportProcess()
    {
        if (RoomManager.Instance == null || string.IsNullOrEmpty(RoomManager.Instance.targetDoorName)) return;

        GameObject targetDoor = GameObject.Find(RoomManager.Instance.targetDoorName);

        if (targetDoor != null)
        {
            Vector3 spawnPosition = targetDoor.transform.position + (Vector3.down * 1.5f);

            transform.position = spawnPosition;

            if (NetworkManager.Singleton != null && NetworkManager.Singleton.IsListening)
            {
                TeleportServerRpc(spawnPosition);
            }

            RoomManager.Instance.targetDoorName = "";
        }
    }

    [ServerRpc]
    private void TeleportServerRpc(Vector3 newPosition)
    {
        transform.position = newPosition;
    }

    void Update()
    {
        // ��Ƽ�÷��� ���ε� ���� �����ϴ� ĳ���Ͱ� �ƴ϶�� ���� ����
        if (NetworkManager.Singleton != null && NetworkManager.Singleton.IsListening && !IsOwner) return;


        // ��ȭ ���̰ų� ä�� ���� �� ���� ó��

        clientPingTimer += Time.deltaTime;
        if (clientPingTimer >= 20f)
        {
            clientPingTimer = 0f;
            KeepAliveServerRpc();
        }

        if (isFrozen || (ChatManager.Instance != null && ChatManager.Instance.IsTyping()))
        {
            if (moveInput != Vector2.zero)
            {
                moveInput = Vector2.zero;
                UpdateAnimation(Vector2.zero);

                if (NetworkManager.Singleton != null && NetworkManager.Singleton.IsListening)
                {
                    MoveServerRpc(Vector2.zero);
                }
                else
                {
                    if (rb != null) rb.velocity = Vector2.zero;
                }
            }
            return;
        }

        Vector2 prevInput = moveInput;
        moveInput.x = Input.GetAxisRaw("Horizontal");
        moveInput.y = Input.GetAxisRaw("Vertical");

        if (moveInput.sqrMagnitude > 1)
        {
            moveInput.Normalize();
        }

        // [�� ȭ�� �ִϸ��̼� ����] Ű���带 ������ ���� ��� �� ȭ�� ĳ���͸� ���̷�Ʈ�� �����ŵ�ϴ�.
        UpdateAnimation(moveInput);

        // �Է°��� ��ȭ�� ������ �� ó��
        if (moveInput != prevInput)
        {
            if (NetworkManager.Singleton != null && NetworkManager.Singleton.IsListening)
            {
                // ��Ƽ�÷��� ���¸� ������ ��Ŷ ����
                MoveServerRpc(moveInput);
            }
        }
    }
    void FixedUpdate()
    {
        // 1. �� ĳ�����̰ų�, �̱��÷����� ��쿡�� ���� �̵� ó��
        if (rb != null && !isFrozen && (IsOwner || NetworkManager.Singleton == null || !NetworkManager.Singleton.IsListening))
        {
            // [�ٽ� ����] MovePosition ��� velocity�� ����Ͽ� ���� ������ 
            // �浹(Collision)�� ���� ����ϰ� �մϴ�. 
            // �̷��� �ϸ� ��(Collider)�� ������ �� ���� ������ �ڵ����� �ӵ��� 0���� �����ϴ�.

            Vector2 targetVelocity = moveInput * moveSpeed;

            // ���� ������ �浹�� ���������� ����� �� �ֵ��� �ӵ��� �����մϴ�.
            rb.velocity = targetVelocity;

            // ���� ���� �մ� ������ ��ӵȴٸ�, �Ʒ� �ּ��� �����Ͽ� 
            // ������ ��ġ ������ �߰��� �� �ֽ��ϴ�. (��Ʈ��ũ ��ġ�� �浹 ��)
            // rb.MovePosition(rb.position + targetVelocity * Time.fixedDeltaTime);
        }
    }

    // �� [�ִϸ����� ���̷�Ʈ ���� �Լ�] ������ ���� ���� �ڵ尡 ���� ���¸� �÷����մϴ�.
    // ���������� �ٶ󺸴� ������ �ִϸ��̼� �̸��� ����ϴ� ���� (�Լ� �ۿ� �����ص� ������ �����ϰ� static�̳� ������ �ΰų�, ���� ������ΰ� Ȱ���մϴ�)
    private string lastAnimation = "Player_Down";

    // �� [�����¿� ���� ����] ������ ���� �� ���� ������ �ٶ󺸸� ���ߴ� �Լ�
    private void UpdateAnimation(Vector2 input)
    {
        if (animator == null) return;

        // 1. Ű���� ����Ű �Է��� ���� �� (�����̴� ��)
        if (input != Vector2.zero)
        {
            // �ִϸ��̼� ��� �ӵ��� ����(1)���� �۵���ŵ�ϴ�.
            animator.speed = 1f;

            // �밢�� �̵� �� �ϳ��� ���õǵ��� �켱������ �Ӵϴ�.
            if (input.x > 0) lastAnimation = "Player_Right";
            else if (input.x < 0) lastAnimation = "Player_Left";
            else if (input.y > 0) lastAnimation = "Player_Up";
            else if (input.y < 0) lastAnimation = "Player_Down";

            // ������ ������ �ִϸ��̼��� ����մϴ�.
            animator.Play(lastAnimation);
        }
        // 2. Ű���忡�� ���� ������ �� (������ ����)
        else
        {
            // ���������� �ȴ� ���� �ִϸ��̼��� 'ù ��° ������(�� �ִ� ���)' ���·� ���� �����մϴ�.
            // Play("�ִϸ��̼��̸�", ���̾��ȣ, �����ġ 0f~1f)
            animator.Play(lastAnimation, 0, 0f);

            // ��� �ӵ��� 0���� ����� ���� �������� �ʰ� ������ ��������ϴ�!
            animator.speed = 0f;
        }
    }

    [ServerRpc]
    private void MoveServerRpc(Vector2 inputDirection)
    {
        if (rb != null)
        {
            rb.velocity = inputDirection * moveSpeed;
        }

        // ���� ���� �̵� �� �ٸ� ������ ȭ�鿡�� �� �ִϸ��̼� ���°� �ٷ� �������� �����մϴ�.
        UpdateAnimationClientRpc(inputDirection);
    }

    [ClientRpc]
    private void UpdateAnimationClientRpc(Vector2 inputDirection)
    {
        // �� ĳ���Ͱ� �ƴ� �ٸ� ��� ȭ���� ĳ���͵��� �ȴٸ��� ������ �ݴϴ�.
        if (!IsOwner)
        {
            UpdateAnimation(inputDirection);
        }
    }

    [ServerRpc]
    private void KeepAliveServerRpc()
    {
        Debug.Log($"[���� ����] Ŭ���̾�Ʈ {OwnerClientId}���� ��ȣ �۽� ��...");
    }
}