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
package com.chingo247.structureapi.placement;

import com.chingo247.blockstore.IBlockStoreChunk;
import com.chingo247.structureapi.placement.block.BlockPlacement;
import com.chingo247.structureapi.placement.options.PlaceOptions;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;

/**
 *
 * @author Chingo
 */
public class BlockStoreChunkPlacement extends BlockPlacement {

    private IBlockStoreChunk blockStoreChunk;
    private boolean reversed;

    public BlockStoreChunkPlacement(IBlockStoreChunk blockStoreChunk) {
        super(blockStoreChunk.getWidth(), blockStoreChunk.getBlockStore().getHeight(), blockStoreChunk.getLength());
        
        this.blockStoreChunk = blockStoreChunk;
        this.reversed = false;
        
    }

    public void setReversed(boolean reversed) {
        this.reversed = reversed;
    }

    @Override
    public BaseBlock getBlock(Vector position) {
        return blockStoreChunk.getBlockAt(position);
    }

    @Override
    public void place(EditSession editSession, Vector pos, PlaceOptions option) {
        if (!reversed) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    for (int z = 0; z < length; z++) {
                        Vector blockPosition = new BlockVector(x, y, z);
                        BaseBlock block = blockStoreChunk.getBlockAt(x, y, z);
                        if (block != null) {
                            Vector p = pos.add(blockPosition);
                            editSession.rawSetBlock(p, block);
                        }
                    }
                }
            }
        } else {
            for (int y = height - 1; y >= 0; y--) {
                for (int x = 0; x < width; x++) {
                    for (int z = 0; z < length; z++) {
                        Vector blockPosition = new BlockVector(x, y, z);
                        BaseBlock block = blockStoreChunk.getBlockAt(x, y, z);
                        if (block != null) {
                            Vector p = pos.add(blockPosition);
                            editSession.rawSetBlock(p, block);
                        }
                    }
                }
            }
        }

    }
    
}
