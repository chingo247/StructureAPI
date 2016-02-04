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
package com.chingo247.structurecraft.blockstore.safe;

import com.chingo247.structurecraft.blockstore.BlockStore;
import static com.chingo247.structurecraft.blockstore.BlockStore.ROOT_NODE;
import com.chingo247.structurecraft.blockstore.IBlockStoreChunkFactory;
import com.chingo247.structurecraft.blockstore.NBTUtils;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.NamedTag;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.regions.CuboidRegion;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author Chingo
 */
public class SafeBlockStore extends BlockStore {

    private final SafeBlockStoreChunkFactory chunkFactory;

    public SafeBlockStore(File file, Map<String, Tag> root, int width, int height, int length) {
        super(file, root, width, height, length);

        this.chunkFactory = new SafeBlockStoreChunkFactory(this);
    }

    public SafeBlockStore(File file, CuboidRegion region) {
        super(file, region);

        this.chunkFactory = new SafeBlockStoreChunkFactory(this);
    }

    public SafeBlockStore(File file, int width, int height, int length) {
        super(file, width, height, length);

        this.chunkFactory = new SafeBlockStoreChunkFactory(this);
    }

    @Override
    public IBlockStoreChunkFactory getChunkFactory() {
        return chunkFactory;
    }

    public static SafeBlockStore load(File f) throws IOException {
        try (NBTInputStream nbtStream = new NBTInputStream(new GZIPInputStream(new FileInputStream(f)))) {
            NamedTag root = nbtStream.readNamedTag();
            if (!root.getName().equals(ROOT_NODE)) {
                throw new RuntimeException("File not of type '" + ROOT_NODE + "'");
            }

            Map<String, Tag> rootMap = (Map) root.getTag().getValue();

            int width = NBTUtils.getChildTag(rootMap, "Width", ShortTag.class).getValue();
            int height = NBTUtils.getChildTag(rootMap, "Height", ShortTag.class).getValue();
            int length = NBTUtils.getChildTag(rootMap, "Length", ShortTag.class).getValue();

            SafeBlockStore blockStore = new SafeBlockStore(f, rootMap, width, height, length);
            return blockStore;
        }

    }

}
