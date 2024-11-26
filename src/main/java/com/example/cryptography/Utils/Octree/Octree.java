package com.example.cryptography.Utils.Octree;

public class Octree<T> {
    private OctPoint point;
    private OctPoint topLeftFront, bottomRightBack;
    private Octree<T>[] children = new Octree[8];
    private T object;
    public Octree(){
        point = new OctPoint();
    }
    public Octree(int x, int y, int z, T object){
        point = new OctPoint(x, y, z);
        this.object = object;
    }
    public Octree(int x1, int y1, int z1, int x2, int y2, int z2) throws OutOfBoundsException {
        if(x2 < x1 || y2 < y1 || z2 < z1){
            throw new OutOfBoundsException("The bounds are not properly set!");
        }
        point = null;
        topLeftFront = new OctPoint(x1, y1, z1);
        bottomRightBack = new OctPoint(x2, y2, z2);
        for (int i = 0; i <= 7; i++){
            children[i] = new Octree<>();
        }
    }
    public void insert(int x, int y, int z, T object) throws OutOfBoundsException {
        if(find(x, y, z)){
            throw new PointExistsException("Point already exists in the tree. X: " + x + " Y: " + y + " Z: " + z + " Object Name: " + object.getClass().getName());
        }
        if (x < topLeftFront.getX() || x > bottomRightBack.getX()
                || y < topLeftFront.getY() || y > bottomRightBack.getY()
                || z < topLeftFront.getZ() || z > bottomRightBack.getZ()){
            throw new OutOfBoundsException("Insertion point is out of bounds! X: " + x + " Y: " + y + " Z: " + z + " Object Name: " + object.getClass().getName());
        }
        int midx = (topLeftFront.getX() + bottomRightBack.getX())/2;
        int midy = (topLeftFront.getY() + bottomRightBack.getY())/2;
        int midz = (topLeftFront.getZ() + bottomRightBack.getZ())/2;
        int pos;
        if(x <= midx){
            if(y <= midy){
                if(z <= midz)
                    pos = OctLocations.TopLeftFront.getNumber();
                else
                    pos = OctLocations.TopLeftBottom.getNumber();
            }else{
                if(z <= midz)
                    pos = OctLocations.BottomLeftFront.getNumber();
                else
                    pos = OctLocations.BottomLeftBack.getNumber();
            }
        }else{
            if(y <= midy){
                if(z <= midz)
                    pos = OctLocations.TopRightFront.getNumber();
                else
                    pos = OctLocations.TopRightBottom.getNumber();
            }else {
                if(z <= midz)
                    pos = OctLocations.BottomRightFront.getNumber();
                else
                    pos = OctLocations.BottomRightBack.getNumber();
            }
        }
        if(children[pos].point == null){
            children[pos].insert(x, y, z, object);
        }
        else if(children[pos].point.isNullified()){
            children[pos] = new Octree<>(x, y, z, object);
        }
        else{
            int x_ = children[pos].point.getX();
            int y_ = children[pos].point.getY();
            int z_ = children[pos].point.getZ();
            T object_ = children[pos].object;
            children[pos] = null;
            if(pos == OctLocations.TopLeftFront.getNumber()){
                children[pos] = new Octree<>(topLeftFront.getX(), topLeftFront.getY(), topLeftFront.getZ(), midx, midy, midz);
            }
            else if(pos == OctLocations.TopRightFront.getNumber()){
                children[pos] = new Octree<>(midx + 1, topLeftFront.getY(), topLeftFront.getZ(), bottomRightBack.getX(), midy, midz);
            }
            else if(pos == OctLocations.BottomRightFront.getNumber()){
                children[pos] = new Octree<>(midx + 1, midy +1, topLeftFront.getZ(), bottomRightBack.getX(), bottomRightBack.getY(), midz);
            }
            else if(pos == OctLocations.BottomLeftFront.getNumber()){
                children[pos] = new Octree<>(topLeftFront.getX(), midy +1, topLeftFront.getZ(), midx, bottomRightBack.getY(), midz);
            }
            else if(pos == OctLocations.TopLeftBottom.getNumber()){
                children[pos] = new Octree<>(topLeftFront.getX(), topLeftFront.getY(), midz + 1, midx, midy, bottomRightBack.getZ());
            }
            else if(pos == OctLocations.TopRightBottom.getNumber()){
                children[pos] = new Octree<>(midx +1, topLeftFront.getY(), midz +1, bottomRightBack.getX(), midy, bottomRightBack.getZ());
            }
            else if(pos == OctLocations.BottomRightBack.getNumber()){
                children[pos] = new Octree<>(midx + 1, midy +1, midz +1, bottomRightBack.getX(), bottomRightBack.getY(), bottomRightBack.getZ());
            }
            else if(pos == OctLocations.BottomLeftBack.getNumber()){
                children[pos] = new Octree<>(topLeftFront.getX(), midy +1, midz +1, midx, bottomRightBack.getY(), bottomRightBack.getZ());
            }
            children[pos].insert(x_, y_, z_, object_);
            children[pos].insert(x, y, z, object);
        }
    }
    public boolean find(int x, int y, int z){
        if (x < topLeftFront.getX() || x > bottomRightBack.getX()
                || y < topLeftFront.getY() || y > bottomRightBack.getY()
                || z < topLeftFront.getZ() || z > bottomRightBack.getZ()) return false;
        int midx = (topLeftFront.getX() + bottomRightBack.getX())/2;
        int midy = (topLeftFront.getY() + bottomRightBack.getY())/2;
        int midz = (topLeftFront.getZ() + bottomRightBack.getZ())/2;
        int pos;
        if(x <= midx){
            if(y <= midy){
                if(z <= midz)
                    pos = OctLocations.TopLeftFront.getNumber();
                else
                    pos = OctLocations.TopLeftBottom.getNumber();
            }else{
                if(z <= midz)
                    pos = OctLocations.BottomLeftFront.getNumber();
                else
                    pos = OctLocations.BottomLeftBack.getNumber();
            }
        }else{
            if(y <= midy){
                if(z <= midz)
                    pos = OctLocations.TopRightFront.getNumber();
                else
                    pos = OctLocations.TopRightBottom.getNumber();
            }else {
                if(z <= midz)
                    pos = OctLocations.BottomRightFront.getNumber();
                else
                    pos = OctLocations.BottomRightBack.getNumber();
            }
        }
        if(children[pos].point == null)
            return children[pos].find(x, y, z);
        if(children[pos].point.isNullified())
            return false;
        return x == children[pos].point.getX() && y == children[pos].point.getY() && z == children[pos].point.getZ();
    }
    public T get(int x, int y, int z){
        if (x < topLeftFront.getX() || x > bottomRightBack.getX()
                || y < topLeftFront.getY() || y > bottomRightBack.getY()
                || z < topLeftFront.getZ() || z > bottomRightBack.getZ()) return null;
        int midx = (topLeftFront.getX() + bottomRightBack.getX())/2;
        int midy = (topLeftFront.getY() + bottomRightBack.getY())/2;
        int midz = (topLeftFront.getZ() + bottomRightBack.getZ())/2;

        int pos;

        if(x <= midx){
            if(y <= midy){
                if(z <= midz)
                    pos = OctLocations.TopLeftFront.getNumber();
                else
                    pos = OctLocations.TopLeftBottom.getNumber();
            }else{
                if(z <= midz)
                    pos = OctLocations.BottomLeftFront.getNumber();
                else
                    pos = OctLocations.BottomLeftBack.getNumber();
            }
        }else{
            if(y <= midy){
                if(z <= midz)
                    pos = OctLocations.TopRightFront.getNumber();
                else
                    pos = OctLocations.TopRightBottom.getNumber();
            }else {
                if(z <= midz)
                    pos = OctLocations.BottomRightFront.getNumber();
                else
                    pos = OctLocations.BottomRightBack.getNumber();
            }
        }

        if(children[pos].point == null)
            return children[pos].get(x, y, z);
        if(children[pos].point.isNullified())
            return null;
        if(x == children[pos].point.getX() && y == children[pos].point.getY() && z == children[pos].point.getZ()){
            return children[pos].object;
        }

        return null;
    }

