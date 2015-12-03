/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.rollback;

import com.chingo247.structurecraft.construction.IConstructionPlan;
import com.chingo247.structurecraft.model.structure.IStructure;

/**
 *
 * @author Chingo
 */
public interface IRestorePlanFactory {
    
    IConstructionPlan newRestorePlan(IStructure structure);
    
}
