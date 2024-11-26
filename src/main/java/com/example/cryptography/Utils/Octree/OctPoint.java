package com.example.cryptography.Utils.Octree;

public class OctPoint {

    private int x;
    private int y;
    private int z;

    private boolean nullify = false;

    public OctPoint(int x, int y, int z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public OctPoint(){
        nullify = true;
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

    public int getZ(){
        return z;
    }

    public boolean isNullified(){
        return nullify;
    }
}
