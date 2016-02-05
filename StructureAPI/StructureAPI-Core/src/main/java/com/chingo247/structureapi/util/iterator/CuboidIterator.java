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
package com.chingo247.structureapi.util.iterator;

import com.chingo247.structureapi.util.traversal.CuboidTraversal;
import com.sk89q.worldedit.Vector;
import java.util.Iterator;

/**
 *
 * @author Chingo
 */
public class CuboidIterator implements AreaIterator {
    
    private int cubeX;
    private int cubeY;
    private int cubeZ;

    public CuboidIterator(int cubeX, int cubeY, int cubeZ) {
        this.cubeX = cubeX;
        this.cubeY = cubeY;
        this.cubeZ = cubeZ;
    }

    @Override
    public Iterator<Vector> iterate(Vector size) {
        return iterate(size, false);
    }
    
    public Iterator<Vector> iterate(Vector size, boolean reversed) {
         
        if(cubeX < 1) cubeX = size.getBlockX();
        if(cubeY < 1) cubeY = size.getBlockY();
        if(cubeZ < 1) cubeZ = size.getBlockZ();
        
        
        int cubx = Math.min(size.getBlockX(), cubeX);
        int cuby = Math.min(size.getBlockY(), cubeY);
        int cubz = Math.min(size.getBlockZ(), cubeZ);
        return new CuboidTraversal(size, cubx, cuby, cubz, reversed);
    }
    
}
