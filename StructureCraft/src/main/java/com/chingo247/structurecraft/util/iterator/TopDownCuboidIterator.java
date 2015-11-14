/*
 * Copyright (C) 2015 Chingo
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
package com.chingo247.structurecraft.util.iterator;

import com.chingo247.structurecraft.util.traversal.TopDownCuboidTraversal;
import com.sk89q.worldedit.Vector;
import java.util.Iterator;

/**
 *
 * @author Chingo
 */
public class TopDownCuboidIterator implements AreaIterator {
    
    private int cubeX;
    private int cubeY;
    private int cubeZ;

    public TopDownCuboidIterator() {
        this.cubeX = 16;
        this.cubeY = 16;
        this.cubeZ = 16;
    }
    
    public TopDownCuboidIterator(int cubeX, int cubeY, int cubeZ) {
        this.cubeX = cubeX;
        this.cubeY = cubeY;
        this.cubeZ = cubeZ;
    }
    

    @Override
    public Iterator<Vector> iterate(Vector size) {
        
        if(cubeX < 1) cubeX = size.getBlockX();
        if(cubeY < 1) cubeY = size.getBlockY();
        if(cubeZ < 1) cubeZ = size.getBlockZ();
        
        
        int cubx = Math.min(size.getBlockX(), cubeX);
        int cuby = Math.min(size.getBlockX(), cubeY);
        int cubz = Math.min(size.getBlockX(), cubeZ);
        
        return new TopDownCuboidTraversal(size, cubx, cuby, cubz);
    }
    
}
