/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.construction.safe.schematic;

import com.chingo247.structurecraft.StructureAPI;
import com.chingo247.structurecraft.construction.IConstructionEntry;
import com.chingo247.structurecraft.construction.ITaskCallback;
import com.chingo247.structurecraft.construction.awe.AWEPlacementTask;
import com.chingo247.structurecraft.event.structure.StructureConstructionCancelledEvent;
import com.chingo247.structurecraft.event.structure.StructureConstructionQueued;
import com.chingo247.structurecraft.event.structure.StructureProgressUpdateEvent;
import com.chingo247.structurecraft.event.structure.StructureStateChangeEvent;
import com.chingo247.structurecraft.exeption.StructureException;
import com.chingo247.structurecraft.model.structure.ConstructionStatus;
import com.chingo247.structurecraft.model.structure.IStructure;
import com.chingo247.structurecraft.placement.BlockPlacement;
import com.chingo247.structurecraft.placement.interfaces.IBlockPlacement;
import com.chingo247.structurecraft.placement.interfaces.RotationalPlacement;
import com.chingo247.structurecraft.plan.interfaces.IStructurePlan;
import com.chingo247.structurecraft.util.Progress;
import com.sk89q.worldedit.regions.CuboidRegion;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.primesoft.asyncworldedit.api.IAsyncWorldEdit;
import org.primesoft.asyncworldedit.worldedit.AsyncEditSession;

/**
 *
 * @author Chingo
 */
class SchematicSavingBuildAssigner extends SchematicSavingAssigner {
    
    
    

    @Override
    public void assignTasks(AsyncEditSession session, UUID playerOrRandomUUID, IConstructionEntry constructionEntry) throws StructureException, IOException {
        IStructure structure = constructionEntry.getStructure();
        
        
        IStructurePlan plan = structure.getStructurePlan();
        IAsyncWorldEdit asyncWorldEdit = StructureAPI.getInstance().getAsyncWorldEditIntegration().getAsyncWorldEdit();
        
        BlockPlacement placement;
        if (plan.getPlacement() instanceof BlockPlacement) {
            placement = (BlockPlacement) plan.getPlacement();
        } else {
            throw new StructureException("Structure placement must inherit from BlockPlacement");
        }
        
        if (placement instanceof RotationalPlacement) {
            RotationalPlacement rt = (RotationalPlacement) placement;
            rt.rotate(constructionEntry.getStructure().getDirection().getRotation());
        }
        
        CuboidRegion affectedArea = placement.getCuboidRegion();
        // Prepare backup resources
        File backupDir = new File(structure.getDirectory(), "backups");
        backupDir.mkdirs();
        File backup = new File(backupDir, "backup.schematic");
        SchematicSaveData safeBlockData = new SchematicSaveData(backup, affectedArea);
        
        // Create place areas...
        List<CuboidRegion> chunks = getChunkedAreas(affectedArea, CHUNK_SIZE);
        
        int count = 0;
        int total = chunks.size() * 2; // Total amount of tasks
        double previousPCT = 0;
        for(CuboidRegion region : chunks) {
            // Define the reportable progress... we don't want to spam the player...
            Progress progress = new Progress(total, count);
            if(Math.abs(progress.getProgress() - previousPCT) > MIN_PCT_DIFFERENCE) {
                previousPCT = progress.getProgress();
            } else {
                progress = null;
            }
            constructionEntry.addTask(new SchematicSavingTask(constructionEntry, playerOrRandomUUID, region, session.getWorld(), safeBlockData, null));
            constructionEntry.addTask(new AWEPlacementTask(
                    asyncWorldEdit, constructionEntry, new SchematicSubPlacement(placement, region), playerOrRandomUUID, session, structure.getMin(), new SafeBuildCallback(constructionEntry, progress))
            );
            count+= 2; // The amount of tasks added
        }
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
