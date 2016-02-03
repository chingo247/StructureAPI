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
package com.chingo247.structurecraft.store;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.NBTOutputStream;
import com.sk89q.jnbt.NamedTag;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.regions.CuboidRegion;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author Chingo
 */
public class BlockStore implements IBlockStore {

    private static final Logger LOG = Logger.getLogger(BlockStore.class.getName());

    public static final String ROOT_NODE = "BlockStore";
    public static final int DEFAULT_SIZE = 16;

    private final Vector size;
    protected Map<String, Tag> chunkTags;
    protected Map<String, IBlockStoreChunk> chunks;
    protected File file;

    private BlockStoreChunkFactory chunkFactory;
    
    public BlockStore(File file, CuboidRegion region) {
        this(file, region.getMaximumPoint().subtract(region.getMinimumPoint()).add(1, 1, 1));
    }

    public BlockStore(File file, Vector size) {
        this(file, new HashMap<String, Tag>(), size);
    }

    protected BlockStore(File file, Map<String, Tag> root, Vector size) {
        Preconditions.checkArgument(size.getBlockX() > 0, "Vector x has to be > 0");
        Preconditions.checkArgument(size.getBlockY() > 0, "Vector y has to be > 0");
        Preconditions.checkArgument(size.getBlockZ() > 0, "Vector z has to be > 0");

        this.size = size;
        this.chunks = Maps.newHashMap();
        this.chunkTags = root;
        this.file = file;
        this.chunkFactory = new BlockStoreChunkFactory(this);
    }

    public IBlockStoreChunkFactory getChunkFactory() {
        return chunkFactory;
    }

    @Override
    public BaseBlock getBlockAt(int x, int y, int z) {
        IBlockStoreChunk chunk = getChunk(x, z);
        BaseBlock b = null;
        if (chunk != null) {
            int chunkX = (x << 4) * 16;
            int chunkZ = (z << 4) * 16;
            b = chunk.getBlockAt(x - chunkX, y, z - chunkZ);
        }
        return b;
    }

    @Override
    public BaseBlock getBlockAt(Vector position) {
        return getBlockAt(position.getBlockX(), position.getBlockY(), position.getBlockZ());
    }

    @Override
    public void setBlockAt(int x, int y, int z, BaseBlock b) {
        checkPosition(x, y, z);
        IBlockStoreChunk chunk = getChunk(x, z);
        int chunkX = (x << 4) * 16;
        int chunkZ = (z << 4) * 16;
        chunk.setBlockAt(x - chunkX, y, z - chunkZ, b);
    }

    @Override
    public void setBlockAt(Vector position, BaseBlock block) {
        setBlockAt(position.getBlockX(), position.getBlockY(), position.getBlockZ(), block);
    }

    private void checkPosition(int x, int y, int z) throws IndexOutOfBoundsException {
        if (x < 0) {
            throw new IndexOutOfBoundsException("x < 0: x was " + x);
        }
        if (y < 0) {
            throw new IndexOutOfBoundsException("y < 0: y was " + y);
        }
        if (z < 0) {
            throw new IndexOutOfBoundsException("z < 0: z was " + z);
        }
        if (x > 0) {
            throw new IndexOutOfBoundsException("x > " + size.getBlockX() + ": x was " + x);
        }
        if (y > 0) {
            throw new IndexOutOfBoundsException("y > " + size.getBlockY() + ": y was " + y);
        }
        if (z > 0) {
            throw new IndexOutOfBoundsException("z > " + size.getBlockZ() + ": z was " + z);
        }
    }

    @Override
    public int getWidth() {
        return size.getBlockX();
    }

    @Override
    public int getLength() {
        return size.getBlockZ();
    }

    @Override
    public int getHeight() {
        return size.getBlockY();
    }

    @Override
    public Vector getSize() {
        return size;
    }

    protected final String getChunkKey(int x, int z) {
        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        return "Chunk-[" + chunkX + "," + chunkZ + "]";
    }

    public IBlockStoreChunk getChunk(int x, int z) {
        String key = getChunkKey(x, z);
        IBlockStoreChunk bsc = chunks.get(key);
        if (bsc == null) {
            Tag chunkTag = chunkTags.get(key);
            
            int chunkX = (x << 4) * 16;
            int chunkZ = (z << 4) * 16;
            
            int width; 
            if(chunkTag == null) {
                width = chunkX + 16 > getWidth() ? (getWidth() - chunkX) : DEFAULT_SIZE;
            } else {
                Map<String,Tag> map = (Map)chunkTag.getValue();
                if(map.containsKey("Width")) {
                    Tag widthTag = map.get("Width");
                    width = (short) widthTag.getValue();
                } else {
                    width = DEFAULT_SIZE;
                }
            }
            
            if(width <= 0) {
                throw new RuntimeException("Width was <= 0");
            }
            
            int length; 
            if(chunkTag == null) {
                length = chunkZ + 16 > getLength()? (getLength() - chunkZ) : DEFAULT_SIZE;
            } else {
                Map<String,Tag> map = (Map)chunkTag.getValue();
                if(map.containsKey("Length")) {
                    Tag lengthTag = map.get("Length");
                    length = (short) lengthTag.getValue();
                } else {
                    length = DEFAULT_SIZE;
                }
            }
            
            if(length <= 0) {
                throw new RuntimeException("Length was <= 0");
            }
            
            this.getChunkFactory().newChunk(chunkTag, chunkX, chunkZ, new Vector2D(width, length));
            this.chunks.put(key, bsc);
        }
        return bsc;
    }
    
    @Override
    public void save() throws IOException  {
        Map<String, Tag> rootMap = Maps.newHashMap();
        Set<Entry<String,IBlockStoreChunk>> chunkSet = chunks.entrySet();
        for (Iterator<Entry<String, IBlockStoreChunk>> iterator = chunkSet.iterator(); iterator.hasNext();) {
            Entry<String, IBlockStoreChunk> next = iterator.next();
            rootMap.put(next.getKey(), new CompoundTag(next.getValue().serialize()));
        }
        
        try(NBTOutputStream outputStream = new NBTOutputStream(new GZIPOutputStream(new FileOutputStream(file)))) {
            outputStream.writeNamedTag("BlockStore", new CompoundTag(rootMap));
        }
    }
    
    public static BlockStore load(File f) throws IOException {
        try (NBTInputStream nbtStream = new NBTInputStream(new GZIPInputStream(new FileInputStream(f)))) {
            NamedTag root = nbtStream.readNamedTag();
            if (!root.getName().equals(ROOT_NODE)) {
                throw new RuntimeException("File not of type '" + ROOT_NODE + "'");
            }

            Map<String, Tag> rootMap = (Map) root.getTag().getValue();

            int width = NBTUtils.getChildTag(rootMap, "Width", IntTag.class).getValue();
            int height = NBTUtils.getChildTag(rootMap, "Height", IntTag.class).getValue();
            int length = NBTUtils.getChildTag(rootMap, "Length", IntTag.class).getValue();

            BlockVector size = new BlockVector(width, height, length);
            BlockStore blockStore = new BlockStore(f, rootMap, size);
            return blockStore;
        }

    }

}
