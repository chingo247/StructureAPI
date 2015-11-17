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
class ConstructionPlanFactory implements IConstructionPlanFactory {
    
    private final IConstructionExecutor executor;

    public ConstructionPlanFactory(IConstructionExecutor executor) {
        this.executor = executor;
    }
    
    

    @Override
    public IConstructionPlan newPlan(IStructure structure, ITaskAssigner taskAssigner) {
        IConstructionPlan plan = new ConstructionPlan(executor, structure, taskAssigner);
        return plan;
    }

    @Override
    public IConstructionPlan newBuildPlan(IStructure structure) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public IConstructionPlan newDemolitionPlan(IStructure structure) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public IConstructionPlan newRestorePlan(IStructure structure) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    
}
