/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.capdevon.control.AdapterControl;
import com.capdevon.control.CameraCollisionControl;
import com.capdevon.control.Animator;
import com.capdevon.engine.Animation3;
import com.capdevon.engine.SoundManager;
import com.capdevon.physx.Physics;
import com.capdevon.physx.PhysxQuery;
import com.capdevon.physx.RaycastHit;
import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class PlayerControl extends AdapterControl implements AnimEventListener {

    ParticleManager particleManager;
    Camera camera;
    Weapon weapon;
    AudioNode footsteps;
    AudioNode shoot;
    AudioNode reload;

    float fov = 0;
    float aimingSpeed = 5f;
    float aimZoomRatio = 0.7f;
    float defaultFOV = 60;

    private CameraCollisionControl collCamera;
    private Animator animator;
    private BetterCharacterControl bcc;
    private final Vector3f walkDirection = new Vector3f(0, 0, 0);
    private final Vector3f viewDirection = new Vector3f(0, 0, 1);

    private final Quaternion dr = new Quaternion();
    private final Vector3f camDir = new Vector3f();
    private final Vector3f camLeft = new Vector3f();
    private final Vector2f velocity = new Vector2f();

    float m_RunSpeed = 5.5f;
    float m_MoveSpeed = 4.5f;
    float m_TurnSpeed = 10f;

    boolean _MoveForward, _MoveBackward, _MoveLeft, _MoveRight;
    boolean isRunning, isAiming, canShooting;

    private Node aimNode;
    private final RaycastHit shootHit = new RaycastHit();

    @Override
    public void setSpatial(Spatial sp) {
        super.setSpatial(sp);
        if (spatial != null) {
            this.aimNode    = addEmptyNode("aim-node", new Vector3f(0, 2, 0));
            this.collCamera = getComponent(CameraCollisionControl.class);
            this.bcc        = getComponent(BetterCharacterControl.class);
            this.animator   = getComponent(Animator.class);
            animator.addListener(this);
            setFOV(defaultFOV);
            
            footsteps   = SoundManager.getSound("footsteps");
            shoot       = SoundManager.getSound("shoot");
            reload      = SoundManager.getSound("reload");
        }
    }

    @Override
    protected void controlUpdate(float tpf) {
        // TODO Auto-generated method stub

        updateWeaponAiming(tpf);

        camera.getDirection(camDir).setY(0);
        camera.getLeft(camLeft).setY(0);

        walkDirection.set(0, 0, 0);

        if (isAiming) {
            bcc.setWalkDirection(walkDirection);
            bcc.setViewDirection(camDir);
            footsteps.stop();

        } else {
            if (_MoveForward) {
                walkDirection.addLocal(camDir);
            } else if (_MoveBackward) {
                walkDirection.addLocal(camDir.negateLocal());
            }

            if (_MoveLeft) {
                walkDirection.addLocal(camLeft);
            } else if (_MoveRight) {
                walkDirection.addLocal(camLeft.negateLocal());
            }

            walkDirection.normalizeLocal();

            if (walkDirection.lengthSquared() > 0) {
                float angle = FastMath.atan2(walkDirection.x, walkDirection.z);
                dr.fromAngleNormalAxis(angle, Vector3f.UNIT_Y);
                spatial.getWorldRotation().slerp(dr, 1 - (tpf * m_TurnSpeed));
                spatial.getWorldRotation().mult(Vector3f.UNIT_Z, viewDirection);
                bcc.setViewDirection(viewDirection);
            }

            float xSpeed = isRunning ? m_RunSpeed : m_MoveSpeed;
            bcc.setWalkDirection(walkDirection.multLocal(xSpeed));

            Vector3f v = bcc.getVelocity(null);
            velocity.set(v.x, v.z);
            boolean isMoving = (velocity.length() / xSpeed) > .2f;

            if (isMoving) {
                setAnimTrigger(isRunning ? AnimDefs.Running_2 : AnimDefs.Running);
                footsteps.setVolume(isRunning ? 2f : .4f);
                footsteps.setPitch(isRunning ? 1f : .85f);
                footsteps.play();

            } else {
                setAnimTrigger(AnimDefs.Idle);
                footsteps.stop();
            }
        }
    }

    private void updateWeaponAiming(float tpf) {
        if ((fov == 0 && !isAiming) || (fov == 1 && isAiming)) {
            return;
        }

        float m = aimingSpeed * tpf;
        fov = (isAiming) ? (fov + m) : (fov - m);
        fov = FastMath.clamp(fov, 0, 1);
        setFOV(FastMath.interpolateLinear(fov, defaultFOV, aimZoomRatio * defaultFOV));
        // System.out.println("\t upateWeaponAiming: " + fov);
    }

    private void setFOV(float fov) {
        float aspect = (float) camera.getWidth() / (float) camera.getHeight();
        camera.setFrustumPerspective(fov, aspect, .2f, 100f);
    }

    public void setAiming(boolean isAiming) {
        this.isAiming = isAiming;
        // collCamera.setZooming(isAiming);
        weapon.crosshair.setEnabled(isAiming);
        setAnimTrigger(AnimDefs.Draw_Arrow);
    }

    public void changeAmmo() {
        weapon.onChangeAmmo();
    }

    public void shooting() {
        shooting(weapon);
    }

    private void shooting(Weapon weapon) {
        if (isAiming && canShooting) {

            shoot.playInstance();
            setAnimTrigger(AnimDefs.Aim_Recoil);

            // Aim the ray from character location in camera direction.
            if (Physics.doRaycast(aimNode.getWorldTranslation(), camera.getDirection(), shootHit, weapon.distance)) {
                System.out.println(" * You shot: " + shootHit);
                applyExplosion(shootHit, weapon);

            } else {
                System.out.println("Target not in range...");
            }
        }
    }

    /**
     * @param hit
     * @param weapon
     */
    private void applyExplosion(RaycastHit hit, Weapon weapon) {
        float explosionRadius = 5;
        float baseStrength = 10f;
        ColorRGBA color = ColorRGBA.randomColor();

        for (PhysicsRigidBody rgb : PhysxQuery.overlapSphere(hit.point, explosionRadius, x -> x.getMass() > 0)) {

            Vector3f expCenter2Body = rgb.getPhysicsLocation().subtract(hit.point);
            float distance = expCenter2Body.length();
            float strength = (1.f - FastMath.clamp(distance / explosionRadius, 0, 1)) * baseStrength;
            rgb.setLinearVelocity(expCenter2Body.normalize().mult(strength));

            Spatial userObj = (Spatial) rgb.getUserObject();
            applyDamage(userObj, color);
        }
        particleManager.playEffect(weapon.getEffectName(), shootHit.point, 10f);
    }

    /**
     * @param hit
     * @param weapon
     */
    private void applyImpulse(RaycastHit hit, Weapon weapon) {
        RigidBodyControl rgb = hit.userObject.getControl(RigidBodyControl.class);
        if (rgb != null && rgb.getMass() > 0) {

            Vector3f force = rgb.getGravity(null).negateLocal().multLocal(rgb.getMass());
            rgb.applyImpulse(force, Vector3f.ZERO);

            ColorRGBA color = ColorRGBA.randomColor();
            applyDamage(hit.userObject, color);
        }
        particleManager.playEffect(weapon.getEffectName(), shootHit.point, 10f);
    }

    private void applyDamage(Spatial sp, ColorRGBA color) {
        if (sp instanceof Geometry) {
            Geometry geom = (Geometry) sp;
            geom.getMaterial().setColor("Color", color);
        }
    }

    private void setAnimTrigger(Animation3 newAnim) {
        if (checkTransition(newAnim, AnimDefs.Running, AnimDefs.Running_2)) {
            animator.crossFade(newAnim);
        } else {
            animator.setAnimation(newAnim);
        }
    }

    private boolean checkTransition(Animation3 newAnim, Animation3 a, Animation3 b) {
        String curAnim = animator.getAnimationName();
        return (newAnim.equals(a) && b.name.equals(curAnim)) || (newAnim.equals(b) && a.name.equals(curAnim));
    }

    @Override
    public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
        //To change body of generated methods, choose Tools | Templates.
        if (animName.equals(AnimDefs.Aim_Recoil.name)) {
            setAnimTrigger(AnimDefs.Draw_Arrow);

        } else if (animName.equals(AnimDefs.Draw_Arrow.name)) {
            setAnimTrigger(AnimDefs.Aim_Overdraw);
        }
    }

    @Override
    public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
        // To change body of generated methods, choose Tools | Templates.
        if (animName.equals(AnimDefs.Aim_Recoil.name) || animName.equals(AnimDefs.Draw_Arrow.name)) {
            setWeaponCharging();

        } else if (animName.equals(AnimDefs.Aim_Overdraw.name)) {
            setWeaponReady();
        }
    }

    private void setWeaponReady() {
        canShooting = true;
        weapon.crosshair.setColor(ColorRGBA.White);
        reload.play();
    }

    private void setWeaponCharging() {
        canShooting = false;
        weapon.crosshair.setColor(ColorRGBA.Red);
        reload.stop();
    }
}
