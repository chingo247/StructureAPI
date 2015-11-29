/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.construction.assigner.awe;

import com.chingo247.structurecraft.IStructureAPI;
import com.chingo247.structurecraft.StructureAPI;
import com.chingo247.structurecraft.construction.ITaskCallback;
import com.chingo247.structurecraft.construction.IConstructionEntry;
import com.chingo247.structurecraft.construction.IConstructionEntry;
import com.chingo247.structurecraft.construction.IPlacementSource;
import com.chingo247.structurecraft.construction.ITaskCallback;
import com.chingo247.structurecraft.construction.SinglePlacementSource;
import com.chingo247.structurecraft.event.structure.StructureBuildCompleteEvent;
import com.chingo247.structurecraft.event.structure.StructureBuildingEvent;
import com.chingo247.structurecraft.event.structure.StructureConstructionCancelledEvent;
import com.chingo247.structurecraft.event.structure.StructureConstructionQueued;
import com.chingo247.structurecraft.exeption.StructureException;
import com.chingo247.structurecraft.placement.interfaces.IPlacement;

/**
 *
 * @author Chingo
 */
class SimpleAWEBuildAss extends AWETaskAssigner {

    @Override
    protected void setPlacementSource(IConstructionEntry entry) throws StructureException {
        entry.setPlacementSource(new SinglePlacementSource(
                entry.getStructure().getStructurePlan().getPlacement()));
    }

    

    @Override
    protected ITaskCallback getCallbackFor(final IConstructionEntry entry) {
        final IStructureAPI structureAPI = StructureAPI.getInstance();
        return new ITaskCallback() {

            @Override
            public void onComplete() {
                structureAPI.getEventDispatcher().dispatchEvent(new StructureBuildCompleteEvent(entry.getStructure()));
            }

            @Override
            public void onCancelled() {
                structureAPI.getEventDispatcher().dispatchEvent(new StructureConstructionCancelledEvent(entry.getStructure()));
            }

            @Override
            public void onStarted() {
                structureAPI.getEventDispatcher().dispatchEvent(new StructureBuildingEvent(entry.getStructure()));
            }

            @Override
            public void onQueued() {
                structureAPI.getEventDispatcher().dispatchEvent(new StructureConstructionQueued(entry.getStructure()));
            }
        };
    }

}
