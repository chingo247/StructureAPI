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

package com.chingo247.structureapi.placement.block;

import com.chingo247.structureapi.construction.engine.BlockPlaceSession;
import com.chingo247.structureapi.placement.options.PlaceOptions;
import com.chingo247.structureapi.util.iterator.CuboidIterator;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import java.util.Iterator;

/**
 *
 * @author Chingo
 */
public class RestoringPlacement extends DemolishingPlacement {
    
    private final BlockPlacement parent;

    public RestoringPlacement(BlockPlacement parent) {
        super(parent.getCuboidRegion().getMaximumPoint().subtract(parent.getCuboidRegion().getMinimumPoint()).add(1, 1, 1));
        this.parent = parent;
    }
    
    
    @Override
    public void place(BlockPlaceSession editSession, Vector pos, PlaceOptions option) {
         Iterator<Vector> traversal = new CuboidIterator(
                option.getCubeX() <= 0 ? getSize().getBlockX() : option.getCubeX(),
                option.getCubeY() <= 0 ? getSize().getBlockY() : option.getCubeY(),
                option.getCubeZ() <= 0 ? getSize().getBlockZ() : option.getCubeZ()
        ).iterate(getSize(), true);
        
        while(traversal.hasNext()) {
            Vector blockPosition = traversal.next();
            BaseBlock nextBlock = parent.getBlock(blockPosition);
            
            if (nextBlock == null) {
                continue;
            }
            
            
            
            parent.doBlock(editSession, pos, blockPosition, nextBlock, option);
        }
        
    }
    
}
