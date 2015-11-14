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
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.BlockType;
import java.util.Iterator;

/**
 *
 * @author Chingo
 */
public class DemolishingPlacement extends AbstractPlacement<DemolitionOptions> {
    

    public DemolishingPlacement(Vector size) {
        super(size.getBlockX(), size.getBlockY(), size.getBlockZ());
    }

    @Override
    public void place(EditSession session, Vector pos, DemolitionOptions option) {
        TopDownCuboidIterator cit = new TopDownCuboidIterator(
                option.getCubeX() < 0 ? getSize().getBlockX() : option.getCubeX(),
                option.getCubeY() < 0 ? getSize().getBlockY() : option.getCubeY(), 
                option.getCubeZ() < 0 ? getSize().getBlockZ() : option.getCubeZ()
        );
        Iterator<Vector> traversal = cit.iterate(getSize());
       
        while(traversal.hasNext()) {
            Vector relativePosition = traversal.next();
            Vector worldPosition = relativePosition.add(pos);
            BaseBlock currentBlock = session.getWorld().getBlock(worldPosition);
            
            if (currentBlock.isAir()  || currentBlock.getId() == BlockID.BEDROCK) {
                continue;
            }
            
            if (relativePosition.getBlockY() == 0 && !currentBlock.isAir()) {
                Vector wUnderPos = worldPosition.subtract(0, 1, 0);
                BaseBlock worldBlockUnder = session.getWorld().getBlock(wUnderPos);
            // replace the block with the block underneath you if it is a natural block
                if (BlockType.isNaturalTerrainBlock(worldBlockUnder)) {
                    session.rawSetBlock(worldPosition, worldBlockUnder);
                }
            } else {
                session.rawSetBlock(worldPosition, new BaseBlock(BlockID.AIR));
            }
        }
    }
    
    @Override
    public String getTypeName() {
        return PlacementTypes.DEMOLISHING;
    }

    
}
