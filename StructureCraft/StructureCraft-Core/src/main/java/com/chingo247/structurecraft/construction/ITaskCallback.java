/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.construction;

/**
 *
 * @author Chingo
 */
public interface ITaskCallback {
    
    void onStarted();
    
    void onQueued();
    
    void onComplete();
    
    void onCancelled();
    
}
