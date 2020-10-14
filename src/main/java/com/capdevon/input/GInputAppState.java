/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.capdevon.input;

import java.util.ArrayList;
import java.util.List;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.input.InputManager;
import com.jme3.input.Joystick;
import com.jme3.input.JoystickAxis;
import com.jme3.input.JoystickButton;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.util.SafeArrayList;

/**
 *
 */
public class GInputAppState extends AbstractAppState implements AnalogListener, ActionListener {

    protected InputManager inputManager;
    protected final List<ActionListener> actionListeners;
    protected final List<AnalogListener> analogListeners;
    protected final List<String> mappingNames;
    
    public GInputAppState() {
        actionListeners = new SafeArrayList<>(ActionListener.class);
        analogListeners = new SafeArrayList<>(AnalogListener.class);
        mappingNames = new ArrayList<>();
    }

    @Override
    public void initialize(AppStateManager asm, Application app) {
        super.initialize(asm, app);
        registerWithInput(app.getInputManager());
    }

    /** 
     * Custom Keybinding: Mapping a named action to a key input.
     * @param inputManager
     */
    public void registerWithInput(InputManager inputManager) {
        
        this.inputManager = inputManager;
        
        addMapping(KeyMapping.MOVE_FORWARD, new KeyTrigger(KeyInput.KEY_W));
        addMapping(KeyMapping.MOVE_BACKWARD, new KeyTrigger(KeyInput.KEY_S));
        addMapping(KeyMapping.MOVE_LEFT, new KeyTrigger(KeyInput.KEY_A));
        addMapping(KeyMapping.MOVE_RIGHT, new KeyTrigger(KeyInput.KEY_D));
        addMapping(KeyMapping.AUTO_AIM, new KeyTrigger(KeyInput.KEY_E));
        addMapping(KeyMapping.AIMING, new KeyTrigger(KeyInput.KEY_R));
        addMapping(KeyMapping.RUNNING, new KeyTrigger(KeyInput.KEY_SPACE));
        addMapping(KeyMapping.FIRE, new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        addMapping(KeyMapping.SWITCH_WEAPON, new KeyTrigger(KeyInput.KEY_LCONTROL));
        addMapping(KeyMapping.TRIGGER_ACTION, new KeyTrigger(KeyInput.KEY_RETURN));
        addMapping(KeyMapping.TOGGLE_COVER, new KeyTrigger(KeyInput.KEY_Q));
        addMapping(KeyMapping.TOGGLE_CROUCH, new KeyTrigger(KeyInput.KEY_Z));
        addMapping(KeyMapping.TOGGLE_FLASHLIGHT, new KeyTrigger(KeyInput.KEY_F));
        addMapping(KeyMapping.TOGGLE_FLYCAMERA, new KeyTrigger(KeyInput.KEY_I));
        
        addMapping(KeyMapping.TOGGLE_PHYSICS_DEBUG, new KeyTrigger(KeyInput.KEY_0));
        addMapping(KeyMapping.KEY_1, new KeyTrigger(KeyInput.KEY_1));
        addMapping(KeyMapping.KEY_2, new KeyTrigger(KeyInput.KEY_2));
        addMapping(KeyMapping.KEY_3, new KeyTrigger(KeyInput.KEY_3));
        addMapping(KeyMapping.KEY_4, new KeyTrigger(KeyInput.KEY_4));
        
        Joystick[] joysticks = inputManager.getJoysticks();
        if (joysticks != null && joysticks.length > 0){
            for (Joystick j : joysticks) {
                mapJoystick(j);
            }
        }
    }
    
    public void mapJoystick(Joystick joypad) {

        // Map it differently if there are Z axis
        if (joypad.getAxis(JoystickAxis.Z_ROTATION) != null && joypad.getAxis(JoystickAxis.Z_AXIS) != null) {

            // And let the dpad be up and down
            assignButton(joypad, JoystickButton.BUTTON_0, KeyMapping.TRIGGER_ACTION);
            assignButton(joypad, JoystickButton.BUTTON_1, KeyMapping.TOGGLE_FLASHLIGHT);
            assignButton(joypad, JoystickButton.BUTTON_2, KeyMapping.TOGGLE_CROUCH);
            assignButton(joypad, JoystickButton.BUTTON_4, KeyMapping.RUNNING);
            assignButton(joypad, JoystickButton.BUTTON_5, KeyMapping.AUTO_AIM);
            assignButton(joypad, JoystickButton.BUTTON_6, KeyMapping.AIMING);
            assignButton(joypad, JoystickButton.BUTTON_7, KeyMapping.FIRE);
            assignButton(joypad, JoystickButton.BUTTON_8, KeyMapping.TOGGLE_PHYSICS_DEBUG);
            assignButton(joypad, JoystickButton.BUTTON_9, KeyMapping.TOGGLE_FLYCAMERA);

            // Make the left stick move
            assignAxis(joypad.getXAxis(), KeyMapping.MOVE_RIGHT, KeyMapping.MOVE_LEFT);
            assignAxis(joypad.getYAxis(), KeyMapping.MOVE_BACKWARD, KeyMapping.MOVE_FORWARD);
            
            // And let the dpad be up and down
            assignAxis(joypad.getPovYAxis(), KeyMapping.SWITCH_WEAPON, KeyMapping.SWITCH_WEAPON);
        }
    }

    public void addMapping(String bindingName, Trigger... triggers) {
        mappingNames.add(bindingName);
        inputManager.addMapping(bindingName, triggers);
        inputManager.addListener(this, bindingName);
    }
    
    public void assignButton(Joystick joypad, String logicalId, String mappingName) {
        joypad.getButton(logicalId).assignButton(mappingName);
        inputManager.addListener(this, mappingName);
    }
    
    public void assignAxis(JoystickAxis axis, String positiveMapping, String negativeMapping) {
        axis.assignAxis(positiveMapping, negativeMapping);
        inputManager.addListener(this, positiveMapping, negativeMapping);
    }

    @Override
    public void cleanup() {
        super.cleanup();
        
        for (String input : mappingNames) {
            if (inputManager.hasMapping(input)) {
                inputManager.deleteMapping(input);
            }
        }
        inputManager.removeListener(this);
    }
    
    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (isEnabled()) {
            for (ActionListener listener : actionListeners) {
                listener.onAction(name, isPressed, tpf);
            }
        }
    }

    @Override
    public void onAnalog(String name, float value, float tpf) {
        if (isEnabled()) {
            for (AnalogListener listener : analogListeners) {
                listener.onAnalog(name, value, tpf);
            }
        }
    }

    public void addActionListener(ActionListener listener) {
        actionListeners.add(listener);
    }

    public void addAnalogListener(AnalogListener listener) {
        analogListeners.add(listener);
    }

    public void removeActionListener(ActionListener listener) {
        actionListeners.remove(listener);
    }

    public void removeAnalogListener(AnalogListener listener) {
        analogListeners.remove(listener);
    }
    
}
