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
package com.chingo247.structureapi.schematic;

import com.chingo247.settlercraft.core.util.XXHasher;
import com.sk89q.worldedit.Vector;
import java.io.File;
import java.io.IOException;
import com.chingo247.structureapi.placement.block.SchematicPlacement;
import com.google.common.base.Preconditions;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.world.DataException;
import java.util.Map;

/**
 *
 * @author Chingo
 */
public class DefaultSchematic implements Schematic {

    private final File schematicFile;
    private final long xxhash;
    private final int width;
    private final int height;
    private final int length;
    private int yAxisOffset;

    protected DefaultSchematic(File schematicFile, int width, int height, int length, int axisOffset) {
        Preconditions.checkNotNull(schematicFile);
        Preconditions.checkArgument(schematicFile.exists());
        this.schematicFile = schematicFile;
        this.yAxisOffset = axisOffset;

        XXHasher hasher = new XXHasher();

        try {
            this.xxhash = hasher.hash64(schematicFile);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        this.width = width;
        this.height = height;
        this.length = length;
    }

    @Override
    public File getFile() {
        return schematicFile;
    }

    @Override
    public long getHash() {
        return this.xxhash;
    }

    @Override
    public final FastClipboard getClipboard() {
        if (!schematicFile.exists()) {
            throw new RuntimeException("File: " + schematicFile.getAbsolutePath() + " doesn't exist");
        }
        try {
            return FastClipboard.read(schematicFile);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Vector getSize() {
        return new Vector(width, height, length);
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public int getAxisOffset() {
        return yAxisOffset;
    }
    
    @Override
    public SchematicPlacement createPlacement() {
        SchematicPlacement placement = new SchematicPlacement(this,yAxisOffset, Vector.ZERO);
        return placement; // Default settlercraft thinks schematics are stored in natural position
    }

//    public static SchematicPlacement readPlacement(File f) throws IOException {
//        NamedTag rootTag;
//        try (NBTInputStream nbtStream = new NBTInputStream(
//                new GZIPInputStream(new FileInputStream(f)))) {
//            rootTag = nbtStream.readNamedTag();
//            if (!rootTag.getName().equalsIgnoreCase("Schematic")) {
//                throw new RuntimeException("Tag 'Schematic' does not exist or is not first");
//            }
//
//            Map<String, Tag> schematic = (Map) rootTag.getTag().getValue();
//            if (!schematic.containsKey("Blocks")) {
//                throw new RuntimeException("Schematic file is missing a \"Blocks\" tag");
//            }
//
//            short width = getChildTag(schematic, "Width", ShortTag.class).getValue();
//            short length = getChildTag(schematic, "Length", ShortTag.class).getValue();
//            short height = getChildTag(schematic, "Height", ShortTag.class).getValue();
//            return new DefaultSchematic(f, width, height, length).createPlacement();
//        }
//    }

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
