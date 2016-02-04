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
package com.chingo247.structurecraft.placement;

import com.chingo247.structurecraft.blockstore.IBlockStore;
import com.chingo247.structurecraft.blockstore.IBlockStoreChunk;
import com.chingo247.structurecraft.placement.block.BlockPlacement;
import com.chingo247.structurecraft.placement.options.PlaceOptions;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import java.util.Iterator;

/**
 *
 * @author Chingo
 */
public class BlockStorePlacement extends BlockPlacement {
    
    private final IBlockStore blockStore;
    private boolean reversed;

    public BlockStorePlacement(IBlockStore blockStore) {
        super(blockStore.getWidth(), blockStore.getHeight(), blockStore.getLength());
        
        this.reversed = false;
        this.blockStore = blockStore;
    }

    public void setReversed(boolean reversed) {
        this.reversed = reversed;
    }
    
    @Override
    public BaseBlock getBlock(Vector position) {
        return blockStore.getBlockAt(position);
    }

    @Override
    public void place(EditSession editSession, Vector pos, PlaceOptions option) {
        Iterator<IBlockStoreChunk> chunkIt = blockStore.iterator();
        
        while(chunkIt.hasNext()) {
            IBlockStoreChunk next = chunkIt.next();
            
            if(!reversed) {
                for(int y = 0; y < blockStore.getHeight(); y++) {
                    for(int x = 0; x < next.getWidth(); x++) {
                        for(int z = 0; z < next.getLength(); z++) {
                            Vector blockposition = new BlockVector(x, y, z);
                            BaseBlock b = next.getBlockAt(x, y, z);
                            
                            if(b != null) {
                                doBlock(editSession, pos, blockposition, b, option);
                            }
                        }
                    }
                }
            } else {
                for(int y = blockStore.getHeight() - 1; y >= 0; y--) {
                    for(int x = 0; x < next.getWidth(); x++) {
                        for(int z = 0; z < next.getLength(); z++) {
                            Vector blockposition = new BlockVector(x, y, z);
                            BaseBlock b = next.getBlockAt(x, y, z);
                            
                            if(b != null) {
                                doBlock(editSession, pos, blockposition, b, option);
                            }
                        }
                    }
                }
            }
            
        }
        
        
    }
    
    
    
    
    
}
