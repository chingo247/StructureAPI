/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.placement.interfaces;

/**
 *
 * @author Chingo
 */
public interface ICleanablePlacement extends IPlacement{
    
    /**
     * Cleans up.
     */
    void cleanUp();
    
    /**
     * Determines if this placement data of this placement should be cleaned up after being placed
     * @return 
     */
    boolean cleanUpAfterUsage();
    
    void setCleanupAfterUsage(boolean cleanUp);
}
