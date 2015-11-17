/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.construction;

import com.chingo247.structurecraft.model.structure.IStructure;

/**
 *
 * @author Chingo
 */
public interface IConstructionPlanFactory {
    
    IConstructionPlan newPlan(IStructure structure, ITaskAssigner taskAssigner);
    
    IConstructionPlan newBuildPlan(IStructure structure);
    
    IConstructionPlan newDemolitionPlan(IStructure structure);
    
    IConstructionPlan newRestorePlan(IStructure structure);
    
}
