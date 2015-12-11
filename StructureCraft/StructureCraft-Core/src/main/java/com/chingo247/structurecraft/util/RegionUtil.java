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
package com.chingo247.structurecraft.util;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.regions.CuboidRegion;
import java.util.Comparator;

/**
 *
 * @author Chingo
 */
public class RegionUtil {

    
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
        CuboidRegion p = new CuboidRegion(parent.getMinimumPoint(), parent.getMaximumPoint());
        CuboidRegion c = new CuboidRegion(child.getMinimumPoint(), child.getMaximumPoint());
        return p.getMinimumPoint().getBlockX() > c.getMinimumPoint().getBlockX()
                && p.getMinimumY() >= c.getMinimumY()
                && p.getMinimumPoint().getBlockZ() > c.getMinimumPoint().getBlockZ()
                && p.getMaximumPoint().getBlockX() < c.getMaximumPoint().getBlockX()
                && p.getMaximumPoint().getBlockY() < c.getMaximumPoint().getBlockY()
                && p.getMaximumPoint().getBlockZ() < c.getMaximumPoint().getBlockZ();

    }

    /**
     * Checks whether two CuboidDimensionals overlap each other
     *
     * @param dimensionalA The dimensional
     * @param dimensianalB The other dimensional
     * @return True if the two CuboidDimensionals overlap each other
     */
    public static boolean overlaps(CuboidRegion dimensionalA, CuboidRegion dimensianalB) {
        CuboidRegion p = new CuboidRegion(dimensionalA.getMinimumPoint(), dimensionalA.getMaximumPoint());
        CuboidRegion c = new CuboidRegion(dimensianalB.getMinimumPoint(), dimensianalB.getMaximumPoint());

        Vector pMax = p.getMaximumPoint();
        Vector pMin = p.getMinimumPoint();

        Vector cMax = c.getMaximumPoint();
        Vector cMin = c.getMinimumPoint();

        return pMax.getBlockX() >= cMin.getBlockX() && pMin.getBlockX() <= cMax.getBlockX()
                && pMax.getBlockY() >= cMin.getBlockY() && pMin.getBlockY() <= cMax.getBlockY()
                && pMax.getBlockZ() >= cMin.getBlockZ() && pMin.getBlockZ() <= cMax.getBlockZ();
    }

    public static Vector getSize(CuboidRegion region) {
        return region.getMaximumPoint().subtract(region.getMinimumPoint()).add(Vector.ONE);
    }

}
