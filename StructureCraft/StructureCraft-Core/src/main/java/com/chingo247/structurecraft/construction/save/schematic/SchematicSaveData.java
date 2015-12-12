/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.construction.save.schematic;

import com.chingo247.structurecraft.util.RegionUtil;
import com.chingo247.structurecraft.util.concurrent.ILoadable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sk89q.jnbt.ByteArrayTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.NBTOutputStream;
import com.sk89q.jnbt.NamedTag;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.regions.CuboidRegion;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author Chingo
 */
public class SchematicSaveData {

    private byte[] done;
    private byte[] blockIds;
    private byte[] data;
    private byte[] addBlocks;
    private Map<Vector, Map<String, Tag>> tileEntities;
    private int width, height, length;
    private File file;

    public SchematicSaveData(File file, CuboidRegion cube) {
        this.file = file;
        Vector size = RegionUtil.getSize(cube).subtract(Vector.ONE);
        
        this.width = size.getBlockX();
        this.height = size.getBlockY();
        this.length = size.getBlockZ();

        int blocks = width * height * length;
        this.blockIds = new byte[blocks];
        this.data = new byte[blocks];
        this.done = new byte[blocks];
        this.tileEntities = Maps.newHashMap();
    }

    private SchematicSaveData(byte[] done, byte[] blockIds, byte[] data, byte[] addBlocks, Map<Vector, Map<String, Tag>> tileEntities, Vector cube, File file) {
        this.done = done;
        this.blockIds = blockIds;
        this.data = data;
        this.addBlocks = addBlocks;
        this.tileEntities = tileEntities;
        this.file = file;
        this.width = cube.getBlockX();
        this.height = cube.getBlockY();
        this.length = cube.getBlockZ();
    }

    public int getWidth() {
        return width;
    }

    public int getLength() {
        return length;
    }

    public int getHeight() {
        return height;
    }
    
     public BaseBlock getBlock(int x, int y, int z) {
        int index = (y * width * length) + z * width + x;
        BaseBlock b = new BaseBlock(blockIds[index], data[index]);

        Map<String, Tag> tileMap = tileEntities.get(new BlockVector(x, y, z));
        if (tileMap != null) {
            b.setNbtData(new CompoundTag(tileMap));
        }

        return b;
    }
     
    public boolean hasBlock(int x, int y, int z) {
        return ((y * width * length) + z * width + x) < blockIds.length;
    }

    public void write() throws IOException {
        try (NBTOutputStream outputStream = new NBTOutputStream(new GZIPOutputStream(new FileOutputStream(file)))) {
            Map<String, Tag> schematic = Maps.newHashMap();
            schematic.put("Width", new ShortTag((short) width));
            schematic.put("Length", new ShortTag((short) length));
            schematic.put("Height", new ShortTag((short) height));
            schematic.put("Blocks", new ByteArrayTag(blockIds));
            schematic.put("Data", new ByteArrayTag(data));
            schematic.put("Done", new ByteArrayTag(done));
            
            List<Tag> tileEntitiesList = Lists.newArrayList();
            for(Map<String,Tag> t : tileEntities.values()) {
                tileEntitiesList.add(new CompoundTag(t));
            }
            
            schematic.put("TileEntities", new ListTag(CompoundTag.class, tileEntitiesList));
            if (addBlocks != null) {
                schematic.put("AddBlocks", new ByteArrayTag(addBlocks));
            }
            outputStream.writeNamedTag("Schematic", new CompoundTag(schematic));
        }

    }

    public void setBlock(Vector vector, BaseBlock block) {
        int index = (vector.getBlockY() * width * length)
                + vector.getBlockZ() * width + vector.getBlockX();

        if (this.done[index] == 0) {
            this.data[index] = (byte) block.getData();
            this.blockIds[index] = (byte) block.getType();

            if (block.hasNbtData()) {
                CompoundTag tag = block.getNbtData();
                if(tag != null) {
                    Map<String,Tag> map = tag.getValue();
                    this.tileEntities.put(vector, map);
                }
            }

            if (block.getType() > 255) {
                if (addBlocks == null) { // Lazily create section
                    addBlocks = new byte[(blockIds.length >> 1) + 1];
                }
                addBlocks[index >> 1] = (byte) (((index & 1) == 0)
                        ? addBlocks[index >> 1] & 0xF0 | (block.getType() >> 8) & 0xF
                        : addBlocks[index >> 1] & 0xF | ((block.getType() >> 8) & 0xF) << 4);
            }

            this.done[index] = 1;
        }
    }

