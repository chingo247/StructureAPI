/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.worldguard.protection;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 *
 * @author Chingo
 */
public class ExpirationTimer {

    private final long expireTime, updateInterval;
    private final GraphDatabaseService graph;
    private final Lock lock;
    private Timer timer;
    
    /**
     * Constructor
     * @param updateInterval The interval at which expiration is checked
     * @param expireDate The age/amount of time that should have passed since the protection of a structure
     */
    public ExpirationTimer(GraphDatabaseService graph, long updateInterval, long expireDate) {
        this.graph = graph;
        this.expireTime = expireDate;
        this.lock = new ReentrantLock();
        this.updateInterval = updateInterval;
        
    }
    
    public synchronized void start() {
       if(timer == null) {
            timer = new Timer();
            timer.schedule(new ExpireTask(), updateInterval, updateInterval); 
       }
    }
    
    public synchronized void stop() {
        if(timer != null) {
            timer.cancel();
            timer = null;
        }
    }
    
    private class ExpireTask extends TimerTask {
        
        
        @Override
        public void run() {
            if(lock.tryLock()) {
                try {
                    System.out.println("Expiration check!");
                    StructureAPIWorldGuard.getInstance().runExpirationUpdate(expireTime);
                } finally {
                    lock.unlock();
                }
            }
        }
        
        
        
    }
    
    
    
}
