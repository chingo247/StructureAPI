/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.construction.safe.schematic;

import com.chingo247.structurecraft.construction.ITaskAssigner;
import com.google.common.collect.Lists;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.regions.CuboidRegion;
import java.util.List;

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
        Vector max = area.getMaximumPoint();
        for(Vector2D v : area.getChunks()) {
            CuboidRegion areaToDo = new CuboidRegion(
                    new BlockVector(v.getBlockX(), min.getBlockY(), v.getBlockZ()), 
                    new BlockVector(v.getBlockX() + chunkSize, max.getBlockY(), v.getBlockZ() + chunkSize) 
            );
            
            Vector minToDo = areaToDo.getMinimumPoint();
            Vector maxToDo = areaToDo.getMaximumPoint();
            
            // Fit min positions
            if(!area.contains(minToDo)) {
                minToDo.setX(minToDo.getBlockX() < min.getBlockX() ? min.getBlockX() : minToDo.getBlockX());
                minToDo.setZ(minToDo.getBlockZ() < min.getBlockZ() ? min.getBlockZ() : minToDo.getBlockZ());
            }
            
            // Fit max positions
            if(!area.contains(maxToDo)) {
                maxToDo.setX(maxToDo.getBlockX() > max.getBlockX() ? max.getBlockX() : maxToDo.getBlockX());
                maxToDo.setZ(maxToDo.getBlockZ() > max.getBlockZ() ? max.getBlockZ() : maxToDo.getBlockZ());
            }
            
            // Make positions relative to the min
            minToDo.setX(Math.abs(minToDo.getBlockX() - min.getBlockX()));
            minToDo.setZ(Math.abs(minToDo.getBlockZ() - min.getBlockZ()));
            maxToDo.setX(Math.abs(maxToDo.getBlockX() - min.getBlockX()));
            maxToDo.setZ(Math.abs(maxToDo.getBlockZ() - min.getBlockZ()));
            
            subareas.add(new CuboidRegion(minToDo, maxToDo));
        }
        return subareas;
    }
    
    
}