    /**
     * Get child tag of a NBT structure.
     *
     * @param items The parent tag map
     * @param key The name of the tag to get
     * @param expected The expected type of the tag
     * @return child tag casted to the expected type
     * @throws DataException if the tag does not exist or the tag is not of the
     * expected type
     */
    private static <T extends Tag> T getChildTag(Map<String, Tag> items, String key,
            Class<T> expected) {

        if (!items.containsKey(key)) {
            throw new RuntimeException("Schematic file is missing a \"" + key + "\" tag");
        }
        Tag tag = items.get(key);
        if (!expected.isInstance(tag)) {
            throw new RuntimeException(
                    key + " tag is not of tag type " + expected.getName());
        }
        return expected.cast(tag);
    }

    public static SchematicSaveData load(File file) throws IOException {
        NamedTag rootTag;
        try (NBTInputStream nbtStream = new NBTInputStream(
                new GZIPInputStream(new FileInputStream(file)))) {

            rootTag = nbtStream.readNamedTag();
            if (!rootTag.getName().equalsIgnoreCase("Schematic")) {
                throw new RuntimeException("Tag 'Schematic' does not exist or is not first");
            }
        }
        // Check
        Map<String, Tag> schematic = (Map) rootTag.getTag().getValue();
        if (!schematic.containsKey("Blocks")) {
            throw new RuntimeException("Schematic file is missing a \"Blocks\" tag");
        }
        byte[] blockIds = getChildTag(schematic, "Blocks", ByteArrayTag.class).getValue();
        byte[] data = getChildTag(schematic, "Data", ByteArrayTag.class).getValue();
        byte[] done = getChildTag(schematic, "Done", ByteArrayTag.class).getValue();
        
        int width = getChildTag(schematic, "Width", IntTag.class).getValue();
        int height = getChildTag(schematic, "Height", IntTag.class).getValue();
        int length = getChildTag(schematic, "Length", IntTag.class).getValue();
        Vector cube = new BlockVector(width, height, length);
        
        List<Tag> tileEntitiesList = getChildTag(schematic, "TileEntities", ListTag.class).getValue();
        Map<Vector, Map<String, Tag>> tileEntities = getTileEntitiesMap(tileEntitiesList);
        
        byte[] addBlocks = schematic.containsKey("AddBlocks") ? getChildTag(schematic, "AddBlocks", ByteArrayTag.class).getValue() : new byte[0];
        return new SchematicSaveData(done, blockIds, data, addBlocks, tileEntities, cube, file);
    }
    
    private static Map<Vector, Map<String, Tag>> getTileEntitiesMap(List<Tag> tileEntities) {
        Map<Vector, Map<String, Tag>> tileEntitiesMap
                = new HashMap<>();

        for (Tag tag : tileEntities) {
            if (!(tag instanceof CompoundTag)) {
                continue;
            }
            CompoundTag t = (CompoundTag) tag;

            int x = 0;
            int y = 0;
            int z = 0;

            Map<String, Tag> values = new HashMap<>();

            for (Map.Entry<String, Tag> entry : t.getValue().entrySet()) {
                if (entry.getKey().equals("x")) {
                    if (entry.getValue() instanceof IntTag) {
                        x = ((IntTag) entry.getValue()).getValue();
                    }
                } else if (entry.getKey().equals("y")) {
                    if (entry.getValue() instanceof IntTag) {
                        y = ((IntTag) entry.getValue()).getValue();
                    }
                } else if (entry.getKey().equals("z")) {
                    if (entry.getValue() instanceof IntTag) {
                        z = ((IntTag) entry.getValue()).getValue();
                    }
                }

                values.put(entry.getKey(), entry.getValue());
            }

            BlockVector vec = new BlockVector(x, y, z);
            tileEntitiesMap.put(vec, values);
        }
        return tileEntitiesMap;
    }
    
}
