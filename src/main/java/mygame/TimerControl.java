package mygame;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;

public abstract class TimerControl extends AbstractControl {

	float lifeTime = 0;
	float maxLifeTime = 0;

	public TimerControl(float maxLifeTime) {
		this.maxLifeTime = maxLifeTime;
	}

	@Override
	protected void controlUpdate(float tpf) {
		lifeTime += tpf;
		if (lifeTime > maxLifeTime) {
			onTrigger();
		}
	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp) {
		// TODO Auto-generated method stub
	}
	
	public abstract void onTrigger();
}