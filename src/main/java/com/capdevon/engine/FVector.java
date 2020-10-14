/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.capdevon.engine;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

/**
 *
 */
public class FVector {
	
    public static float distanceTo(Spatial a, Spatial b) {
        return a.getWorldBound().distanceToEdge(b.getWorldTranslation());
    }
    
    public static float distanceTo(Spatial a, Spatial b, float radius) {
    	return Math.max(distance(a, b) - radius, 0f);
    }
    
    public static Vector3f subtract(Spatial a, Spatial b) {
        return b.getWorldTranslation().subtract(a.getWorldTranslation());
    }

    public static float distance(Spatial a, Spatial b) {
        return b.getWorldTranslation().distance(a.getWorldTranslation());
    }
    
     public static float sqrDistance(Spatial a, Spatial b) {
        return b.getWorldTranslation().distanceSquared(a.getWorldTranslation());
    }

    public static float distanceFrom(Vector3f a, Vector3f b) {
        return a.subtract(b).length();
    }

    public static float sqrDistanceFrom(Vector3f a, Vector3f b) {
        return a.subtract(b).lengthSquared();
    }

    public static float angle(Vector3f v1, Vector3f v2) {
        return v1.angleBetween(v2);
    }
    
    public static Vector3f dirFromAngle(float angle) {
        return new Vector3f(FastMath.sin(angle), 0, FastMath.cos(angle));
    }
    
    public static Vector3f forward(Spatial sp) {
        return sp.getWorldRotation().mult(Vector3f.UNIT_Z);
    }

    public static Vector3f left(Spatial sp) {
        return sp.getWorldRotation().mult(Vector3f.UNIT_X);
    }

}
