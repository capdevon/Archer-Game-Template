/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.addon;

import java.io.File;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.capdevon.control.AdapterControl;
import com.capdevon.control.Animator;
import com.capdevon.control.CameraHandler;
import com.capdevon.engine.JMonkey3;
import com.capdevon.input.GInputAppState;
import com.capdevon.input.KeyMapping;
import com.capdevon.physx.Physics;
import com.capdevon.trigger.BoxTrigger;
import com.capdevon.trigger.EnterableTrigger;
import com.capdevon.trigger.TriggerListener;
import com.capdevon.trigger.TriggerManager;
import com.capdevon.util.BaseGameApplication;
import com.capdevon.util.WaterFactory;
import com.jme3.app.FlyCamAppState;
import com.jme3.app.state.ScreenshotAppState;
import com.jme3.app.state.VideoRecorderAppState;
import com.jme3.audio.AudioNode;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.input.controls.ActionListener;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import com.jme3.util.SkyFactory;

import mygame.AnimDefs;

/**
 */
public class Test_Swimming extends BaseGameApplication {

	/**
	 * Start the jMonkeyEngine application
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Test_Swimming app = new Test_Swimming();

		AppSettings settings = new AppSettings(true);
		settings.setTitle(Test_Swimming.class.getSimpleName());
		settings.setUseJoysticks(true);
		settings.setResolution(800, 600);
		settings.setFrequency(60);
		settings.setFrameRate(30);
		settings.setSamples(4);
		settings.setBitsPerPixel(32);

		app.setSettings(settings);
		app.setShowSettings(false);
		app.setPauseOnLostFocus(false);
		app.start();
	}

	private Node player;

	private interface TagName {
		final String SWIMMING_POOL = "SWIMMING_POOL";
	}

	@Override
	public void simpleInitApp() {
		// disable the default 1st-person flyCam!
		stateManager.detach(stateManager.getState(FlyCamAppState.class));
		flyCam.setEnabled(false);

		JMonkey3.initEngine(this);

		initPhysics(false);
		setupPlayer();
		setupScene();
		setupLights();
		setupFilters();

		/* nature sound - keeps playing in a loop. */
		AudioNode audio_nature = getAudioEnv("Sound/Environment/Nature.ogg", true, false, 4);

