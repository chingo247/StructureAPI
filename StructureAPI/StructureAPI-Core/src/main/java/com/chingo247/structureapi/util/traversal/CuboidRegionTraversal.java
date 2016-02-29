package com.chingo247.structureapi.util.traversal;

///*
// * Copyright (C) 2016 Chingo
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program.  If not, see <http://www.gnu.org/licenses/>.
// */
//package com.chingo247.structurecraft.util.traversal;
//
//import com.chingo247.structurecraft.util.RegionUtil;
//import com.google.common.collect.Lists;
//import com.sk89q.worldedit.BlockVector;
//import com.sk89q.worldedit.Vector;
//import com.sk89q.worldedit.Vector2D;
//import com.sk89q.worldedit.regions.CuboidRegion;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.List;
//import java.util.NoSuchElementException;
//
///**
// *
// * @author Chingo
// */
//public class CuboidRegionTraversal implements Iterator<Vector> {
//
//    private static final int CHUNK_SIZE = 16;
//
//    private Collection<CuboidRegion> regions;
//    private Iterator<CuboidRegion> regionIt;
//    private CuboidRegion currentRegion;
//    private RegionTraverser regionTraverser;
//    private Vector size;
//    private boolean reversed;
//    private boolean regionsTraversalDone = false;
//
//    private CuboidRegionTraversal(Collection<CuboidRegion> regions, Iterator<CuboidRegion> regionIt, CuboidRegion currentRegion, RegionTraverser regionTraverser, boolean reversed) {
//        this.regions = regions;
//        this.regionIt = regionIt;
//        this.currentRegion = currentRegion;
//        this.regionTraverser = regionTraverser;
//        this.reversed = reversed;
//    }
//
//    public CuboidRegionTraversal(Vector size) {
//        this(size, false);
//    }
//
//    public CuboidRegionTraversal(Vector size, boolean reversed) {
////        this.regions = getChunkCubes(new CuboidRegion(Vector.ZERO, size), CHUNK_SIZE, RegionUtil.ORDERED_XZ);
//        this.regionIt = regions.iterator();
//        this.reversed = reversed;
//    }
//
////    private static int fitsNeeded(int number, int roundTo) {
////        double needed = Math.ceil((double) number / roundTo);
////        return (int) needed;
////    }
////
////    private Collection<CuboidRegion> getChunkCubes(CuboidRegion area, int chunkSize, Comparator<Vector2D> sorter) {
////        List<CuboidRegion> subareas = Lists.newArrayList();
////
////        Vector size = RegionUtil.getSize(area);
////        Vector min = area.getMinimumPoint();
////
////        Vector max = area.getMinimumPoint()
////                .add(
////                        fitsNeeded(size.getBlockX(), chunkSize) * chunkSize,
////                        fitsNeeded(size.getBlockY(), chunkSize) * chunkSize,
////                        fitsNeeded(size.getBlockZ(), chunkSize) * chunkSize
////                );
////        
////        for(in)
////
////        for (Vector2D v : chunks) {
////            Vector minRel = new BlockVector(v.getBlockX() * CHUNK_SIZE, 0, v.getBlockZ() * CHUNK_SIZE).subtract(min).setY(0);
////            Vector maxRel = minRel.add(chunkSize, area.getMaximumY() - area.getMinimumY(), chunkSize);
////
//////            // Fit min positions
////            if (!area.contains(minRel)) {
////                minRel = minRel
////                        .setX(minRel.getBlockX() < areaMin.getBlockX() ? areaMin.getBlockX() : minRel.getBlockX())
////                        .setZ(minRel.getBlockZ() < areaMin.getBlockZ() ? areaMin.getBlockZ() : minRel.getBlockZ());
////            }
//////            
//////            // Fit max positions
////            if (!area.contains(maxRel)) {
////                maxRel = maxRel
////                        .setX(maxRel.getBlockX() > areaMax.getBlockX() ? areaMax.getBlockX() : maxRel.getBlockX())
////                        .setZ(maxRel.getBlockZ() > areaMax.getBlockZ() ? areaMax.getBlockZ() : maxRel.getBlockZ());
////
////            }
////
////            CuboidRegion subarea = new CuboidRegion(minRel, maxRel);
////            subareas.add(subarea);
////        }
////        return subareas;
////    }
//
//    @Override
//    public boolean hasNext() {
//        return !regionsTraversalDone;
//    }
//
//    @Override
//    public Vector next() {
//        Vector nextPos = null;
//
//        if ((regionTraverser == null || (regionTraverser != null && !regionTraverser.hasNext())) && regionIt.hasNext()) {
//            currentRegion = regionIt.next();
//            regionTraverser = new RegionTraverser();
//        }
//
//        if (regionTraverser != null && regionTraverser.hasNext()) {
//            nextPos = regionTraverser.next();
//        }
//
//        if (nextPos == null) {
//            throw new NoSuchElementException("No next element!");
//        }
//
//        if (regionTraverser == null || (regionTraverser != null && !regionTraverser.hasNext() && !regionIt.hasNext())) {
//            regionsTraversalDone = true;
//        }
//
//        return nextPos;
//    }
//
//    public CuboidRegionTraversal copy() {
//        Collection<CuboidRegion> copy = new HashSet<>();
//        CuboidRegion cloneCurrent = null;
//        for (Iterator<CuboidRegion> iterator = regions.iterator(); iterator.hasNext();) {
//
//            CuboidRegion next = iterator.next();
//            CuboidRegion clone = next.clone();
//            copy.add(clone);
//            if (clone.equals(currentRegion)) {
//                cloneCurrent = clone;
//            }
//        }
//
//        CuboidRegionTraversal cloneTraversal = new CuboidRegionTraversal(copy, copy.iterator(), cloneCurrent, regionTraverser.copy(), reversed);
//        return cloneTraversal;
//    }
//
//    private class RegionTraverser {
//
//        int x, y, z;
//        Vector min;
//        Vector max;
//        boolean done = false;
//
//        public RegionTraverser(int x, int y, int z, Vector min, Vector max, boolean done) {
//            this.x = x;
//            this.y = y;
//            this.z = z;
//            this.min = min;
//            this.max = max;
//            this.done = done;
//        }
//
//        public RegionTraverser() {
//            this.min = currentRegion.getMinimumPoint();
//            this.max = currentRegion.getMaximumPoint();
//            this.x = min.getBlockX();
//            this.y = min.getBlockY();
//            this.z = min.getBlockZ();
//        }
//
//        private RegionTraverser copy() {
//            return new RegionTraverser(x, y, z, min, max, done);
//        }
//
//        private boolean hasNext() {
//            return !done;
//        }
//
//        private Vector next() {
//            if (done != true) {
//                int currentX = x;
//                int currentY = y;
//                int currentZ = z;
//
//                x++;
//                if (x > max.getBlockX()) {
//                    x = 0;
//                    z++;
//
//                    if (z > max.getBlockZ()) {
//                        z = 0;
//
//                        if (!reversed) {
//                            y++;
//                        } else {
//                            y--;
//                        }
//
//                    }
//                }
//
//                if (!currentRegion.contains(new BlockVector(x, y, z))) {
//                    done = true;
//                }
//
//                return new BlockVector(currentX, currentY, currentZ);
//            } else {
//                throw new NoSuchElementException("No next element!");
//            }
//        }
//    }
//
//}
