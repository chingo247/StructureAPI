/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.placing;

/**
 *
 * @author Chingo
 */
public interface IPlaceResult {
    
    /**
     * Returns true if the placing was succesfull
     * @return True if the placing was succesfull
     */
    boolean succes();
    
    /**
     * The error message if the operation was unsuccesfull
     * @return the error message or null if the operation was succesfull
     */
    String error();
    
}
