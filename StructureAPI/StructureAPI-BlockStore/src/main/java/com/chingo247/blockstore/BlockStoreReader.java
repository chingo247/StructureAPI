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
package com.chingo247.blockstore;

import static com.chingo247.blockstore.BlockStore.EXTENSION;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author Chingo
 */
public class BlockStoreReader implements IBlockStoreReader<BlockStore> {

    @Override
    public BlockStore read(File blockstoreDirectory) throws IOException {
       Map<String, Tag> metaData = readMetaDataTag(blockstoreDirectory);
       BlockStore blockStore = new BlockStore(
               blockstoreDirectory, 
               NBTUtils.getChildTag(metaData, "Width", ShortTag.class).getValue(), 
               NBTUtils.getChildTag(metaData, "Height", ShortTag.class).getValue(), 
               NBTUtils.getChildTag(metaData, "Length", ShortTag.class).getValue()
       );
       return blockStore;
    }
    
    public final File getMetaDataFile(File blockstoreDirectory) {
        return new File(blockstoreDirectory, blockstoreDirectory.getName() + ".meta" + EXTENSION);
    }
    
    @Override
    public Map<String, Tag> readMetaDataTag(File blockstoreDirectory) throws IOException {
        File metaDataFile = getMetaDataFile(blockstoreDirectory);
        if(!metaDataFile.exists()) {
            throw new FileNotFoundException("Missing blockstore meta in '" + blockstoreDirectory.getAbsolutePath()+ "'");
        }
        
        Map<String, Tag> rootMeta;
        try (NBTInputStream input = new NBTInputStream(new GZIPInputStream(new FileInputStream(metaDataFile)))) {
            rootMeta = (Map) input.readNamedTag().getTag().getValue();
        }
        return rootMeta;
    }
    

    @Override
    public IBlockStoreRegion readRegion(IBlockStore blockstore, File regionFile) throws IOException {
        String fileName = regionFile.getName();
        if (!fileName.contains(".r.")) {
            throw new RuntimeException("File '" + regionFile.getName() + "' is not a blockstore region file.");
        }
        Map<String, Tag> regionMap = readRegionTag(regionFile);
        return makeRegion(blockstore, regionFile, regionMap);
    }
    
    

    @Override
    public Map<String, Tag> readRegionTag(File regionFile) throws IOException {
        try (NBTInputStream input = new NBTInputStream(new GZIPInputStream(new FileInputStream(regionFile)))) {
            Map<String, Tag> region = (Map) input.readNamedTag().getTag().getValue();
            return region;
        }
    }

    protected IBlockStoreRegion makeRegion(IBlockStore blockStore, File regionFile, Map<String, Tag> regionMap) {
        int regionWidth = NBTUtils.getChildTag(regionMap, "Width", ShortTag.class).getValue();
        int regionHeight = NBTUtils.getChildTag(regionMap, "Height", ShortTag.class).getValue();
        int regionLength = NBTUtils.getChildTag(regionMap, "Length", ShortTag.class).getValue();
        return new BlockStoreRegion(blockStore, regionFile, regionMap, regionWidth, regionHeight, regionLength);
    }
    
}
