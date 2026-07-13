using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class PlayerManager : MonoBehaviour
{
    public static PlayerManager Instance { get; private set; }
    private static BoxCollider2D BC;
   
    // Start is called before the first frame update
    void Start()
    {
        BC = GetComponent<BoxCollider2D>();
    }

    public static void OnBoxCollider2D()
    {
        BC.enabled = false;
    }
}
