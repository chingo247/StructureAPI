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
package com.chingo247.structurecraft.blockstore;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
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

    protected Map<String, Tag> chunkTags;
    protected Map<String, IBlockStoreChunk> chunks;
    protected File file;

    private int width, height, length;

    private BlockStoreChunkFactory chunkFactory;

    public BlockStore(File file, CuboidRegion region) {
        this(file, region.getWidth(), region.getHeight(), region.getLength());
    }

    public BlockStore(File file, int width, int height, int length) {
        this(file, new HashMap<String, Tag>(), width, height, length);
    }

    protected BlockStore(File file, Map<String, Tag> root, int width, int height, int length) {
        Preconditions.checkArgument(width > 0, "width has to be > 0");
        Preconditions.checkArgument(height > 0, "height has to be > 0");
        Preconditions.checkArgument(length > 0, "length has to be > 0");

        this.width = width;
        this.height = height;
        this.length = length;
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
            int chunkX = (x >> 4) * 16;
            int chunkZ = (z >> 4) * 16;
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
        int chunkX = (x >> 4) * 16;
        int chunkZ = (z >> 4) * 16;

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
        if (x > getWidth()) {
            throw new IndexOutOfBoundsException("x > " + getWidth() + ": x was " + x);
        }
        if (y > getHeight()) {
            throw new IndexOutOfBoundsException("y > " + getHeight() + ": y was " + y);
        }
        if (z > getLength()) {
            throw new IndexOutOfBoundsException("z > " + getLength() + ": z was " + z);
        }
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public Vector getSize() {
        return new BlockVector(width, height, length);
    }

    protected final String getChunkKey(int x, int z) {
        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        return "Chunk-[" + chunkX + "," + chunkZ + "]";
    }
    
    @Override
    public Iterator<IBlockStoreChunk> iterator() {
        return new BlockStoreChunkIterator(chunkTags);
    }

    public IBlockStoreChunk getChunk(int x, int z) {
        String key = getChunkKey(x, z);
        IBlockStoreChunk bsc = chunks.get(key);
        if (bsc == null) {
            Tag chunkTag = chunkTags.get(key);

            int chunkX = (x >> 4) * 16;
            int chunkZ = (z >> 4) * 16;

            int chunkWidth;
            if (chunkTag == null) {
                chunkWidth = chunkX + 16 > getWidth() ? (getWidth() - chunkX) : DEFAULT_SIZE;
            } else {
                Map<String, Tag> map = (Map) chunkTag.getValue();
                if (map.containsKey("Width")) {
                    Tag widthTag = map.get("Width");
                    chunkWidth = (short) widthTag.getValue();
                } else {
                    chunkWidth = DEFAULT_SIZE;
                }
            }

            if (chunkWidth <= 0) {
                throw new RuntimeException("Width was <= 0");
            }

            int chunkLength;
            if (chunkTag == null) {
                chunkLength = chunkZ + 16 > getLength() ? (getLength() - chunkZ) : DEFAULT_SIZE;
            } else {
                Map<String, Tag> map = (Map) chunkTag.getValue();
                if (map.containsKey("Length")) {
                    Tag lengthTag = map.get("Length");
                    chunkLength = (short) lengthTag.getValue();
                } else {
                    chunkLength = DEFAULT_SIZE;
                }
            }

            if (chunkLength <= 0) {
                throw new RuntimeException("Length was <= 0");
            }
            bsc = getChunkFactory().newChunk(chunkTag, chunkX, chunkZ, new Vector2D(chunkWidth, chunkLength));
            this.chunks.put(key, bsc);
        }
        return bsc;
    }

    @Override
    public void save() throws IOException {
        Map<String, Tag> rootMap = serialize();

        try (NBTOutputStream outputStream = new NBTOutputStream(new GZIPOutputStream(new FileOutputStream(file)))) {
            outputStream.writeNamedTag("BlockStore", new CompoundTag(rootMap));
        }
    }

    public Map<String, Tag> serialize() {
        Map<String, Tag> rootMap = chunkTags;
        Set<Entry<String, IBlockStoreChunk>> chunkSet = chunks.entrySet();
        for (Iterator<Entry<String, IBlockStoreChunk>> iterator = chunkSet.iterator(); iterator.hasNext();) {
            Entry<String, IBlockStoreChunk> next = iterator.next();
            rootMap.put(next.getKey(), new CompoundTag(next.getValue().serialize()));
        }

        rootMap.put("Width", new ShortTag((short) width));
        rootMap.put("Height", new ShortTag((short) height));
        rootMap.put("Length", new ShortTag((short) length));

        return rootMap;
    }

    public static BlockStore load(File f) throws IOException {
        try (NBTInputStream nbtStream = new NBTInputStream(new GZIPInputStream(new FileInputStream(f)))) {
            NamedTag root = nbtStream.readNamedTag();
            if (!root.getName().equals(ROOT_NODE)) {
                throw new RuntimeException("File not of type '" + ROOT_NODE + "'");
            }

            Map<String, Tag> rootMap = (Map) root.getTag().getValue();

            int width = NBTUtils.getChildTag(rootMap, "Width", ShortTag.class).getValue();
            int height = NBTUtils.getChildTag(rootMap, "Height", ShortTag.class).getValue();
            int length = NBTUtils.getChildTag(rootMap, "Length", ShortTag.class).getValue();

            BlockStore blockStore = new BlockStore(f, rootMap, width, height, length);
            return blockStore;
        }

    }

    private class BlockStoreChunkIterator implements Iterator<IBlockStoreChunk> {

        private Iterator<String> keyIterator;

        public BlockStoreChunkIterator(Map<String, Tag> rootTag) {
            Map<String, Tag> map = Maps.filterKeys(rootTag, new Predicate<String>() {

                @Override
                public boolean apply(String input) {
                    return input.startsWith("Chunk-[");
                }
            });
            Set<String> keys = new TreeSet<>(new Comparator<String>() {

                @Override
                public int compare(String o1, String o2) {
                    int[] coord1 = getCoords(o1);
                    int[] coord2 = getCoords(o2);
                    
                    int comp1 = Integer.compare(coord1[0], coord2[0]);
                    if(comp1 == 0) {
                        return Integer.compare(coord1[1], coord2[1]);
                    }
                    return comp1;
                }
            }); // Ordering alphabetical = IN ORDER!
            keys.addAll(map.keySet());
            keys.addAll(chunks.keySet());
            this.keyIterator = keys.iterator();

        }

        public int[] getCoords(String key) {
            String[] coordsString = key.split(",");
            String xString = coordsString[0].substring("Chunk-[".length());
            String zString = coordsString[1].replaceAll("]", "");
            
            int[] coords = new int[2];
            coords[0] = Integer.parseInt(xString);
            coords[1] = Integer.parseInt(zString);
            return coords;
        }

        @Override
        public boolean hasNext() {
            return keyIterator.hasNext();
        }

        @Override
        public IBlockStoreChunk next() {
            String key = keyIterator.next();
            int[] coords = getCoords(key);
            return getChunk(coords[0] * DEFAULT_SIZE, coords[1] * DEFAULT_SIZE);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported."); //To change body of generated methods, choose Tools | Templates.
        }

    }

}
