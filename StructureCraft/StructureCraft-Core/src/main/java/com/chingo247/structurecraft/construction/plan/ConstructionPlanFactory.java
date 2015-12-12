/*
 * Copyright (C) 2015 Chingo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.chingo247.structurecraft.construction.plan;

import com.chingo247.structurecraft.IStructureAPI;
import com.chingo247.structurecraft.construction.IConstructionExecutor;
import com.chingo247.structurecraft.construction.ITaskAssigner;
import com.chingo247.structurecraft.construction.awe.AWETaskAssigner;
import com.chingo247.structurecraft.construction.save.schematic.SchematicSavingAssigner;
import com.chingo247.structurecraft.exeption.StructureException;
import com.chingo247.structurecraft.model.structure.IStructure;
import com.chingo247.structurecraft.placement.block.IBlockPlacement;
import com.chingo247.structurecraft.placement.IPlacement;
import com.chingo247.structurecraft.plan.IStructurePlan;

/**
 *
 * @author Chingo
 */
public class ConstructionPlanFactory implements IConstructionPlanFactory {
    
    private final IConstructionExecutor executor;

    public ConstructionPlanFactory(IConstructionExecutor executor) {
        this.executor = executor;
    }
    
    private boolean isSupported(IStructure structure) throws StructureException {
        IPlacement placement = structure.getStructurePlan().getPlacement();
        return (placement instanceof IBlockPlacement);
    }

    @Override
    public IConstructionPlan newRollbackPlan(IStructure structure) throws StructureException, Exception {
        ITaskAssigner assigner = new AWETaskAssigner();
        return new RollbackPlan(executor, structure, assigner);
    }
    
    
    
    @Override
    public IConstructionPlan newBuildPlan(IStructure structure) throws StructureException {
        if(!isSupported(structure)) {
            throw new StructureException("Structure doesn't support build");
        }
        
        ITaskAssigner assigner = new AWETaskAssigner();
        return new BuildPlan(executor, structure, assigner);
    }

    @Override
    public IConstructionPlan newDemolitionPlan(IStructure structure) throws StructureException {
        if(!isSupported(structure)) {
            throw new StructureException("Structure doesn't support demolishment");
        }
        ITaskAssigner assigner = new AWETaskAssigner();
        return new DemolitionPlan(executor, structure, assigner);
    }
    
    @Override
    public IConstructionPlan newSaveBuildPlan(IStructure structure) throws StructureException {
        if(!isSupported(structure)) {
            throw new StructureException("Structure doesn't support build");
        }
        
        ITaskAssigner assigner = new SchematicSavingAssigner();
        return new BuildPlan(executor, structure, assigner);
    }

    @Override
    public IConstructionPlan newSaveDemolitionPlan(IStructure structure) throws StructureException {
        if(!isSupported(structure)) {
            throw new StructureException("Structure doesn't support demolishment");
        }
        ITaskAssigner assigner = new SchematicSavingAssigner();
        return new DemolitionPlan(executor, structure, assigner);
    }

    
    
    
    
}
