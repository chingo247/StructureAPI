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
package com.chingo247.structurecraft.construction.contract.safe.schematic;

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
public class IOSchematicSafeData {

    private IOSchematicSafeData() {
    }

    public static SchematicSafeData read(File file) throws IOException {
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

        int width = getChildTag(schematic, "Width", ShortTag.class).getValue();
        int height = getChildTag(schematic, "Height", ShortTag.class).getValue();
        int length = getChildTag(schematic, "Length", ShortTag.class).getValue();

        SchematicSafeData schematicSafeData = new SchematicSafeData(width, height, length);

        byte[] blockIds = getChildTag(schematic, "Blocks", ByteArrayTag.class).getValue();
        schematicSafeData.setBlockIds(blockIds);

        byte[] data = getChildTag(schematic, "Data", ByteArrayTag.class).getValue();
        schematicSafeData.setData(data);

        byte[] done = getChildTag(schematic, "Done", ByteArrayTag.class).getValue();
        schematicSafeData.setDone(done);

        List<Tag> tileEntitiesList = getChildTag(schematic, "TileEntities", ListTag.class).getValue();
        Map<Vector, Map<String, Tag>> tileEntities = getTileEntitiesMap(tileEntitiesList);
        schematicSafeData.setTileEntities(tileEntities);

        byte[] addBlocks = schematic.containsKey("AddBlocks") ? getChildTag(schematic, "AddBlocks", ByteArrayTag.class).getValue() : new byte[0];
        schematicSafeData.setAddBlocks(addBlocks);

        return schematicSafeData;

    }

    public static void write(File file, SchematicSafeData safeData) throws IOException {
        try (NBTOutputStream outputStream = new NBTOutputStream(new GZIPOutputStream(new FileOutputStream(file)))) {
            Map<String, Tag> schematic = Maps.newHashMap();
            schematic.put("Width", new ShortTag((short) safeData.getWidth()));
            schematic.put("Length", new ShortTag((short) safeData.getLength()));
            schematic.put("Height", new ShortTag((short) safeData.getHeight()));
            schematic.put("Blocks", new ByteArrayTag(safeData.getBlockIds()));
            schematic.put("Data", new ByteArrayTag(safeData.getData()));
            schematic.put("Done", new ByteArrayTag(safeData.getDone()));

            List<Tag> tileEntitiesList = Lists.newArrayList();
            for (Map<String, Tag> t : safeData.getTileEntities().values()) {
                tileEntitiesList.add(new CompoundTag(t));
            }

            schematic.put("TileEntities", new ListTag(CompoundTag.class, tileEntitiesList));
            if (safeData.getAddBlocks() != null) {
                schematic.put("AddBlocks", new ByteArrayTag(safeData.getAddBlocks()));
            }
            outputStream.writeNamedTag("Schematic", new CompoundTag(schematic));
        }

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
