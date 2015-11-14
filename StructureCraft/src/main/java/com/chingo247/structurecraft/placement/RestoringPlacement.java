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

package com.chingo247.structurecraft.placement;

import com.chingo247.structurecraft.construction.options.DemolitionOptions;
import com.chingo247.structurecraft.util.iterator.TopDownCuboidIterator;
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
    public void place(EditSession editSession, Vector pos, DemolitionOptions option) {
        
        
        
        
        Iterator<Vector> traversal = new TopDownCuboidIterator(option.getCubeX(), option.getCubeY(), option.getCubeZ()).iterate(parent.getSize());
        
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
