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
import com.chingo247.structurecraft.placement.DemolishingPlacement;
import com.chingo247.structurecraft.placement.IBlockPlacement;
import com.chingo247.structurecraft.util.Progress;
import com.chingo247.structurecraft.util.RegionUtil;

/**
 *
 * @author Chingo
 */
public class SchematicSavingDemolishAssigner extends SchematicSavingAssigner {

    @Override
    protected ITaskCallback makeCallback(IConstructionEntry structure, Progress finished) {
        return new SafeDemolishCallback(structure, finished);
    }

    @Override
    protected IBlockPlacement getPlacement(IStructure structure) throws StructureException {
        return new DemolishingPlacement(RegionUtil.getSize(structure.getCuboidRegion()));
    }
    
    private class SafeDemolishCallback implements ITaskCallback {
        
        private IConstructionEntry entry;
        private Progress progress;

        public SafeDemolishCallback(IConstructionEntry entry, Progress progress) {
            this.entry = entry;
            this.progress = progress;
        }
        
        @Override
            public void onComplete() {
                if(!entry.hasNextTask()) {
                    StructureAPI.getInstance().getEventDispatcher().dispatchEvent(new StructureStateChangeEvent(entry.getStructure(), ConstructionStatus.REMOVED));
                } else if (progress != null) {
                    StructureAPI.getInstance().getEventDispatcher().dispatchEvent(new StructureProgressUpdateEvent(entry.getStructure(), progress, ConstructionStatus.DEMOLISHING));
                }
            }

            @Override
            public void onCancelled() {
                StructureAPI.getInstance().getEventDispatcher().dispatchEvent(new StructureConstructionCancelledEvent(entry.getStructure()));
            }

            @Override
            public void onStarted() {
                StructureAPI.getInstance().getEventDispatcher().dispatchEvent(new StructureStateChangeEvent(entry.getStructure(), ConstructionStatus.DEMOLISHING));
            }

            @Override
            public void onQueued() {
                StructureAPI.getInstance().getEventDispatcher().dispatchEvent(new StructureConstructionQueued(entry.getStructure()));
            }
    }
}
