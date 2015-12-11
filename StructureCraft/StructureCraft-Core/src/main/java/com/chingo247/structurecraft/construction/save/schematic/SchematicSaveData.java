/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.construction.save.schematic;

import com.chingo247.structurecraft.util.RegionUtil;
import com.chingo247.structurecraft.util.concurrent.ILoadable;
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
public class SchematicSaveData implements ILoadable {

    private byte[] done;
    private byte[] blockIds;
    private byte[] data;
    private byte[] addBlocks;
    private List<Tag> tileEntities;
    private Vector cube;
    private File file;

    public SchematicSaveData(File file, CuboidRegion cube) {
        this.file = file;
        this.cube = RegionUtil.getSize(cube).subtract(Vector.ONE);
    }

    @Override
    public void load() throws IOException {
        if (file.exists()) {
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
            blockIds = getChildTag(schematic, "Blocks", ByteArrayTag.class).getValue();
            data = getChildTag(schematic, "Data", ByteArrayTag.class).getValue();
            done = getChildTag(schematic, "Done", ByteArrayTag.class).getValue();
            tileEntities = getChildTag(schematic, "TileEntities", ListTag.class).getValue();
            addBlocks = schematic.containsKey("AddBlocks") ? getChildTag(schematic, "AddBlocks", ByteArrayTag.class).getValue() : new byte[0];
        } else {
            int size = cube.getBlockX()* cube.getBlockY()* cube.getBlockZ();
            blockIds = new byte[size];
            data = new byte[size];
            done = new byte[size];
            tileEntities = new ArrayList<>();
        }
    }
    
    public void write() throws IOException {
        try(NBTOutputStream outputStream = new NBTOutputStream(new GZIPOutputStream(new FileOutputStream(file)))) {
            Map<String,Tag> schematic = Maps.newHashMap();
            schematic.put("Width", new ShortTag((short) cube.getBlockX()));
            schematic.put("Length", new ShortTag((short) cube.getBlockZ()));
            schematic.put("Height", new ShortTag((short) cube.getBlockY()));
            schematic.put("Blocks", new ByteArrayTag(blockIds));
            schematic.put("Data", new ByteArrayTag(data));
            schematic.put("Done", new ByteArrayTag(done));
            schematic.put("TileEntities", new ListTag(CompoundTag.class, tileEntities));
            if (addBlocks != null) {
                schematic.put("AddBlocks", new ByteArrayTag(addBlocks));
            }
            outputStream.writeNamedTag("Schematic", new CompoundTag(schematic));
        }
        
    }

    public void setBlock(Vector vector, BaseBlock block) {
        int index = (vector.getBlockY() * cube.getBlockX() * cube.getBlockZ()) 
                + vector.getBlockZ() * cube.getBlockX() + vector.getBlockX();
        
        if(this.done[index] == 0) {
            this.data[index] = (byte) block.getData();
            this.blockIds[index] = (byte) block.getType();
            
            if(block.hasNbtData()) {
                this.tileEntities.add(block.getNbtData());
            }
            
            if (block.getType() > 255) {
                if (addBlocks == null) { // Lazily create section
                    addBlocks = new byte[(blockIds.length >> 1) + 1];
                }
                addBlocks[index >> 1] = (byte) (((index & 1) == 0) ?
                        addBlocks[index >> 1] & 0xF0 | (block.getType() >> 8) & 0xF
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

}
