/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.util.concurrent;

/**
 *
 * @author ching
 */
public abstract class AsyncLoad extends AsyncTask {
    
    private final ILoadable loadable;
    
    /**
     * Constructor.
     * @param loadable The loadable
     */
    public AsyncLoad(final ILoadable loadable) {
        this.loadable = loadable;
    }

    @Override
    public void execute() throws Exception {
        this.loadable.load();
    }
    
    
    
}
