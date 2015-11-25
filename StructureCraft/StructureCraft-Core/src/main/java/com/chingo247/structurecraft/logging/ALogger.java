/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.logging;

/**
 *
 * @author Chingo
 */
public abstract class ALogger implements IStructureAPIBlockLogger {

    private boolean enabled;
    private String name;

    public ALogger(String name) {
        this.enabled = true;
        this.name = name;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public boolean isEnabled() {
        return enabled;
    }

    public boolean reloadConfiguration() {
        return true;
    }

    public String getName() {
        return name;
    }
    
    
}
