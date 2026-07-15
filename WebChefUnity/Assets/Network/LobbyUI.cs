private IEnumerator CheckAndRepositionLocalPlayer()
{
    isSpawnedHere = true;
    if (doorCollider != null) doorCollider.enabled = true;

    // 씬 로드 직후 물리 연산이 안정될 때까지 1프레임 대기
    yield return new WaitForEndOfFrame();

    var localPlayer = NetworkManager.Singleton?.LocalClient?.PlayerObject?.gameObject;
    if (localPlayer != null)
    {
        // 1. 물리 엔진 강제 정지 (Kinematic 활용)
        var rb = localPlayer.GetComponent<Rigidbody2D>();
        RigidbodyType2D originalBodyType = RigidbodyType2D.Dynamic;
        if (rb != null)
        {
            originalBodyType = rb.bodyType;
            rb.bodyType = RigidbodyType2D.Kinematic;
            rb.velocity = Vector2.zero;
        }

        // 2. 위치 계산 및 텔레포트
        Vector3 offset = spawnDirection switch
        {
            SpawnDirection.Right => Vector3.right * spawnDistance,
            SpawnDirection.Left => Vector3.left * spawnDistance,
            SpawnDirection.Up => Vector3.up * spawnDistance,
            _ => Vector3.down * spawnDistance
        };

        Vector3 finalPos = transform.position + offset;

        // NetworkTransform의 Teleport 활용 (동기화 보장)
        if (localPlayer.TryGetComponent<NetworkTransform>(out var netTransform))
        {
            netTransform.Teleport(finalPos, localPlayer.transform.rotation, localPlayer.transform.localScale);
        }
        else
        {
            localPlayer.transform.position = finalPos;
        }

        // 3. 물리 엔진 복구
        if (rb != null)
        {
            rb.bodyType = originalBodyType;
        }

        // 4. 안전하게 초기화
        if (RoomManager.Instance != null) RoomManager.Instance.targetDoorName = "";
    }
}