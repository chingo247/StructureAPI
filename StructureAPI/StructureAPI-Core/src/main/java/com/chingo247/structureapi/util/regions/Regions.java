/*
 * Copyright (C) 2016 Chingo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.chingo247.structureapi.util.regions;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import java.util.Collection;

/**
 *
 * @author Chingo
 */
public class Regions {
    
//    public static Collection<ChunkCube> makeCubes(Vector min, Vector max) {
//        
//    }
//    
//    public static Collection<ChunkCube> makeCubes(CuboidRegion region, Vector cube) {
//        Vector min = region.getMinimumPoint();
//        Vector max = region.getMaximumPoint();
//        
//        int maxX = (int) Math.ceil( (double) max.getBlockX() / cube.getBlockX()) * cube.getBlockX();
//        int maxY = (int) Math.ceil( (double) max.getBlockY() / cube.getBlockY()) * cube.getBlockY();
//        int maxZ = (int) Math.ceil( (double) max.getBlockZ() / cube.getBlockZ()) * cube.getBlockZ();
//        
//        System.out.println("maxX: " + maxX + ", maxY: " + maxY + ", maxZ: " + maxZ);
//        
//        for (int x = min.getBlockX(); x < maxX; x+= cube.getBlockX()) {
//            for (int y = min.getBlockY(); y < maxY; y+= cube.getBlockY()) {
//                for (int z = min.getBlockX(); z < maxZ; z+= cube.getBlockZ()) {
//                    Vector position = new BlockVector(x, y, z);
//                    if(region.contains(position)) {
//                        
//                        int sizeX = position.getBlockX() + cube.getBlockX();
//                        int sizeY = position.getBlockY() + cube.getBlockY();
//                        int sizeZ = position.getBlockZ() + cube.getBlockZ();
//                        
//                        if(position.getBlockX() + sizeX > max.getBlockX()) {
//                            sizeX -= Math.abs(max.getBlockX() - (position.getBlockX() + sizeX));
//                        }
//                        
//                        if(position.getBlockY() + sizeY > max.getBlockY()) {
//                            sizeY -= Math.abs(max.getBlockY() - (position.getBlockY() + sizeY));
//                        }
//                        
//                        if(position.getBlockZ() + sizeZ > max.getBlockZ()) {
//                            sizeZ -= Math.abs(max.getBlockZ() - (position.getBlockZ() + sizeZ));
//                        }
//                    }
//                }
//            }
//        }
//        
//        
//    }
    
    
    
}
