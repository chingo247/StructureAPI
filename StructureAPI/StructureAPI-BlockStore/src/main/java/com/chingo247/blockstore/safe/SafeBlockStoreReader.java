/*
 * The MIT License
 *
 * Copyright 2016 Chingo.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.chingo247.blockstore.safe;

import com.chingo247.blockstore.BlockStoreReader;
import com.chingo247.blockstore.IBlockStore;
import com.chingo247.blockstore.IBlockStoreRegion;
import com.chingo247.blockstore.NBTUtils;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.Tag;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 *
 * @author Chingo
 */
public class SafeBlockStoreReader extends BlockStoreReader {

    @Override
    public SafeBlockStore read(File blockstoreDirectory) throws IOException {
        Map<String, Tag> metaData = readMetaDataTag(blockstoreDirectory);
        SafeBlockStore blockStore = new SafeBlockStore(
                blockstoreDirectory,
                NBTUtils.getChildTag(metaData, "Width", ShortTag.class).getValue(),
                NBTUtils.getChildTag(metaData, "Height", ShortTag.class).getValue(),
                NBTUtils.getChildTag(metaData, "Length", ShortTag.class).getValue()
        );
        return blockStore;
    }

    public IBlockStoreRegion readRegion(SafeBlockStore blockstore, File regionFile) throws IOException {
        return super.readRegion(blockstore, regionFile);
    }

    @Override
    public Map<String, Tag> readMetaDataTag(File blockstoreDirectory) throws IOException {
        return super.readMetaDataTag(blockstoreDirectory);
    }

    @Override
    public Map<String, Tag> readRegionTag(File regionFile) throws IOException {
        return super.readRegionTag(regionFile);
    }

    @Override
    protected IBlockStoreRegion makeRegion(IBlockStore blockStore, File regionFile, Map<String, Tag> regionMap) {
        int regionWidth = NBTUtils.getChildTag(regionMap, "Width", ShortTag.class).getValue();
        int regionHeight = NBTUtils.getChildTag(regionMap, "Height", ShortTag.class).getValue();
        int regionLength = NBTUtils.getChildTag(regionMap, "Length", ShortTag.class).getValue();
        return new SafeBlockStoreRegion((SafeBlockStore) blockStore, regionFile, regionMap, regionWidth, regionHeight, regionLength);
    }

}
