/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.construction.assigner;

import com.chingo247.structurecraft.IStructureAPI;
import com.chingo247.structurecraft.construction.ICallback;
import com.chingo247.structurecraft.construction.IConstructionEntry;
import com.chingo247.structurecraft.event.construction.DemolitionCancelledEvent;
import com.chingo247.structurecraft.event.construction.DemolitionCompleteEvent;
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

    public SimpleAWEDemolitionAss(IStructureAPI structureAPI) {
        super(structureAPI);
    }

    @Override
    protected IPlacement getPlacementFor(IConstructionEntry entry) throws StructureException {
        Vector size = RegionUtil.getSize(entry.getStructure().getCuboidRegion());
        return new DemolishingPlacement(size);
    }

    @Override
    protected ICallback getCallbackFor(final IConstructionEntry entry) {
        return new ICallback() {

            @Override
            public void onComplete() {
                structureAPI.getEventDispatcher().post(new DemolitionCompleteEvent(entry.getStructure()));
            }

            @Override
            public void onCancelled() {
                structureAPI.getEventDispatcher().post(new DemolitionCancelledEvent(entry.getStructure()));
            }
        };
    }
    
   
    
}
