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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author Chingo
 */
public class BlockStore implements IBlockStore {

    public static final String EXTENSION = ".blockstore";
    public static final String VERSION = "1.0.0";
    public static final int CHUNK_SIZE = 16;
    public static final int REGION_SIZE = CHUNK_SIZE * 32;
    public static final int REGION_HEIGHT = 256;

    private int height;
    private int width;
    private int length;
    private int chunkSize;
    private File directory;
    private String name, version;

//    private Map<String, File> regions;
    /**
     * TODO: Replace with GUAVA's cache loader to save memory
     */
    private Map<String, IBlockStoreRegion> loadedRegions;

    public BlockStore(File directory, int width, int height, int length) {
        Preconditions.checkArgument(width > 0, "Width must be greater than 0");
        Preconditions.checkArgument(height > 0, "Height must be greater than 0");
        Preconditions.checkArgument(length > 0, "Length must be greater than 0");
        this.width = width;
        this.height = height;
        this.length = length;
        this.directory = directory;
        this.name = directory.getName();
        this.loadedRegions = Maps.newHashMap();
        this.version = VERSION;
        this.chunkSize = CHUNK_SIZE;
    }

    @Override
    public String getName() {
        return name;
    }

    public int getChunkSize() {
        return chunkSize;
    }
    
    public List<IBlockStoreRegion> getLoadedRegions() {
        return new ArrayList<>(loadedRegions.values());
    }

    public final File getMetaDataFile() {
        return new File(directory, name + ".meta" + EXTENSION);
    }

    public String getVersion() {
        return version;
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

    public int getHeight() {
        return height;
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
        IBlockStoreRegion region = loadedRegions.get(key);

        if (region == null) {
            File regionFile = new File(directory, key + ".blockstore");
            if (regionFile.exists()) {
                try {
                    region = readRegion(regionFile);
                } catch (IOException ex) {
                    throw new RuntimeException("Failed to read region file '" + regionFile.getAbsolutePath() + "'", ex);
                }
            } else {
                int chunkX = x >> 4;
                int chunkZ = z >> 4;

                int regionX = (chunkX >> 5) * REGION_SIZE;
                int regionZ = (chunkZ >> 5) * REGION_SIZE;

                int regionWidth = (regionX + REGION_SIZE) > width ? width - regionX : REGION_SIZE;
                int regionHeight = height;
                int regionLength = (regionZ + REGION_SIZE) > length ? length - regionZ : REGION_SIZE;
                region = newRegion(regionFile, new HashMap<String, Tag>(), regionWidth, regionHeight, regionLength);

            }

            if (region == null) {
                throw new RuntimeException("Failed to create region, region was null");
            }

            loadedRegions.put(key, region);
        }

        return region;
    }

    private static String makePrefix(String name) {
        return name + ".r.";
    }

    protected IBlockStoreRegion readRegion(File regionFile) throws IOException {
        BlockStoreReader reader = new BlockStoreReader();
        return reader.readRegion(this, regionFile);
    }

    protected IBlockStoreRegion newRegion(File regionFile, Map<String, Tag> root, int width, int height, int length) {
        BlockStoreRegion region = new BlockStoreRegion(this, regionFile, width, height, length);
        return region;
    }

    protected final String getRegionKey(int x, int z) {
        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        int regionX = chunkX >> 5;
        int regionZ = chunkZ >> 5;
        return makePrefix(name) + regionX + "." + regionZ;
    }

    public Iterator<File> regionFileIterator() {
        List<File> regionFiles = Lists.newArrayList();
        for (File f : directory.listFiles()) {
            if (FilenameUtils.isExtension(f.getName(), "blockstore") && f.getName().startsWith(makePrefix(name))) {
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
            try {
                return readRegion(f);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported");
        }
        
    }

}
