/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.construction.assigner;

import com.chingo247.structurecraft.IStructureAPI;
import com.chingo247.structurecraft.construction.ICallback;
import com.chingo247.structurecraft.construction.IConstructionEntry;
import com.chingo247.structurecraft.event.construction.BuildCompleteEvent;
import com.chingo247.structurecraft.event.construction.DemolitionCompleteEvent;
import com.chingo247.structurecraft.exeption.StructureException;
import com.chingo247.structurecraft.placement.interfaces.IPlacement;

/**
 *
 * @author Chingo
 */
class SimpleAWEBuildAss extends AWETaskAssigner {

    public SimpleAWEBuildAss(IStructureAPI structureAPI) {
        super(structureAPI);
    }

    @Override
    protected IPlacement getPlacementFor(IConstructionEntry entry) throws StructureException {
        return entry.getStructure().getStructurePlan().getPlacement();
    }

    @Override
    protected ICallback getCallbackFor(final IConstructionEntry entry) {
        return new ICallback() {

            @Override
            public void onComplete() {
                structureAPI.getEventDispatcher().post(new BuildCompleteEvent(entry.getStructure()));
            }

            @Override
            public void onCancelled() {
                structureAPI.getEventDispatcher().post(new DemolitionCompleteEvent(entry.getStructure()));
            }
        };
    }

}
