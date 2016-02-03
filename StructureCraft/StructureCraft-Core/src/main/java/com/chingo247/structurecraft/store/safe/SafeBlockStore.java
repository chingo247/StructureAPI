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
package com.chingo247.structurecraft.store.safe;

import com.chingo247.structurecraft.store.BlockStore;
import com.chingo247.structurecraft.store.IBlockStoreChunkFactory;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import java.io.File;
import java.util.Map;

/**
 *
 * @author Chingo
 */
public class SafeBlockStore extends BlockStore {
    
    private final SafeBlockStoreChunkFactory chunkFactory;

    public SafeBlockStore(File file, Map<String, Tag> root, Vector size) {
        super(file, root, size);
        
        this.chunkFactory = new SafeBlockStoreChunkFactory(this);
    }

    public SafeBlockStore(File file, CuboidRegion region) {
        super(file, region);
        
        this.chunkFactory = new SafeBlockStoreChunkFactory(this);
    }

    public SafeBlockStore(File file, Vector size) {
        super(file, size);
        
        this.chunkFactory = new SafeBlockStoreChunkFactory(this);
    }

    @Override
    public IBlockStoreChunkFactory getChunkFactory() {
        return chunkFactory;
    }
    
    
    
    
}
