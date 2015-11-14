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
package com.chingo247.structurecraft.util.traversal;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import java.util.Iterator;

/**
 *
 * @author Chingo
 */
public class CuboidRegionTraversal implements Iterator<CuboidRegion>, Iterable<CuboidRegion>{

    private final CuboidRegion region;
    private Vector currentStart, currentEnd;
    private int minX, minY, minZ;
    private final int xRange, yRange, zRange;

    public CuboidRegionTraversal(CuboidRegion region, int xRange, int yRange, int zRange) {
        Preconditions.checkNotNull(region);
        this.region = region;
        this.minX = this.region.getMinimumPoint().getBlockX();
        this.minY = this.region.getMinimumPoint().getBlockY();
        this.minZ = this.region.getMinimumPoint().getBlockZ();
        this.xRange = xRange;
        this.yRange = yRange;
        this.zRange = zRange;
    }
    
    /**
     * Copy constructor
     * @param dimension
     * @param currentStart
     * @param currentEnd
     * @param xRange
     * @param yRange
     * @param zRange 
     */
    private CuboidRegionTraversal(CuboidRegion dimension, Vector currentStart, Vector currentEnd, int xRange, int yRange, int zRange, int minX, int minY, int minZ) {
        this.region = dimension;
        this.currentStart = currentStart;
        this.currentEnd = currentEnd;
        this.xRange = xRange;
        this.yRange = yRange;
        this.zRange = zRange;
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
    }

    public CuboidRegion getDimension() {
        return region;
    }
    
    @Override
    public CuboidRegion next() {
        currentStart = new Vector(minX, minY, minZ);
        currentEnd = new Vector(minX + xRange, minY + yRange, minZ + zRange);
        
        if(minY > region.getMaximumY()) {
            return null;
        }

        fitEnd();
        minX += xRange + 1;
        
        if(minX > region.getMaximumY()) {
            minX = region.getMinimumPoint().getBlockX();
            minZ+= zRange + 1;
            
            if(minZ > region.getMaximumPoint().getBlockZ()) {
                minZ = region.getMinimumPoint().getBlockZ();
                minY += yRange + 1;
            }
        }
        return new CuboidRegion(currentStart, currentEnd);
    }

    /**
     * Makes the end fit
     */
    private void fitEnd() {
        if(isInside(currentEnd)) return;
        int x = currentEnd.getBlockX(), y = currentEnd.getBlockY(), z = currentEnd.getBlockZ();
        if (currentEnd.getBlockX() > region.getMaximumPoint().getBlockX()) {
            x = region.getMaximumPoint().getBlockX();
        }
        if (currentEnd.getBlockY() > region.getMaximumPoint().getBlockY()) {
            y = region.getMaximumPoint().getBlockY();
        }
        if (currentEnd.getBlockZ() > region.getMaximumPoint().getBlockZ()) {
            z = region.getMaximumPoint().getBlockZ();
        }
        currentEnd = new Vector(x, y, z);
    }

    private boolean isInside(Vector v) {
        return region.contains(v);
//        return v.getBlockX() < region.getMaxX() && v.getBlockX() > region.getMinX()
//                && v.getBlockY() < region.getMaxY() && v.getBlockY() > region.getMinY()
//                && region.getMaxZ() < region.getMaxZ() && v.getBlockZ() > region.getMinZ();
    }
    
    public CuboidRegionTraversal copy() {
        return new CuboidRegionTraversal(region, currentStart, currentEnd, xRange, yRange, zRange, minX, minY, minZ);
    }
    
    @Override
    public boolean hasNext() {
        return this.copy().next() != null;
    }

    @Override
    public Iterator<CuboidRegion> iterator() {
        return this;
    }

    /**
     * Does nothing at the moment
     */
    @Override
    public void remove() {
        
    }

}
