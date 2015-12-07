/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.construction;

import com.chingo247.structurecraft.IStructureAPI;
import com.chingo247.structurecraft.construction.IConstructionExecutor;
import com.chingo247.structurecraft.model.structure.IStructure;

/**
 *
 * @author Chingo
 */
class ConstructionPlanFactory implements IConstructionPlanFactory {
    
    private final IConstructionExecutor executor;
    private final IStructureAPI structureAPI;

    public ConstructionPlanFactory(IStructureAPI structureAPI, IConstructionExecutor executor) {
        this.executor = executor;
        this.structureAPI = structureAPI;
    }
    
    

    @Override
    public IConstructionPlan newPlan(IStructure structure, ITaskAssigner taskAssigner) {
        IConstructionPlan plan = new ConstructionPlan(executor, structure, taskAssigner);
        return plan;
    }

    @Override
    public IConstructionPlan newBuildPlan(IStructure structure) {
        ITaskAssigner assigner = executor.getAssignerFactory().newSimpleBuildAssigner();
        return new ConstructionPlan(executor, structure, assigner);
    }

    @Override
    public IConstructionPlan newDemolitionPlan(IStructure structure) {
        ITaskAssigner assigner = executor.getAssignerFactory().newSimpleDemolitionAssigner();
        return new ConstructionPlan(executor, structure, assigner);
    }

    
    
    
    
}
