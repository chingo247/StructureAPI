/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.logging;

import com.chingo247.xplatform.core.IWorld;


/**
 *
 * @author Chingo
 */
public abstract class ALogger  {

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
    
    public abstract void logBlock(int x, int y, int z, String player, IWorld world, int oldMaterial, byte oldData, int newMaterial, byte newData);
    
}
