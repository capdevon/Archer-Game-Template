/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.capdevon.engine;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.font.BitmapFont;
import com.jme3.input.FlyByCamera;
import com.jme3.input.InputManager;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitorAdapter;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public abstract class SimpleAppState extends AbstractAppState {
    
    // variables
    public SimpleApplication app;
    public BulletAppState    physics;
    public AppSettings       settings;
    public AppStateManager   stateManager;
    public AssetManager      assetManager;
    public InputManager      inputManager;
    public RenderManager     renderManager;
    public ViewPort          viewPort;
    public Camera            camera;
    public FlyByCamera       flyCam;
    public Node              rootNode;
    public Node              guiNode;
    public BitmapFont        guiFont;
    
//    public Node rootLocal = new Node("RootLocal");
//    public Node guiLocal = new Node("GuiLocal");
    
    public SimpleAppState() {
    }
    
    public SimpleAppState(boolean enabled) {
        super.setEnabled(enabled);
    }
    
    @Override
    public void initialize(AppStateManager asm, Application appl) {
        if (!(appl instanceof SimpleApplication)) {
            throw new IllegalArgumentException("application should be a SimpleApplication");
        }
        
        super.initialize(asm, appl);
        this.app     = (SimpleApplication) appl;
        this.physics = asm.getState(BulletAppState.class);
        
        refreshCacheFields();
        simpleInit();
        registerInput();
    }
    
    protected void refreshCacheFields() {
        this.settings       = app.getContext().getSettings();
        this.stateManager   = app.getStateManager();
        this.assetManager   = app.getAssetManager();
        this.inputManager   = app.getInputManager();
        this.renderManager  = app.getRenderManager();
        this.viewPort       = app.getViewPort();
        this.camera         = app.getCamera();
        this.flyCam         = app.getFlyByCamera();
        this.rootNode       = app.getRootNode();
        this.guiNode        = app.getGuiNode();
        this.guiFont        = assetManager.loadFont("Interface/Fonts/Default.fnt");
    }
    
    protected void simpleInit() {}
    
    protected void registerInput() {}
   
    /**
     * @param childName
     * @return 
     */
    public Node find(final String childName) {
        final List<Node> lst = new ArrayList<>();
        rootNode.breadthFirstTraversal(new SceneGraphVisitorAdapter() {
            @Override
            public void visit(Node node) {
                if (childName.equals(node.getName())) {
                    lst.add(node);
                }
            }
        });
        if (lst.isEmpty()) {
            String err = "The component %s could not be found";
            throw new RuntimeException(String.format(err, childName));
        }
        return lst.get(0);
    }

    /**
     * @param tagName
     * @return 
     */
    public List<Node> findGameObjectsWithTag(final String tagName) {
        final List<Node> lst = new ArrayList<>();
        rootNode.breadthFirstTraversal(new SceneGraphVisitorAdapter() {
            @Override
            public void visit(Node node) {
                if (tagName.equals(node.getUserData("TagName"))) {
                    lst.add(node);
                }
            }
        });
        return lst;
    }

    /**
     * @param tagName
     * @return 
     */
    public Node findWithTag(final String tagName) {
        List<Node> lst = findGameObjectsWithTag(tagName);
        if (lst.isEmpty()) {
            String err = "The object %s could not be found";
            throw new RuntimeException(String.format(err, tagName));
        }
        return lst.get(0);
    }
    
    /**
     * @param model
     * @param position
     * @param rotation
     * @return 
     */
    public Spatial instantiate(Spatial model, Vector3f position, Quaternion rotation) {
        Spatial sp = model.clone();
        sp.setLocalTranslation(position);
        sp.setLocalRotation(rotation);
        rootNode.attachChild(sp);
        return sp;
    }
    
}
