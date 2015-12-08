/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.construction;

import com.chingo247.structurecraft.placement.options.PlaceOptions;

/**
 *
 * @author Chingo
 */
public interface IAssignerFactory {
    
    ITaskAssigner newSimpleBuildAssigner();
    
    ITaskAssigner newSimpleDemolitionAssigner();
    
}
