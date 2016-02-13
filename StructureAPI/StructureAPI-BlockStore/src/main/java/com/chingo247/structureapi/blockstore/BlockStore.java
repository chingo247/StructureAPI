/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.blockstore;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author Chingo
 */
public class BlockStore implements IBlockStore {
    
    public static final String REGION_PREFIX = "blockstore.r.";
    public static final int CHUNK_SIZE = 16;
    public static final int REGION_SIZE = CHUNK_SIZE * 32;
    public static final int REGION_HEIGHT = 256;

    private int height;
    private int width;
    private int length;
    private File directory;

//    private Map<String, File> regions;
    /**
     * TODO: Replace with GUAVA's cache loader to save memory
     */
    private Map<String, IBlockStoreRegion> loadedRegion;

    public BlockStore(File directory, int width, int height, int length) {
        Preconditions.checkArgument(width > 0, "Width must be greater than 0");
        Preconditions.checkArgument(height > 0, "Height must be greater than 0");
        Preconditions.checkArgument(length > 0, "Length must be greater than 0");
        this.width = width;
        this.length = length;
        this.directory = directory;
    }
    
    @Override
    public File getDirectory() {
        return directory;
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
    public BaseBlock getBlockAt(Vector position) {
        return getBlockAt(position.getBlockX(), position.getBlockY(), position.getBlockZ());
    }

    @Override
    public BaseBlock getBlockAt(int x, int y, int z) {
        IBlockStoreRegion region = getRegion(x, z);
        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        int regionX = (chunkX >> 5) * REGION_SIZE;
        int regionZ = (chunkZ >> 5) * REGION_SIZE;
        return region.getBlockAt(x - regionX, y, z - regionZ);
    }

    @Override
    public void setBlockAt(int x, int y, int z, BaseBlock block) {
        IBlockStoreRegion region = getRegion(x, z);
        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        int regionX = (chunkX >> 5) * REGION_SIZE;
        int regionZ = (chunkZ >> 5) * REGION_SIZE;
        region.setBlockAt(x - regionX, y, z - regionZ, block);
    }

    @Override
    public void setBlockAt(Vector position, BaseBlock block) {
        setBlockAt(position.getBlockX(), position.getBlockY(), position.getBlockZ(), block);
    }

    @Override
    public IBlockStoreRegion getRegion(int x, int z) {
        if (x < 0) {
            throw new IndexOutOfBoundsException("x < 0, x was " + x);
        }
        if (z < 0) {
            throw new IndexOutOfBoundsException("z < 0, z was " + z);
        }
        if (width != -1 && x > width) {
            throw new IndexOutOfBoundsException("x > " + width + " (=width), x was " + x);
        }
        if (length != -1 && z > length) {
            throw new IndexOutOfBoundsException("z > " + length + " (=length), z was " + z);
        }

        String key = getRegionKey(x, z);
        IBlockStoreRegion region = loadedRegion.get(key);

        if (region == null) {
            File regionFile = new File(directory, key + ".blockstore");
            if (regionFile.exists()) {
                region = read(regionFile);
            } else {
                int chunkX = x >> 4;
                int chunkZ = z >> 4;

                int regionX = (chunkX >> 5) * REGION_SIZE;
                int regionZ = (chunkZ >> 5) * REGION_SIZE;

                int regionWidth = (regionX + REGION_SIZE) > width ? width - regionX : REGION_SIZE;
                int regionHeight = height;
                int regionLength = (regionZ + REGION_SIZE) > length ? length - regionZ : REGION_SIZE;
                region = newRegion(regionFile, regionX, regionZ, regionWidth, regionHeight, regionLength);

            }

            if (region == null) {
                throw new RuntimeException("Failed to create region, region was null");
            }

            loadedRegion.put(key, region);
        }

        return region;
    }

    protected IBlockStoreRegion read(File regionFile) {
        try {
            return BlockStoreRegion.load(regionFile);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected IBlockStoreRegion newRegion(File regionFile, int x, int z, int width, int height, int length) {
        BlockStoreRegion region = new BlockStoreRegion(regionFile, width, height, length);
        return region;
    }

    protected final String getRegionKey(int x, int z) {
        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        int regionX = chunkX >> 5;
        int regionZ = chunkZ >> 5;
        return REGION_PREFIX + regionX + "." + regionZ;
    }
    
    public Iterator<File> regionFileIterator() {
        List<File> regionFiles = Lists.newArrayList();
        for(File f : directory.listFiles()) {
            if(FilenameUtils.isExtension(f.getName(), "blockstore") && f.getName().startsWith(REGION_PREFIX)) {
                regionFiles.add(f);
            }
        }
        return regionFiles.iterator();
    }

    @Override
    public Iterator<IBlockStoreRegion> iterator() {
        return new RegionIterator(regionFileIterator());
    }
    
    private class RegionIterator implements Iterator<IBlockStoreRegion> {
        
        private Iterator<File> regionFileIt;

        public RegionIterator(Iterator<File> regionFileIt) {
            this.regionFileIt = regionFileIt;
        }

        @Override
        public boolean hasNext() {
            return regionFileIt.hasNext();
        }

        @Override
        public IBlockStoreRegion next() {
            File f = regionFileIt.next();
            return read(f);
        }
    }

}
