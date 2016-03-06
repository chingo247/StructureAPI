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
package com.chingo247.structureapi.plan.heightmap;

import com.chingo247.blockstore.NBTUtils;
import static com.chingo247.blockstore.NBTUtils.getChildTag;
import com.chingo247.settlercraft.core.util.XXHasher;
import com.chingo247.structureapi.StructureAPI;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import com.sk89q.jnbt.ByteArrayTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.IntArrayTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.NBTOutputStream;
import com.sk89q.jnbt.NamedTag;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.Tag;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Map;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author Chingo
 */
public class HeightmapStorage {

    private LoadingCache<SchematicKey, String> heightMap;

    private HeightmapStorage() {
        this.heightMap = CacheBuilder.newBuilder()
                .concurrencyLevel(Math.max(2, Runtime.getRuntime().availableProcessors() / 2))
                .maximumSize(1000)
                .build(new CacheLoader<SchematicKey, String>() {

                    @Override
                    public String load(SchematicKey key) throws Exception {
                        File workingDir = StructureAPI.getInstance().getWorkingDirectory();
                        File heightmapDir = new File(workingDir, "heightmaps");
                        heightmapDir.mkdirs();

                        File heightmapFile = new File(heightmapDir, String.valueOf(key.hash) + ".heightmap");
                        if (!heightmapFile.exists()) {
                            Heightmap heightmap = HeightmapStorage.heightMapFromSchematic(new File(key.file));
                            writeHeightmap(heightmap, heightmapFile);
                        }
                        return heightmapFile.getAbsolutePath();
                    }
                });
    }

    public Heightmap getHeightmap(File schematic) throws Exception {
        XXHasher xxh = new XXHasher();
        long hash = xxh.hash64(schematic);
        SchematicKey key = new SchematicKey(hash, schematic.getAbsolutePath());
        String heightmap = heightMap.get(key);
        File heightmapFile = new File(heightmap);
        return readHeightmap(heightmapFile);
    }

    public static Heightmap heightMapFromSchematic(File schematic) throws IOException {
        URL url = schematic.toURI().toURL();
        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        final InputStream is = Channels.newInputStream(rbc);
        return heightMapFromSchematic(is);
    }

    public static Heightmap heightMapFromSchematic(InputStream schematicStream) throws IOException {
        long start = System.currentTimeMillis();
        try (NBTInputStream stream = new NBTInputStream(new GZIPInputStream(schematicStream))) {
            NamedTag tag = stream.readNamedTag();
            stream.close();

            Map<String, Tag> schematic = (Map) tag.getTag().getValue();

            short width = getChildTag(schematic, "Width", ShortTag.class).getValue();
            short length = getChildTag(schematic, "Length", ShortTag.class).getValue();
            short height = getChildTag(schematic, "Height", ShortTag.class).getValue();

            byte[] ids = getChildTag(schematic, "Blocks", ByteArrayTag.class).getValue();
            byte[] data = getChildTag(schematic, "Data", ByteArrayTag.class).getValue();
            byte[] addId = new byte[0];

            if (schematic.containsKey("AddBlocks")) {
                addId = getChildTag(schematic, "AddBlocks", ByteArrayTag.class).getValue();
            }

            int[] heightmap = new int[width * length];

            for (int x = 0; x < width; x++) {
                for (int z = 0; z < length; z++) {
                    for (int y = height - 1; y >= 0; y--) {
                        int index = ((y * width * length) + z * width + x);
                        short id;
                        if ((index >> 1) >= addId.length) { // No corresponding AddBlocks index
                            id = (short) (ids[index] & 0xFF);
                        } else {
                            if ((index & 1) == 0) {
                                id = (short) (((addId[index >> 1] & 0x0F) << 8) + (ids[index] & 0xFF));
                            } else {
                                id = (short) (((addId[index >> 1] & 0xF0) << 4) + (ids[index] & 0xFF));
                            }
                        }
                        if (id != 0) {
                            heightmap[z * width + x] = (byte) y;
                        }
                    }
                }
            }
            long stop = System.currentTimeMillis();
            System.out.println("Finished in " + (stop - start) + "ms");
            return new Heightmap(heightmap, width, length);
        }
    }

    public static void writeHeightmap(Heightmap heightmap, File outputFile) throws IOException {
        Map<String, Tag> root = Maps.newHashMap();
        root.put("width", new IntTag(heightmap.getWidth()));
        root.put("length", new IntTag(heightmap.getLength()));
        root.put("map", new IntArrayTag(heightmap.getMap()));
        try (NBTOutputStream stream = new NBTOutputStream(new GZIPOutputStream(new FileOutputStream(outputFile)))) {
            stream.writeNamedTag("heightmap", new CompoundTag(root));
        }
    }

    public static Heightmap readHeightmap(File heightmap) throws IOException {
        URL url = heightmap.toURI().toURL();
        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        final InputStream is = Channels.newInputStream(rbc);
        return readHeightmap(is);
    }

    public static Heightmap readHeightmap(InputStream heightmap) throws IOException {
        try (NBTInputStream inputStream = new NBTInputStream(heightmap)) {
            NamedTag tag = inputStream.readNamedTag();
            Map<String, Tag> root = (Map) tag.getTag().getValue();
            int width = NBTUtils.getChildTag(root, "width", IntTag.class).getValue();
            int length = NBTUtils.getChildTag(root, "length", IntTag.class).getValue();
            int[] map = NBTUtils.getChildTag(root, "map", IntArrayTag.class).getValue();
            return new Heightmap(map, width, length);
        }
    }

    public static HeightmapStorage getInstance() {
        return HeightmapStorageHolder.INSTANCE;
    }

    private static class HeightmapStorageHolder {

        private static final HeightmapStorage INSTANCE = new HeightmapStorage();
    }

    private class SchematicKey {

        private Long hash;
        private String file;

        public SchematicKey(Long hash, String file) {
            this.hash = hash;
            this.file = file;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 47 * hash + Objects.hashCode(this.hash);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final SchematicKey other = (SchematicKey) obj;
            if (!Objects.equals(this.hash, other.hash)) {
                return false;
            }
            return true;
        }

    }
}
