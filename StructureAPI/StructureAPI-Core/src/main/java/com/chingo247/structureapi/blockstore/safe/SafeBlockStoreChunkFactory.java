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
package com.chingo247.structureapi.blockstore.safe;

import com.chingo247.structureapi.blockstore.IBlockStoreChunkFactory;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.Vector2D;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Chingo
 */
public class SafeBlockStoreChunkFactory implements IBlockStoreChunkFactory<ISafeBlockStoreChunk>{
    
    private SafeBlockStoreRegion blockStore;

    public SafeBlockStoreChunkFactory(SafeBlockStoreRegion SafeBlockStore) {
        this.blockStore = SafeBlockStore;
    }
    
    @Override
    public ISafeBlockStoreChunk newChunk(Tag sectionTagOrNull, int x, int z, Vector2D size) {
        ISafeBlockStoreChunk bsc;
        if (sectionTagOrNull == null) {
            bsc = new SafeBlockStoreChunk(blockStore, new HashMap<String, Tag>(), x, z, size);
        } else {
            bsc = new SafeBlockStoreChunk(blockStore, (Map) sectionTagOrNull.getValue(), x, z, size);
        }
        return bsc;
    }

    
}
