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
package com.chingo247.structureapi.blockstore;

import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.Vector2D;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Chingo
 */
public class BlockStoreChunkFactory implements IBlockStoreChunkFactory<IBlockStoreChunk> {
    
    private BlockStore blockstore;

    public BlockStoreChunkFactory(BlockStore blockstore) {
        this.blockstore = blockstore;
    }
    
    @Override
    public IBlockStoreChunk newChunk( Tag sectionTagOrNull, int x, int z, Vector2D size) {
        BlockStoreChunk bsc;
        if (sectionTagOrNull == null) {
            bsc = new BlockStoreChunk(blockstore, new HashMap<String, Tag>(), x, z, size);
        } else {
            bsc = new BlockStoreChunk(blockstore, (Map) sectionTagOrNull.getValue(), x, z, size);
        }
        return bsc;
    }

}
