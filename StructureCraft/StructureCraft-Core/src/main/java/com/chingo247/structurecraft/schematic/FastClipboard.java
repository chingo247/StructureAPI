/*
 * Copyright (C) 2015 Chingo
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
package com.chingo247.structurecraft.schematic;

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
import com.sk89q.worldedit.data.DataException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author Chingo
 */
public class FastClipboard {

    private Vector offset;
    private int width;
    private int height;
    private int length;
    private final short[] blockIds;
    private final byte[] data;
    private final Map<BlockVector, Map<String, Tag>> tileEntities;
    private final int yAxisOffset;

    private FastClipboard(int yAxisOffset, int width, int height, int length, short[] blockIds, byte[] data, Map<BlockVector, Map<String, Tag>> tileEntities) {
        this.offset = Vector.ZERO;
        this.tileEntities = tileEntities;
        this.width = width;
        this.height = height;
        this.length = length;
        this.blockIds = blockIds;
        this.data = data;
        this.yAxisOffset = yAxisOffset;
    }

    /**
     * Gets the rotation offset over the y-axis
     * @return 
     */
    public int getyAxisOffset() {
        return yAxisOffset;
    }
    
    

    public BaseBlock getBlock(Vector pos) {
        return getBlock(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
    }
    
    public boolean hasBlock(int x, int y, int z) {
        return ((y * width * length) + z * width + x) < blockIds.length;
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

    public int getHeight() {
        return height;
    }

    public int getLength() {
        return length;
    }

    public int getWidth() {
        return width;
    }

    public Vector getSize() {
        return new Vector(width, height, length);
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

    public static FastClipboard read(File schematicFile) throws IOException {
        NBTInputStream nbtStream = new NBTInputStream(
                new GZIPInputStream(new FileInputStream(schematicFile)));

        NamedTag rootTag = nbtStream.readNamedTag();

        if (!rootTag.getName().equalsIgnoreCase("Schematic")) {
            throw new RuntimeException("Tag 'Schematic' does not exist or is not first");
        }
        nbtStream.close();

        // Check

        Map<String, Tag> schematic = (Map) rootTag.getTag().getValue();
        if (!schematic.containsKey("Blocks")) {
            throw new RuntimeException("Schematic file is missing a \"Blocks\" tag");
        }

        // Get information
        
        short width = getChildTag(schematic, "Width", ShortTag.class).getValue();
        short length = getChildTag(schematic, "Length", ShortTag.class).getValue();
        short height = getChildTag(schematic, "Height", ShortTag.class).getValue();
        byte[] ids = getChildTag(schematic, "Blocks", ByteArrayTag.class).getValue();
        byte[] data = getChildTag(schematic, "Data", ByteArrayTag.class).getValue();
        byte[] addId = new byte[0];

        // We support 4096 block IDs using the same method as vanilla Minecraft, where
        // the highest 4 bits are stored in a separate byte array.
        if (schematic.containsKey("AddBlocks")) {
            addId = getChildTag(schematic, "AddBlocks", ByteArrayTag.class).getValue();
        }

        // read ids
        short[] blockids = new short[ids.length]; // Have to later combine IDs
        // Combine the AddBlocks data with the first 8-bit block ID
        for (int index = 0; index < ids.length; index++) {
            if ((index >> 1) >= addId.length) { // No corresponding AddBlocks index
                blockids[index] = (short) (ids[index] & 0xFF);
            } else {
                if ((index & 1) == 0) {
                    blockids[index] = (short) (((addId[index >> 1] & 0x0F) << 8) + (ids[index] & 0xFF));
                } else {
                    blockids[index] = (short) (((addId[index >> 1] & 0xF0) << 4) + (ids[index] & 0xFF));
                }
            }
            
        }

        // read data
        byte[] blockData = new byte[data.length];
        for (int index = 0; index < data.length; index++) {
            if ((index >> 1) >= addId.length) { // No corresponding AddBlocks index
                blockData[index] = (data[index]);
            } else {
                if ((index & 1) == 0) {
                    blockData[index] = (byte) (((addId[index >> 1] & 0x0F) << 8) + (blockData[index]));
                } else {
                    blockData[index] = (byte) (((addId[index >> 1] & 0xF0) << 4) + (blockData[index]));
                }
            }
        }

        // Need to pull out tile entities
        List<Tag> tileEntities = getChildTag(schematic, "TileEntities", ListTag.class)
                .getValue();
        Map<BlockVector, Map<String, Tag>> tileEntitiesMap
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
        
        Tag tag = schematic.get("SCyAxisOffset");
        
        int yAxisOffset;
        if(tag == null) {
            yAxisOffset = 0;
        } else {
            IntTag yAxisTag = (IntTag) tag;
            yAxisOffset = yAxisTag.getValue();
        }
        
        return new FastClipboard(yAxisOffset, width, height, length, blockids, blockData, tileEntitiesMap);
    }
    
    
    public static void rotateAndWrite(File schematicFile, int degree) throws IOException {
        if(degree % 90 != 0) {
            throw new IllegalArgumentException("Degree must be a multiple of 90");
        }
        NBTInputStream nbtStream = new NBTInputStream(
                new GZIPInputStream(new FileInputStream(schematicFile)));

        NamedTag rootTag = nbtStream.readNamedTag();

        if (!rootTag.getName().equalsIgnoreCase("Schematic")) {
            throw new RuntimeException("Tag 'Schematic' does not exist or is not first");
        }
        nbtStream.close();
        
        Map<String, Tag> schematic = (Map) rootTag.getTag().getValue(); // Because this is an unmodifiable instance....
        Tag currentYOffSetTag = schematic.get("SCyAxisOffset");
        
        int currentYOffSet;
        if(currentYOffSetTag == null) {
            currentYOffSet = 0;
        } else {
            IntTag yAxisTag = (IntTag) currentYOffSetTag;
            currentYOffSet = yAxisTag.getValue();
        }
        
        Map<String, Tag> copy = new HashMap<>(schematic); // We need to copy here
        int normalizedYOffset = (int) normalizeYaw(degree + currentYOffSet);
        copy.put("SCyAxisOffset", new IntTag(normalizedYOffset));
        
        CompoundTag tag = new CompoundTag(copy);
       
        
        try(NBTOutputStream outputStream = new NBTOutputStream(new GZIPOutputStream(new FileOutputStream(schematicFile)))) {
            outputStream.writeNamedTag("Schematic", tag);
        }
        
        
        
    }
    
    private static float normalizeYaw(float yaw) {
        float ya = yaw;
        if(yaw > 360) {
            int times = (int)((ya - (ya % 360)) / 360);
            int normalizer = times * 360;
            ya -= normalizer;
        } else if (yaw < -360) {
            ya = Math.abs(ya);
            int times = (int)((ya - (ya % 360)) / 360);
            int normalizer = times * 360;
            ya = yaw + normalizer;
        }
        return ya;
    } 

}
