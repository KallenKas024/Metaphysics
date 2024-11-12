package com.example.cryptography;

public class Node {
    public int x, y, z;
    public Node parent;

    public Node(int x, int y, int z, Node parent) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.parent = parent;
    }
}
