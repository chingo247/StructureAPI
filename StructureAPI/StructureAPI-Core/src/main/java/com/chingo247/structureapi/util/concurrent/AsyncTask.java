/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.util.concurrent;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chingo
 */
public abstract class AsyncTask implements Runnable, ITask {
    /**
     * Logger.
     */
    private static final Logger LOG = Logger.getLogger(AsyncTask.class.getName());

    public AsyncTask() {
    }
    
    
    
    @Override
    public void run() {
        try {
            execute();
            succes();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            fail();
        }
    }
    
    /**
     * Executed when the 
     */
    public abstract void succes();
    
    /**
     * Executed when an exception is thrown.
     */
    public abstract void fail();
    
    
    
}