    public boolean remove(int x, int y, int z){
        if (x < topLeftFront.getX() || x > bottomRightBack.getX()
                || y < topLeftFront.getY() || y > bottomRightBack.getY()
                || z < topLeftFront.getZ() || z > bottomRightBack.getZ()) return false;
        int midx = (topLeftFront.getX() + bottomRightBack.getX())/2;
        int midy = (topLeftFront.getY() + bottomRightBack.getY())/2;
        int midz = (topLeftFront.getZ() + bottomRightBack.getZ())/2;

        int pos;

        if(x <= midx){
            if(y <= midy){
                if(z <= midz)
                    pos = OctLocations.TopLeftFront.getNumber();
                else
                    pos = OctLocations.TopLeftBottom.getNumber();
            }else{
                if(z <= midz)
                    pos = OctLocations.BottomLeftFront.getNumber();
                else
                    pos = OctLocations.BottomLeftBack.getNumber();
            }
        }else{
            if(y <= midy){
                if(z <= midz)
                    pos = OctLocations.TopRightFront.getNumber();
                else
                    pos = OctLocations.TopRightBottom.getNumber();
            }else {
                if(z <= midz)
                    pos = OctLocations.BottomRightFront.getNumber();
                else
                    pos = OctLocations.BottomRightBack.getNumber();
            }
        }
        if(children[pos].point == null)
            return children[pos].remove(x, y, z);
        if(children[pos].point.isNullified())
            return false;
        if(x == children[pos].point.getX() && y == children[pos].point.getY() && z == children[pos].point.getZ()){
            children[pos] = new Octree<>();
        }
        return false;
    }
}
