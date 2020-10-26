/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.capdevon.engine;

import com.jme3.app.state.AppState;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * capdevon
 */
public class SceneManager extends SimpleAppState {
    
    private Scene currScene;
    private AsyncOperation asyncOperation;
    private ScheduledThreadPoolExecutor executor;
    
    @Override
    public void simpleInit() {
        // TODO Auto-generated method stub
        this.executor = new ScheduledThreadPoolExecutor(2);
    }

    @Override
    public void update(float tpf) {
        //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * Unloads the Scene asynchronously in the background
     * @param newScene
     * @return 
     */
    public AsyncOperation unloadSceneAsync(Scene newScene) {
        currScene = newScene;
        // Run a task specified by a Supplier object asynchronously
        CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> unloadScene(), executor);
        asyncOperation = new AsyncOperation(future);
        return asyncOperation;
    }
    
    public void unloadScene(Scene newScene) {
        currScene = newScene;
        asyncOperation = null;
        unloadScene();
    }
    
    /**
     * Loads the Scene asynchronously in the background
     * @param newScene
     * @return 
     */
    public AsyncOperation loadSceneAsync(Scene newScene) {
        currScene = newScene;
        // Run a task specified by a Supplier object asynchronously
        CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> loadScene(), executor);
        asyncOperation = new AsyncOperation(future);
        return asyncOperation;
    }
    
    public void loadScene(Scene newScene) {
        currScene = newScene;
        asyncOperation = null;
        loadScene();
    }
    
    private boolean loadScene() {
        // attach all state managers
        float i = 1;
        for (Class<? extends AppState> clazz : currScene.systemPrefabs) {
            try {
                AppState appState = clazz.newInstance();
                stateManager.attach(appState);
                System.out.println("attaching ... AppState: " + clazz.getCanonicalName());
                
                float progress = (i / currScene.systemPrefabs.size()) * 100;
                updateProgress(Math.round(progress));
                i++;
                
            } catch (InstantiationException | IllegalAccessException ex) {
                System.err.println(ex);
                return false;
            }
        }
        
        return true;
    }
    
    private boolean unloadScene() {
        float i = 1;
        for (Class<? extends AppState> clazz : currScene.systemPrefabs) {
            try {
                AppState appState = stateManager.getState(clazz);
                if (appState != null) {
                    stateManager.detach(appState);
                    System.out.println("detaching ... AppState: " + clazz.getCanonicalName());

                    float progress = (i / currScene.systemPrefabs.size()) * 100;
                    updateProgress(Math.round(progress));
                    i++;
                }
            } catch (Exception ex) {
                System.err.println(ex);
                return false;
            }
        }

        return true;
    }
    
    private void updateProgress(int value) {
        if (asyncOperation != null) {
            System.out.println("progress: " + value + "%");
            asyncOperation.setProgress(value);
            
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                System.err.println(ex);
            }
        }
    }
    
    @Override
    public void cleanup() {
        executor.shutdown();
        System.out.println("executor.shutdown");
    }
    
}
