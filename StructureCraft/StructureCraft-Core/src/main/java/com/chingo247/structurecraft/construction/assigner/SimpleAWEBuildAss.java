/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.construction.assigner;

import com.chingo247.structurecraft.IStructureAPI;
import com.chingo247.structurecraft.construction.ITaskCallback;
import com.chingo247.structurecraft.construction.IConstructionEntry;
import com.chingo247.structurecraft.construction.options.PlaceOptions;
import com.chingo247.structurecraft.event.StructureEvent;
import com.chingo247.structurecraft.event.structure.StructureBuildCompleteEvent;
import com.chingo247.structurecraft.event.structure.StructureBuildingEvent;
import com.chingo247.structurecraft.event.structure.StructureConstructionCancelledEvent;
import com.chingo247.structurecraft.event.structure.StructureConstructionQueued;
import com.chingo247.structurecraft.event.structure.StructureDemolitionCompleteEvent;
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
    protected ITaskCallback getCallbackFor(final IConstructionEntry entry) {
        return new ITaskCallback() {

            @Override
            public void onComplete() {
                structureAPI.getEventDispatcher().post(new StructureBuildCompleteEvent(entry.getStructure()));
            }

            @Override
            public void onCancelled() {
                structureAPI.getEventDispatcher().post(new StructureConstructionCancelledEvent(entry.getStructure()));
            }

            @Override
            public void onStarted() {
                structureAPI.getEventDispatcher().post(new StructureBuildingEvent(entry.getStructure()));
            }

            @Override
            public void onQueued() {
                structureAPI.getEventDispatcher().post(new StructureConstructionQueued(entry.getStructure()));
            }
        };
    }

}
