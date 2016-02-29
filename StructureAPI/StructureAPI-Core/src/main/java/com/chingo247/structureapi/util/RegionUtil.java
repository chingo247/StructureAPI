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
package com.chingo247.structureapi.util;

import com.google.common.collect.Lists;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.regions.CuboidRegion;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author Chingo
 */
public class RegionUtil {

    private static final int CHUNK_SIZE = 16;
    
    public static final Comparator<Vector2D> ORDERED_XZ = new Comparator<Vector2D>() {

        @Override
        public int compare(Vector2D t, Vector2D t1) {
            int v = Integer.compare(t.getBlockZ(), t1.getBlockZ());
            if (v == 0) {
                return Integer.compare(t.getBlockX(), t1.getBlockX());
            }
            return v;
        }
    };

    private RegionUtil() {
    }

    public static boolean isDimensionWithin(CuboidRegion parent, CuboidRegion child) {
        return parent.contains(child.getMinimumPoint()) && parent.contains(child.getMaximumPoint());
    }

    /**
     * Checks whether two CuboidDimensionals overlap each other
     *
     * @param dimensionalA The dimensional
     * @param dimensional The other dimensional
     * @return True if the two CuboidDimensionals overlap each other
     */
    public static boolean overlaps(CuboidRegion dimensionalA, CuboidRegion dimensional) {
        CuboidRegion p = new CuboidRegion(dimensionalA.getMinimumPoint(), dimensionalA.getMaximumPoint());
        CuboidRegion c = new CuboidRegion(dimensional.getMinimumPoint(), dimensional.getMaximumPoint());

        Vector pMax = p.getMaximumPoint();
        Vector pMin = p.getMinimumPoint();

        Vector cMax = c.getMaximumPoint();
        Vector cMin = c.getMinimumPoint();

        return pMax.getBlockX() >= cMin.getBlockX() && pMin.getBlockX() <= cMax.getBlockX()
                && pMax.getBlockY() >= cMin.getBlockY() && pMin.getBlockY() <= cMax.getBlockY()
                && pMax.getBlockZ() >= cMin.getBlockZ() && pMin.getBlockZ() <= cMax.getBlockZ();
    }

    public static Vector getSize(CuboidRegion region) {
        return region.getMaximumPoint().subtract(region.getMinimumPoint()).add(1, 1, 1);
    }

    public static Collection<CuboidRegion> getChunkCubes(CuboidRegion area, int chunkSize) {
        return getChunkCubes(area, chunkSize, null);
    }
    
    public static Collection<CuboidRegion> getChunkCubes(CuboidRegion area, int chunkSize, Comparator<Vector2D> sorter) {
        List<CuboidRegion> subareas = Lists.newArrayList();
        Vector min = area.getMinimumPoint();

        List<Vector2D> chunks = new ArrayList<>(area.getChunks());
        if(sorter != null) {
            Collections.sort(chunks, sorter);
        }

        Vector areaMin = Vector.ZERO;
        Vector areaMax = RegionUtil.getSize(area).subtract(Vector.ONE);

        for (Vector2D v : chunks) {
            Vector minRel = new BlockVector(v.getBlockX() * CHUNK_SIZE, 0, v.getBlockZ() * CHUNK_SIZE).subtract(min).setY(0);
            Vector maxRel = minRel.add(chunkSize, area.getMaximumY() - area.getMinimumY(), chunkSize);

//            // Fit min positions
            if (!area.contains(minRel)) {
                minRel = minRel
                        .setX(minRel.getBlockX() < areaMin.getBlockX() ? areaMin.getBlockX() : minRel.getBlockX())
                        .setZ(minRel.getBlockZ() < areaMin.getBlockZ() ? areaMin.getBlockZ() : minRel.getBlockZ());
            }
//            
//            // Fit max positions
            if (!area.contains(maxRel)) {
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
