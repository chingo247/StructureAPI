/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.worldguard.protection;

import com.chingo247.settlercraft.core.concurrent.KeyPool;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chingo
 */
public class StructureAPIWorldGuardScheduler {

    private static final Logger LOG = Logger.getLogger(StructureAPIWorldGuardScheduler.class.getName());
    
    private KeyPool<String> executor;
    
    private static StructureAPIWorldGuardScheduler instance;
    
    private StructureAPIWorldGuardScheduler() {}
    
    public static StructureAPIWorldGuardScheduler getInstance() {
        if(instance == null) {
            instance = new StructureAPIWorldGuardScheduler();
        }
        return instance;
    }
    
    public void setExecutor(ExecutorService executorService) {
        if(this.executor != null) {
            throw new RuntimeException("Already set a pool!");
        }
        this.executor = new KeyPool<>(executorService);
    }
    
    public void submit(String world, final Runnable runnable) {
        submit(world, runnable, null);
    }
 
    public void submit(String world, final Runnable runnable, final ICallback callback) {
        this.executor.execute(world, new Runnable() {

            @Override
            public void run() {
                try {
                    runnable.run();
                } catch(Exception ex) {
                    LOG.log(Level.SEVERE, ex.getMessage(), ex);
                    callback.onFail(ex);
                } finally {
                    if(callback != null) {
                        callback.onComplete();
                    }
                }
            }
        });
    }
    
    
    
    
    
}
