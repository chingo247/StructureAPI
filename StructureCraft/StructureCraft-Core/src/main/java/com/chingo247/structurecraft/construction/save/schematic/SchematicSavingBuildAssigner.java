/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.construction.save.schematic;

import com.chingo247.structurecraft.StructureAPI;
import com.chingo247.structurecraft.construction.IConstructionEntry;
import com.chingo247.structurecraft.construction.ITaskCallback;
import com.chingo247.structurecraft.event.structure.StructureConstructionCancelledEvent;
import com.chingo247.structurecraft.event.structure.StructureConstructionQueued;
import com.chingo247.structurecraft.event.structure.StructureProgressUpdateEvent;
import com.chingo247.structurecraft.event.structure.StructureStateChangeEvent;
import com.chingo247.structurecraft.exeption.StructureException;
import com.chingo247.structurecraft.model.structure.ConstructionStatus;
import com.chingo247.structurecraft.model.structure.IStructure;
import com.chingo247.structurecraft.placement.BlockPlacement;
import com.chingo247.structurecraft.placement.IBlockPlacement;
import com.chingo247.structurecraft.placement.RotationalPlacement;
import com.chingo247.structurecraft.plan.IStructurePlan;
import com.chingo247.structurecraft.util.Progress;

/**
 *
 * @author Chingo
 */
class SchematicSavingBuildAssigner extends SchematicSavingAssigner {

    @Override
    protected IBlockPlacement getPlacement(IStructure structure) throws StructureException {
        IStructurePlan plan = structure.getStructurePlan();
        BlockPlacement placement;
        if (plan.getPlacement() instanceof BlockPlacement) {
            placement = (BlockPlacement) plan.getPlacement();
        } else {
            throw new StructureException("Structure placement must inherit from BlockPlacement");
        }
        if (placement instanceof RotationalPlacement) {
            RotationalPlacement rt = (RotationalPlacement) placement;
            rt.rotate(structure.getDirection().getRotation());
        }
        
        return placement;
    }

    @Override
    protected ITaskCallback makeCallback(IConstructionEntry constructionEntry, Progress finished) {
        return new SafeBuildCallback(constructionEntry, finished);
    }
    
    private class SafeBuildCallback implements ITaskCallback {
        
        private IConstructionEntry entry;
        private Progress progress;

        public SafeBuildCallback(IConstructionEntry entry, Progress progress) {
            this.entry = entry;
            this.progress = progress;
        }
        
        @Override
            public void onComplete() {
                if(!entry.hasNextTask()) {
                    StructureAPI.getInstance().getEventDispatcher().dispatchEvent(new StructureStateChangeEvent(entry.getStructure(), ConstructionStatus.COMPLETED));
                } else if (progress != null) {
                    StructureAPI.getInstance().getEventDispatcher().dispatchEvent(new StructureProgressUpdateEvent(entry.getStructure(), progress, ConstructionStatus.BUILDING));
                }
            }

            @Override
            public void onCancelled() {
                StructureAPI.getInstance().getEventDispatcher().dispatchEvent(new StructureConstructionCancelledEvent(entry.getStructure()));
            }

            @Override
            public void onStarted() {
                if(progress != null && progress.getProgress() == 0.0) {
                    StructureAPI.getInstance().getEventDispatcher().dispatchEvent(new StructureStateChangeEvent(entry.getStructure(), ConstructionStatus.BUILDING));
                }
            }

            @Override
            public void onQueued() {
                if(progress != null && progress.getProgress() == 0.0) {
                    StructureAPI.getInstance().getEventDispatcher().dispatchEvent(new StructureConstructionQueued(entry.getStructure()));
                }
            }
        
    }
    
    
    
    
    
}
