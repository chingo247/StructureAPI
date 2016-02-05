/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.util;

/**
 * Represents something that can 'progress'
 * @author Chingo
 */
public interface IProgressable {
    
    /**
     * Gets the current progress. Returns a value between 0 and 100
     * @return The progress
     */
    double getProgress();        
    
}
