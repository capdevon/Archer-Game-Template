/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.capdevon.engine.SimpleAppState;
import com.jme3.audio.AudioNode;
import com.jme3.effect.ParticleEmitter;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;


/**
 *
 */
public class ParticleManager extends SimpleAppState {

    private Node rootLocal;

	@Override
	protected void simpleInit() {
		rootLocal = new Node("Particles");
		rootNode.attachChild(rootLocal);
	}

	@Override
	public void cleanup() {
		rootLocal.detachAllChildren();
	}

	public void playEffect(String name, Vector3f location, float time) {
		
		Node emitter = (Node) assetManager.loadModel(name);
		emitter.setLocalTranslation(location);
		rootLocal.attachChild(emitter);

		EmitterData data = new EmitterData(emitter);
		emitter.addControl(new TimerControl(time) {
			@Override
			public void onTrigger() {
				data.stop();
				rootLocal.detachChild(emitter);
			}
		});
		
		// play effect
		data.play();
	}

    /**
     * ------------------------------------------------------------------
     * @EmitterData
     * ------------------------------------------------------------------
     */
    private class EmitterData {

        public Node emitter;

        public EmitterData(Node emitter) {
        	this.emitter = emitter;
        }

        protected void stop() {
            for (Spatial sp : emitter.getChildren()) {
                if (sp instanceof AudioNode) {
                    ((AudioNode) sp).stop();

                } else if (sp instanceof ParticleEmitter) {
                    ((ParticleEmitter) sp).killAllParticles();
                }
            }
        }

        protected void play() {
            for (Spatial sp : emitter.getChildren()) {
                if (sp instanceof AudioNode) {
                    ((AudioNode) sp).play();

                } else if (sp instanceof ParticleEmitter) {
                    ((ParticleEmitter) sp).emitAllParticles();
                }
            }
        }

    }
}
