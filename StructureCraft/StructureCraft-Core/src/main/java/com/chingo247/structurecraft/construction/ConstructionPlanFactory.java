/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.construction;

import com.chingo247.structurecraft.IStructureAPI;
import com.chingo247.structurecraft.construction.IConstructionExecutor;
import com.chingo247.structurecraft.construction.awe.AWEAssignerFactory;
import com.chingo247.structurecraft.construction.save.schematic.SchematicSavingAssignerFactory;
import com.chingo247.structurecraft.model.structure.IStructure;

/**
 *
 * @author Chingo
 */
class ConstructionPlanFactory implements IConstructionPlanFactory {
    
    private final IConstructionExecutor executor;
    private final IStructureAPI structureAPI;
    private IAssignerFactory defaultAssignerFactory;
    private SchematicSavingAssignerFactory schematicSavingAssignerFactory;

    public ConstructionPlanFactory(IStructureAPI structureAPI, IConstructionExecutor executor) {
        this.executor = executor;
        this.structureAPI = structureAPI;
        this.defaultAssignerFactory = new AWEAssignerFactory(structureAPI);
        this.schematicSavingAssignerFactory = new SchematicSavingAssignerFactory();
    }
    
    

    @Override
    public IConstructionPlan newPlan(IStructure structure, ITaskAssigner taskAssigner) {
        IConstructionPlan plan = new ConstructionPlan(executor, structure, taskAssigner);
        return plan;
    }

    @Override
    public IConstructionPlan newBuildPlan(IStructure structure) {
        ITaskAssigner assigner = defaultAssignerFactory.newSimpleBuildAssigner();
        return new ConstructionPlan(executor, structure, assigner);
    }

    @Override
    public IConstructionPlan newDemolitionPlan(IStructure structure) {
        ITaskAssigner assigner = defaultAssignerFactory.newSimpleDemolitionAssigner();
        return new ConstructionPlan(executor, structure, assigner);
    }

    @Override
    public IConstructionPlan newSavingBuildPlan(IStructure structure) {
        ITaskAssigner assigner = schematicSavingAssignerFactory.createSavingSchematicBuildAssigner();
        return new ConstructionPlan(executor, structure, assigner);
    }

    @Override
    public IConstructionPlan newSavingDemolitionPlan(IStructure structure) {
        ITaskAssigner assigner = schematicSavingAssignerFactory.createSavingSchematicBuildAssigner();
        return new ConstructionPlan(executor, structure, assigner);
    }

    
    
    
    
}
