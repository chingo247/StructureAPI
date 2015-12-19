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
import com.chingo247.structurecraft.StructureAPI;
import com.chingo247.structurecraft.construction.IConstructionEntry;
import com.chingo247.structurecraft.construction.IConstructionExecutor;
import com.chingo247.structurecraft.construction.IConstructionListener;
import com.chingo247.structurecraft.construction.ITaskAssigner;
import com.chingo247.structurecraft.event.structure.construction.StructureConstructionCancelledEvent;
import com.chingo247.structurecraft.event.structure.construction.StructureConstructionFailedEvent;
import com.chingo247.structurecraft.event.structure.construction.StructureConstructionQueued;
import com.chingo247.structurecraft.event.structure.construction.StructureProgressUpdateEvent;
import com.chingo247.structurecraft.event.structure.StructureStateChangeEvent;
import com.chingo247.structurecraft.exeption.StructureException;
import com.chingo247.structurecraft.model.structure.ConstructionStatus;
import com.chingo247.structurecraft.model.structure.IStructure;
import com.chingo247.structurecraft.placement.IPlacement;
import com.chingo247.structurecraft.placement.RotationalPlacement;

/**
 *
 * @author Chingo
 */
public class BuildPlan extends ConstructionPlan {

    public BuildPlan(IConstructionExecutor executor, IStructure structure, ITaskAssigner assigner) {
        super(executor, structure, assigner);
    }

    @Override
    public IPlacement getPlacement(IStructure structure) throws StructureException {
        IPlacement placement = structure.getStructurePlan().getPlacement();
        if (placement instanceof RotationalPlacement) {
            RotationalPlacement rt = (RotationalPlacement) placement;
            rt.rotate(structure.getDirection().getRotation());
        }
        return structure.getStructurePlan().getPlacement();
    }

    @Override
    public void register(IConstructionEntry entry) throws Exception {
        final IStructureAPI structureAPI = StructureAPI.getInstance();
        entry.addListener(new IConstructionListener() {

            @Override
            public void onComplete(IConstructionEntry entry) {
                structureAPI.getEventDispatcher().dispatchEvent(new StructureProgressUpdateEvent(entry.getStructure(), entry, ConstructionStatus.COMPLETED));
            }

            @Override
            public void onCancelled(IConstructionEntry entry) {
                structureAPI.getEventDispatcher().dispatchEvent(new StructureConstructionCancelledEvent(entry.getStructure()));
            }

            @Override
            public void onStarted(IConstructionEntry entry) {                               
                structureAPI.getEventDispatcher().dispatchEvent(new StructureProgressUpdateEvent(entry.getStructure(), entry, ConstructionStatus.BUILDING));
            }

            @Override
            public void onQueued(IConstructionEntry entry) {
                structureAPI.getEventDispatcher().dispatchEvent(new StructureConstructionQueued(entry.getStructure()));
            }

            @Override
            public void onProgress(IConstructionEntry entry) {
                structureAPI.getEventDispatcher().dispatchEvent(new StructureProgressUpdateEvent(entry.getStructure(), entry, ConstructionStatus.BUILDING));
            }

            @Override
            public void onFailed(IConstructionEntry entry) {
                structureAPI.getEventDispatcher().dispatchEvent(new StructureConstructionFailedEvent(entry.getStructure()));
            }
        });
    }
    
    

}
