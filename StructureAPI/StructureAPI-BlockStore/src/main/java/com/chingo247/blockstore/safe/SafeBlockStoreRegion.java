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
package com.chingo247.blockstore.safe;

import com.chingo247.blockstore.BlockStoreRegion;
import com.chingo247.blockstore.IBlockStoreChunkFactory;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.regions.CuboidRegion;
import java.io.File;
import java.util.Map;

/**
 *
 * @author Chingo
 */
public class SafeBlockStoreRegion extends BlockStoreRegion {

    private final SafeBlockStoreChunkFactory chunkFactory;

    public SafeBlockStoreRegion(SafeBlockStore blockStore, File file, Map<String, Tag> root, int width, int height, int length) {
        super(blockStore, file, root, width, height, length);

        this.chunkFactory = new SafeBlockStoreChunkFactory(this);
    }

    public SafeBlockStoreRegion(SafeBlockStore blockStore, File file, CuboidRegion region) {
        super(blockStore, file, region);

        this.chunkFactory = new SafeBlockStoreChunkFactory(this);
    }

    public SafeBlockStoreRegion(SafeBlockStore blockStore, File file, int width, int height, int length) {
        super(blockStore, file, width, height, length);

        this.chunkFactory = new SafeBlockStoreChunkFactory(this);
    }

    @Override
    public IBlockStoreChunkFactory getChunkFactory() {
        return chunkFactory;
    }

   

}
