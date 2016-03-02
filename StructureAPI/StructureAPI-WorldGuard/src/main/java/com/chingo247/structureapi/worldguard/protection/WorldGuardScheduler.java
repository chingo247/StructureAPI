/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.worldguard.protection;

import com.chingo247.settlercraft.core.concurrent.KeyPool;
import java.util.concurrent.ExecutorService;

/**
 *
 * @author Chingo
 */
public class WorldGuardScheduler {
    
    private KeyPool<String> pool;
    
    private static WorldGuardScheduler instance;
    
    private WorldGuardScheduler() {}
    
    public static WorldGuardScheduler getInstance() {
        if(instance == null) {
            instance = new WorldGuardScheduler();
        }
        return instance;
    }
    
    public void setPool(ExecutorService executorService) {
        if(this.pool != null) {
            throw new RuntimeException("Already set a pool!");
        }
        this.pool = new KeyPool<>(executorService);
    }

    public void submit(String world, Runnable runnable) {
        
    }
    
    
    
}
