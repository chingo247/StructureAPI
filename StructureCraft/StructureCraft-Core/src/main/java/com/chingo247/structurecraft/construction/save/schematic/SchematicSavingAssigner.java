/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.construction.save.schematic;

import com.chingo247.structurecraft.StructureAPI;
import com.chingo247.structurecraft.construction.IConstructionEntry;
import com.chingo247.structurecraft.construction.ITaskAssigner;
import com.chingo247.structurecraft.construction.ITaskCallback;
import com.chingo247.structurecraft.construction.awe.AWEPlacementTask;
import com.chingo247.structurecraft.exeption.StructureException;
import com.chingo247.structurecraft.model.structure.IStructure;
import com.chingo247.structurecraft.placement.BlockPlacement;
import com.chingo247.structurecraft.placement.IBlockPlacement;
import com.chingo247.structurecraft.placement.RotationalPlacement;
import com.chingo247.structurecraft.plan.IStructurePlan;
import com.chingo247.structurecraft.util.Progress;
import com.chingo247.structurecraft.util.RegionUtil;
import com.google.common.collect.Lists;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.regions.CuboidRegion;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.primesoft.asyncworldedit.api.IAsyncWorldEdit;
import org.primesoft.asyncworldedit.worldedit.AsyncEditSession;

/**
 *
 * @author Chingo
 */
public abstract class SchematicSavingAssigner implements ITaskAssigner {
    
    protected static final int CHUNK_SIZE = 16;
    protected static final double MIN_PCT_DIFFERENCE = 10;

    protected List<CuboidRegion> getChunkedAreas(CuboidRegion area ,int chunkSize) {
        List<CuboidRegion> subareas = Lists.newArrayList();
        Vector min = area.getMinimumPoint();
        
        List<Vector2D> chunks = new ArrayList<>(area.getChunks());
        Collections.sort(chunks, RegionUtil.ORDERED_XZ);
        
        Vector areaMin = Vector.ZERO;
        Vector areaMax = RegionUtil.getSize(area).subtract(Vector.ONE);
        
        for(Vector2D v : chunks) {
            Vector minRel = new BlockVector(v.getBlockX() * CHUNK_SIZE, 0, v.getBlockZ()  * CHUNK_SIZE).subtract(min).setY(0);
            Vector maxRel = minRel.add(chunkSize, area.getMaximumY() - area.getMinimumY(), chunkSize);
            
//            // Fit min positions
            if(!area.contains(minRel)) {
                minRel = minRel
                        .setX(minRel.getBlockX() < areaMin.getBlockX() ? areaMin.getBlockX() : minRel.getBlockX())
                        .setZ(minRel.getBlockZ() < areaMin.getBlockZ() ? areaMin.getBlockZ() : minRel.getBlockZ());
            }
//            
//            // Fit max positions
            if(!area.contains(maxRel)) {
                maxRel = maxRel
                        .setX(maxRel.getBlockX() > areaMax.getBlockX() ? areaMax.getBlockX() : maxRel.getBlockX())
                        .setZ(maxRel.getBlockZ() > areaMax.getBlockZ() ? areaMax.getBlockZ() : maxRel.getBlockZ());
                        
            }
            
            CuboidRegion subarea = new CuboidRegion(minRel, maxRel);
            subareas.add(subarea);
        }
        return subareas;
    }
    
    protected abstract ITaskCallback makeCallback(IConstructionEntry structure, Progress finished);
    
    protected abstract IBlockPlacement getPlacement(IStructure structure) throws StructureException;

    @Override
    public void assignTasks(AsyncEditSession session, UUID playerOrRandomUUID, IConstructionEntry constructionEntry) throws StructureException, IOException {
        IStructure structure = constructionEntry.getStructure();
        IAsyncWorldEdit asyncWorldEdit = StructureAPI.getInstance().getAsyncWorldEditIntegration().getAsyncWorldEdit();
        IBlockPlacement placement = getPlacement(structure);
        CuboidRegion affectedArea = placement.getCuboidRegion();
        // Prepare backup resources
        File backupDir = new File(structure.getDirectory(), "backups");
        backupDir.mkdirs();
        File backup = new File(backupDir, "rollback.schematic");
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
            ITaskCallback callback = makeCallback(constructionEntry, progress);
            constructionEntry.addTask(new AWEPlacementTask(
                    asyncWorldEdit, constructionEntry, new SchematicSubPlacement(placement, region), playerOrRandomUUID, session, structure.getMin(), callback)
            );
            count+= 2; // The amount of tasks added
        }
    }
    
    
    
}
