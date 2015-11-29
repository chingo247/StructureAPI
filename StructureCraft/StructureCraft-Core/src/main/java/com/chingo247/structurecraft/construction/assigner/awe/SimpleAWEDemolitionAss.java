/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.construction.assigner.awe;

import com.chingo247.structurecraft.IStructureAPI;
import com.chingo247.structurecraft.StructureAPI;
import com.chingo247.structurecraft.construction.IConstructionEntry;
import com.chingo247.structurecraft.construction.ITaskCallback;
import com.chingo247.structurecraft.construction.SinglePlacementSource;
import com.chingo247.structurecraft.event.structure.StructureConstructionCancelledEvent;
import com.chingo247.structurecraft.event.structure.StructureConstructionQueued;
import com.chingo247.structurecraft.event.structure.StructureDemolishingEvent;
import com.chingo247.structurecraft.event.structure.StructureDemolitionCompleteEvent;
import com.chingo247.structurecraft.exeption.StructureException;
import com.chingo247.structurecraft.placement.DemolishingPlacement;
import com.chingo247.structurecraft.placement.interfaces.IPlacement;
import com.chingo247.structurecraft.util.RegionUtil;
import com.sk89q.worldedit.Vector;

/**
 *
 * @author Chingo
 */
class SimpleAWEDemolitionAss extends AWETaskAssigner {

    @Override
    protected void setPlacementSource(IConstructionEntry entry) throws StructureException {
        Vector size = RegionUtil.getSize(entry.getStructure().getCuboidRegion());
        DemolishingPlacement pl =  new DemolishingPlacement(size);
        entry.setPlacementSource(new SinglePlacementSource(pl));
    }

    @Override
    protected ITaskCallback getCallbackFor(final IConstructionEntry entry) {
        final IStructureAPI structureAPI = StructureAPI.getInstance();
        return new ITaskCallback() {

            @Override
            public void onComplete() {
                structureAPI.getEventDispatcher().dispatchEvent(new StructureDemolitionCompleteEvent(entry.getStructure()));
            }

            @Override
            public void onCancelled() {
                structureAPI.getEventDispatcher().dispatchEvent(new StructureConstructionCancelledEvent(entry.getStructure()));
            }

            @Override
            public void onStarted() {
                structureAPI.getEventDispatcher().dispatchEvent(new StructureDemolishingEvent(entry.getStructure()));
            }

            @Override
            public void onQueued() {
                structureAPI.getEventDispatcher().dispatchEvent(new StructureConstructionQueued(entry.getStructure()));
            }
        };
    }

    
   
    
}
