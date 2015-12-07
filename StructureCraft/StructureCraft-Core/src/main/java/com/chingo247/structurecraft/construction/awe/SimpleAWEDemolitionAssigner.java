/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.construction.awe;

import com.chingo247.structurecraft.IStructureAPI;
import com.chingo247.structurecraft.StructureAPI;
import com.chingo247.structurecraft.construction.IConstructionEntry;
import com.chingo247.structurecraft.construction.ITaskCallback;
import com.chingo247.structurecraft.construction.ITaskAssigner;
import com.chingo247.structurecraft.construction.awe.AWEPlacementTask;
import com.chingo247.structurecraft.construction.options.PlaceOptions;
import com.chingo247.structurecraft.event.structure.StructureConstructionCancelledEvent;
import com.chingo247.structurecraft.event.structure.StructureConstructionQueued;
import com.chingo247.structurecraft.event.structure.StructureStateChangeEvent;
import com.chingo247.structurecraft.exeption.StructureException;
import com.chingo247.structurecraft.model.structure.ConstructionStatus;
import com.chingo247.structurecraft.placement.DemolishingPlacement;
import com.chingo247.structurecraft.placement.interfaces.IPlacement;
import com.chingo247.structurecraft.util.RegionUtil;
import com.sk89q.worldedit.Vector;
import java.io.IOException;
import java.util.UUID;
import org.primesoft.asyncworldedit.api.IAsyncWorldEdit;
import org.primesoft.asyncworldedit.worldedit.AsyncEditSession;

/**
 *
 * @author Chingo
 */
public class SimpleAWEDemolitionAssigner implements ITaskAssigner {


    protected ITaskCallback getCallbackFor(final IConstructionEntry entry) {
        final IStructureAPI structureAPI = StructureAPI.getInstance();
        return new ITaskCallback() {

            @Override
            public void onComplete() {
                if(!entry.hasNextTask()) {
                    structureAPI.getEventDispatcher().dispatchEvent(new StructureStateChangeEvent(entry.getStructure(), ConstructionStatus.REMOVED));
                } 
//                else if(source instanceof IProgressable) {
//                    structureAPI.getEventDispatcher().dispatchEvent(
//                            new StructureDemolitionProgressUpdateEvent(
//                                    entry.getStructure(), 
//                                    (IProgressable) source)
//                    );
//                }
            }

            @Override
            public void onCancelled() {
                structureAPI.getEventDispatcher().dispatchEvent(new StructureConstructionCancelledEvent(entry.getStructure()));
            }

            @Override
            public void onStarted() {
                structureAPI.getEventDispatcher().dispatchEvent(new StructureStateChangeEvent(entry.getStructure(), ConstructionStatus.DEMOLISHING));
            }

            @Override
            public void onQueued() {
                structureAPI.getEventDispatcher().dispatchEvent(new StructureConstructionQueued(entry.getStructure()));
            }
        };
    }

    @Override
    public void assignTasks(AsyncEditSession session, UUID playerOrRandomUUID, IConstructionEntry constructionEntry) throws StructureException, IOException {
        IStructureAPI structureAPI = StructureAPI.getInstance();
        IAsyncWorldEdit asyncWorldEdit = structureAPI.getAsyncWorldEditIntegration().getAsyncWorldEdit();
        Vector position = constructionEntry.getStructure().getMin(); // Always place from the min position... 
        ITaskCallback callback = getCallbackFor(constructionEntry);  
        IPlacement placement = new DemolishingPlacement(RegionUtil.getSize(constructionEntry.getStructure().getCuboidRegion()));
        AWEPlacementTask task = new AWEPlacementTask(
                        asyncWorldEdit,
                        constructionEntry,
                        placement,
                        playerOrRandomUUID,
                        session,
                        position,
                        callback
                );
        task.setOptions(new PlaceOptions());
        constructionEntry.addTask(task);
    }
    
   
    
}