		GInputAppState ginput = new GInputAppState();
		ginput.addActionListener(player.getControl(PlayerControl.class));
		stateManager.attach(ginput);
	}

	@Override
	public void setupScene() {
		// To change body of generated methods, choose Tools | Templates.
		Node level = (Node) assetManager.loadModel("Scenes/swimming-pool.j3o");
		rootNode.attachChild(level);

		Node scene = (Node) level.getChild("Scene");
		for (Spatial sp : scene.getChildren()) {

			System.out.println("--ChildName: " + sp);
			Physics.addMeshCollider(sp, 0f, false);

			if (sp.getName().equals("Pool")) {

				WaterFactory factory = new WaterFactory(assetManager, viewPort);
				Geometry water = factory.build(level, 9, 10);
				water.setLocalTranslation(-5f, -0.65f, 5f);
				rootNode.attachChild(water);

				BoundingBox bbox = (BoundingBox) sp.getWorldBound();
				System.out.println(bbox);
				System.out.println(sp.getLocalTranslation());
				EnterableTrigger trigger = new BoxTrigger(assetManager, bbox.getExtent(null), true);
				trigger.setTagName(TagName.SWIMMING_POOL);
				trigger.setTarget(player.getChild("player-head"));
				sp.addControl(trigger);
			}
		}

		level.attachChild(SkyFactory.createSky(assetManager, "Scenes/Beach/FullskiesSunset0068.dds",
				SkyFactory.EnvMapType.CubeMap));

//        scene.attachChild(player);
	}

	private void setupPlayer() {
		player = (Node) assetManager.loadModel(AnimDefs.MODEL);
		player.setName("Player");
		rootNode.attachChild(player);

		BetterCharacterControl bcc = new BetterCharacterControl(.5f, 2f, 50f);
		player.addControl(bcc);
		PhysicsSpace.getPhysicsSpace().add(bcc);

		bcc.setGravity(new Vector3f(0, -9.81f, 0).multLocal(2));
		bcc.setPhysicsDamping(0.8f);

		// Setup Third Person Camera
		CameraHandler.bindChaseCamera(cam, player, inputManager, settings.useJoysticks());

		player.addControl(new Animator());
		player.addControl(new PlayerControl(cam));

		Node head = new Node("player-head");
		player.attachChild(head);
		head.setLocalTranslation(new Vector3f(0, 1.8f, 0));
	}

	private class PlayerControl extends AdapterControl implements ActionListener, TriggerListener {

		private Camera camera;
		private Animator animator;
		private BetterCharacterControl bcc;

		private final Transform transform = new Transform();
		private final Quaternion dr = new Quaternion();
		private final Vector3f walkDirection = new Vector3f(0, 0, 0);
		private final Vector3f viewDirection = new Vector3f(0, 0, 1);
		private final Vector3f camDir = new Vector3f();
		private final Vector3f camLeft = new Vector3f();
		private final Vector2f velocity = new Vector2f();

		private float m_SwimSpeed = 1.5f;
		private float m_MoveSpeed = 4.5f;
		private float m_TurnSpeed = 10f;

		private boolean _MoveForward, _MoveBackward, _MoveLeft, _MoveRight;
		private boolean isSwimming;

		public PlayerControl(Camera camera) {
			this.camera = camera;
		}

		@Override
		public void setSpatial(Spatial sp) {
			super.setSpatial(sp);
			if (spatial != null) {
				this.bcc = getComponent(BetterCharacterControl.class);
				this.animator = getComponent(Animator.class);

				// Register this as TriggerListener
				TriggerManager.getInstance().addListener(this);
			}
		}

		@Override
		protected void controlUpdate(float tpf) {
			camera.getDirection(camDir).setY(0);
			camera.getLeft(camLeft).setY(0);

			walkDirection.set(0, 0, 0);

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
				spatial.getWorldRotation().slerp(dr, m_TurnSpeed * tpf);
				spatial.getWorldRotation().mult(Vector3f.UNIT_Z, viewDirection);
				bcc.setViewDirection(viewDirection);
			}

			float xSpeed = isSwimming ? m_SwimSpeed : m_MoveSpeed;
			bcc.setWalkDirection(walkDirection.multLocal(xSpeed));

			velocity.set(bcc.getVelocity().x, bcc.getVelocity().z);
			boolean isMoving = (velocity.length() / xSpeed) > .2f;

			if (isMoving) {
				animator.setAnimation(isSwimming ? AnimDefs.Water_Moving : AnimDefs.Running);
			} else {
				animator.setAnimation(isSwimming ? AnimDefs.Water_Idle : AnimDefs.Idle);
			}
		}

		@Override
		public void onAction(String action, boolean keyPressed, float tpf) {
			// To change body of generated methods, choose Tools | Templates.
			if (action.equals(KeyMapping.MOVE_LEFT)) {
				_MoveLeft = keyPressed;
			} else if (action.equals(KeyMapping.MOVE_RIGHT)) {
				_MoveRight = keyPressed;
			} else if (action.equals(KeyMapping.MOVE_FORWARD)) {
				_MoveForward = keyPressed;
			} else if (action.equals(KeyMapping.MOVE_BACKWARD)) {
				_MoveBackward = keyPressed;
			}
		}

		public Transform getTransform() {
			return transform.set(spatial.getWorldTransform());
		}

		public Vector3f getLocation() {
			return getTransform().getTranslation();
		}

		public Quaternion getRotation() {
			return getTransform().getRotation();
		}

		public void setWalkDirection(Vector3f vec) {
			walkDirection.set(vec);
			bcc.setWalkDirection(walkDirection);
		}

		public void setViewDirection(Vector3f vec) {
			viewDirection.set(vec);
			bcc.setViewDirection(viewDirection);
		}

		@Override
		public void onTriggerEnter(EnterableTrigger trigger) {
			System.out.println("onCollisionEnter: " + trigger.getTagName());
			if (trigger.getTagName() == TagName.SWIMMING_POOL) {
				isSwimming = true;
			}
		}

		@Override
		public void onTriggerExit(EnterableTrigger trigger) {
			System.out.println("onCollisionExit: " + trigger.getTagName());
			if (trigger.getTagName() == TagName.SWIMMING_POOL) {
				isSwimming = false;
			}
		}

	}

}
