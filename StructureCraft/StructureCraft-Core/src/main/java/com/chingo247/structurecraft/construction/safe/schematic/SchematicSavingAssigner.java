/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.construction.safe.schematic;

import com.chingo247.structurecraft.construction.ITaskAssigner;
import com.chingo247.structurecraft.util.RegionUtil;
import com.google.common.collect.Lists;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.regions.CuboidRegion;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
        
        List<Vector2D> chunks = new ArrayList<>(area.getChunks());
        Collections.sort(chunks, new Comparator<Vector2D>() {

            @Override
            public int compare(Vector2D t, Vector2D t1) {
                int v =  Integer.compare(t.getBlockZ(), t1.getBlockZ());
                if(v == 0) {
                    return Integer.compare(t.getBlockX(), t1.getBlockX());
                }
                return v;
            }
        });
        
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
    
    
}
