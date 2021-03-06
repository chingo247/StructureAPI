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
package com.chingo247.structureapi.util;

import com.chingo247.structureapi.StructureAPI;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.World;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chingo
 */
public class BlockDrawer {
    
    private BlockDrawer() {}
    
    public static void visualize(World world, CuboidRegion region, BaseBlock b) {
        EditSession session = StructureAPI.getInstance().getSessionFactory().getEditSession(world, -1);
        try {
            session.setBlocks(region, b);
        } catch (MaxChangedBlocksException ex) {
            Logger.getLogger(BlockDrawer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void visualizeBottom(World world, CuboidRegion region, BaseBlock b) {
        EditSession session = StructureAPI.getInstance().getSessionFactory().getEditSession(world, -1);
        Vector min = region.getMinimumPoint();
        Vector max = region.getMaximumPoint();
        CuboidRegion bottom = new CuboidRegion(min, new BlockVector(max.getBlockX(), min.getBlockY() + 1, max.getBlockZ()));
        try {
            session.setBlocks(bottom, b);
        } catch (MaxChangedBlocksException ex) {
            Logger.getLogger(BlockDrawer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
