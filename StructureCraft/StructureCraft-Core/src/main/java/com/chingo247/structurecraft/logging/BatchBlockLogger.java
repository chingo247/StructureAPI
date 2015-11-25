/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.logging;

import com.chingo247.structurecraft.model.logging.BlockLogEntry;
import com.chingo247.xplatform.core.IWorld;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chingo
 */
public abstract class BatchBlockLogger<T extends BlockLogEntry, P extends ABlockLogTask<T>> extends ALogger {

    // Acts as BUFFER
    private Queue<T> blocksQueue;
    private int batchSize, interval, bulkSize;
    private final long DELAY, MAX_INTERVAL;
    private final ScheduledThreadPoolExecutor scheduler;
    private boolean running;
    private final Object mutex = new Object();
    private final ExecutorService service;
    private int count = 0;

    public BatchBlockLogger(String name, ExecutorService executorService) {
        super(name);
        this.MAX_INTERVAL = 5000;
        this.DELAY = 5000;
        this.bulkSize = 100000;
        this.batchSize = 5000;
        this.interval = 1000;
        this.blocksQueue = new LinkedBlockingQueue<>();
        this.scheduler = new ScheduledThreadPoolExecutor(1);
        this.service = executorService;
        
    }

    public int getBatchSize() {
        return batchSize;
    }
    
   
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    
    protected abstract T makeEntry(int x, int y, int z, String player, IWorld world, int oldMaterial, byte oldData, int newMaterial, byte newData);
    
    protected abstract P makeTask(List<T> blocks);

    @Override
    public void logBlock(int x, int y, int z, String player, IWorld world, int oldMaterial, byte oldData, int newMaterial, byte newData) {
        if(oldMaterial == newMaterial && oldMaterial == 0) {
            return; // Don't log AIR when there is no change
        }
        
        
        T blockEntry = makeEntry(x, y, z, player, world, oldMaterial, oldData, newMaterial, newData);
        if(blockEntry != null) {
            if(count < 10) {
                System.out.println("Logged new Block");
                count++;
            }
            
            blocksQueue.add(blockEntry);
            synchronized(mutex) {
                if(!running) {
                    running = true;
                    System.out.println("[BatchLogger]: START!");
                    scheduler.schedule(new LogTask(), DELAY, TimeUnit.MILLISECONDS);
                }
            }
        }
    }
    
    

    private class LogTask implements Runnable {
        
        private List<T> blocks;
        private long prevLogTime;
        private Lock lock;

        public LogTask() {
            this.lock = new ReentrantLock();
            this.blocks = new ArrayList<>(bulkSize);
        }
        
        

        @Override
        public void run() {
//            System.out.println("[BatchLogger]: LOG!");
//            System.out.println("[BatchLogger]: TRY LOCK!");
            if(blocks.isEmpty() && blocksQueue.peek() == null){
                synchronized(mutex) {
                    running = false;
                }
                System.out.println("[BatchLogger]: QUEUE AND BUFFER WAS EMPTY!");
                return; // queue was empty
            }
            
            
            
            if(lock.tryLock()) {
                boolean continueTask = true;
//                System.out.println("[BatchLogger]: LOCKED");
                
                try {
                    // Check is batch size
                    if(blocks.size() >= bulkSize) {
                        service.execute(makeTask(blocks));
                        scheduler.schedule(new LogTask(), interval, TimeUnit.MILLISECONDS);
                        System.out.println("[BatchLogger]: NEW TASK!");
                        System.out.println("[BatchLogger]: QUEUE SIZE " + blocksQueue.size());
                        continueTask = false;
                    } else {
                        
                        // Set the current log time
                        if(blocks.isEmpty()) {
                            this.prevLogTime = System.currentTimeMillis();
                        }
                        
                        while(blocks.size() < bulkSize && blocksQueue.peek() != null) {
                            blocks.add(blocksQueue.poll());
                        }
                        
                        
                        if(blocks.size() >= bulkSize || ((System.currentTimeMillis() - prevLogTime) > MAX_INTERVAL)) {
                            service.execute(makeTask(blocks));
                            scheduler.schedule(new LogTask(), interval, TimeUnit.MILLISECONDS);
                            System.out.println("[BatchLogger]: NEW TASK!");
                            System.out.println("[BatchLogger]: QUEUE SIZE " + blocksQueue.size());
                            continueTask = false;
                        }
                        
                        
                    }
                } catch(Exception ex) {
                    Logger.getLogger(BatchBlockLogger.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                } finally {
                    lock.unlock();
//                    System.out.println("[BatchLogger]: UNLOCKED");
                }
                
                if(continueTask) {
                    System.out.println("[BatchLogger]: KEEP CURRENT TASK");
                    System.out.println("[BatchLogger]: QUEUE SIZE " + blocksQueue.size());
                    scheduler.schedule(this, interval, TimeUnit.MILLISECONDS);
                }
            }
            
        }

    }
    

}
