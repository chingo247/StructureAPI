/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.construction.plan;

import com.chingo247.structurecraft.exeption.StructureException;
import com.chingo247.structurecraft.model.structure.IStructure;

/**
 *
 * @author Chingo
 */
public interface IConstructionPlanFactory {
    
    IConstructionPlan newRollbackPlan(IStructure structure) throws StructureException, Exception;
    
    IConstructionPlan newBuildPlan(IStructure structure) throws StructureException, Exception;
    
    IConstructionPlan newDemolitionPlan(IStructure structure) throws StructureException, Exception;
    
    IConstructionPlan newSaveBuildPlan(IStructure structure) throws StructureException, Exception;
    
    IConstructionPlan newSaveDemolitionPlan(IStructure structure)  throws StructureException, Exception;
    
    
}
