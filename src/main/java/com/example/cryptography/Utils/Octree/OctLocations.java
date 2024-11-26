package com.example.cryptography.Utils.Octree;

public enum OctLocations {
    TopLeftFront(0),
    TopRightFront(1),
    BottomRightFront(2),
    BottomLeftFront(3),
    TopLeftBottom(4),
    TopRightBottom(5),
    BottomRightBack(6),
    BottomLeftBack(7);

    int num;

    OctLocations(int num){
        this.num = num;
    }

    int getNumber(){
        return num;
    }
}
