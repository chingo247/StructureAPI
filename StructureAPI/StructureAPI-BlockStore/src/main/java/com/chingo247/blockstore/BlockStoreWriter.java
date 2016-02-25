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
import com.google.common.collect.Maps;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.NBTOutputStream;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author Chingo
 */
public class BlockStoreWriter implements IBlockStoreWriter<BlockStore>{

    @Override
    public void save(BlockStore blockstore) throws IOException {
        save(blockstore, blockstore.getDirectory());
    }

    @Override
    public void save(BlockStore blockstore, File directory) throws IOException {
        directory.mkdirs();
        saveMetaData(blockstore);
        for(Iterator<IBlockStoreRegion> regionIt = blockstore.getLoadedRegions().iterator(); regionIt.hasNext();) {
            IBlockStoreRegion next = regionIt.next();
//            System.out.println("SAVE REGION: " + next.getSize()); 
            saveRegion(next, directory);
        }
    }

    @Override
    public void saveMetaData(BlockStore blockstore) throws IOException {
        String metaName = blockstore.getName() + ".meta" + EXTENSION;
        Map<String, Tag> metaRoot = serializeMetaData(blockstore);
        try (NBTOutputStream output = new NBTOutputStream(new GZIPOutputStream(new FileOutputStream(new File(blockstore.getDirectory(), metaName))))) {
            output.writeNamedTag("blockstore.meta", new CompoundTag(metaRoot));
        }
    }
    
    @Override
    public Map<String, Tag> serializeMetaData(BlockStore blockStore) {
        Map<String, Tag> root = Maps.newHashMap();
        root.put("Width", new ShortTag((short) blockStore.getWidth()));
        root.put("Height", new ShortTag((short) blockStore.getHeight()));
        root.put("Length", new ShortTag((short) blockStore.getLength()));
        root.put("Version", new StringTag(blockStore.getVersion()));
        root.put("ChunkSize", new ShortTag((short) blockStore.getChunkSize()));
        return root;
    }

    @Override
    public void saveRegion(IBlockStoreRegion region, File directory) throws IOException {
//        System.out.println("REGION DIRTY ? " + region.isDirty());
        if(region.isDirty()) {
            region.save(directory);
        }
    }
    
    @Override
    public void saveRegion(IBlockStoreRegion region) throws IOException {
        saveRegion(region, region.getBlockStore().getDirectory());
    }
    
}
