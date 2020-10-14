package com.capdevon.physx;

import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

public class RaycastHit {

    public Spatial collider;
    public float distance;
    public Vector3f normal = new Vector3f();
    public Vector3f point = new Vector3f();

    public Spatial getCollider() {
        return collider;
    }

    public Vector3f getNormal() {
        return normal;
    }

    public Vector3f getPoint() {
        return point;
    }

    public float getDistance() {
        return distance;
    }

    @Override
    public String toString() {
        return "RaycastHit [collider=" + collider
                + ", normal=" + normal
                + ", point=" + point
                + ", distance=" + distance
                + "]";
    }

}
