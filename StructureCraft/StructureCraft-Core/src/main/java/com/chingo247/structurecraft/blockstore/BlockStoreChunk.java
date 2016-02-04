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

import static com.chingo247.structurecraft.blockstore.BlockStore.DEFAULT_SIZE;
import static com.chingo247.structurecraft.blockstore.NBTUtils.getChildTag;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.blocks.BaseBlock;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author Chingo
 */
public class BlockStoreChunk implements IBlockStoreChunk {

    protected IBlockStore blockStore;
    protected Map<String, Tag> chunkTagMap;
    protected Map<String, IBlockStoreSection> sections;
    protected IBlockStoreSectionFactory<IBlockStoreSection> sectionFactory;
    protected Map<Vector, Map<String, Tag>> tileEntitiesMap;

    protected final int x;
    protected final int z;
    private final Vector2D dimension;

    protected BlockStoreChunk(IBlockStore blockStore, Map<String, Tag> chunkTagMap, int x, int z, Vector2D dimension) {
        this.blockStore = blockStore;
        this.chunkTagMap = chunkTagMap;
        this.x = x;
        this.z = z;
        this.dimension = dimension;
        this.sectionFactory = new BlockStoreSectionFactory(this);
        this.sections = Maps.newHashMap();
        this.tileEntitiesMap = Maps.newHashMap();

        if (chunkTagMap.containsKey("TileEntities")) {
            List<Tag> tileEntities = getChildTag(chunkTagMap, "TileEntities", ListTag.class)
                    .getValue();

            for (Tag tag : tileEntities) {
                if (!(tag instanceof CompoundTag)) {
                    continue;
                }

                CompoundTag t = (CompoundTag) tag;

                int eX = 0;
                int eY = 0;
                int eZ = 0;

                Map<String, Tag> values = new HashMap<>();
                for (Map.Entry<String, Tag> entry : t.getValue().entrySet()) {
                    if (entry.getKey().equals("x")) {
                        if (entry.getValue() instanceof IntTag) {
                            eX = ((IntTag) entry.getValue()).getValue();
                        }
                    } else if (entry.getKey().equals("y")) {
                        if (entry.getValue() instanceof IntTag) {
                            eY = ((IntTag) entry.getValue()).getValue();
                        }
                    } else if (entry.getKey().equals("z")) {
                        if (entry.getValue() instanceof IntTag) {
                            eZ = ((IntTag) entry.getValue()).getValue();
                        }
                    }
                    values.put(entry.getKey(), entry.getValue());
                }

                BlockVector vec = new BlockVector(eX, eY, eZ);
                tileEntitiesMap.put(vec, values);
            }
        }

    }

    public IBlockStoreSectionFactory<IBlockStoreSection> getSectionFactory() {
        return sectionFactory;
    }

    public boolean hasSectionAt(int y) {
        String key = getSectionKey(y);
        return sections.get(key) != null || chunkTagMap.get(key) != null;
    }

    @Override
    public CompoundTag getTileEntityData(int x, int y, int z) {
        Map<String, Tag> compoundData = tileEntitiesMap.get(new BlockVector(x, y, z));
        return compoundData != null ? new CompoundTag(compoundData) : null;
    }

    @Override
    public void setTileEntityData(int x, int y, int z, CompoundTag tag) {
        this.tileEntitiesMap.put(new BlockVector(x, y, z), tag.getValue());
    }

    protected final String getSectionKey(int y) {
        int sectionY = y >> 4;
        return "Section-[" + sectionY + "]";
    }

    @Override
    public Vector2D getSize() {
        return dimension;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getZ() {
        return z;
    }

    @Override
    public int getChunkX() {
        return x >> 4;
    }

    @Override
    public int getChunkZ() {
        return z >> 4;
    }

    public final IBlockStoreSection getSection(int y) {
        String key = getSectionKey(y);
        IBlockStoreSection section = sections.get(key);
        int sectionY = (y >> 4) * 16;
        if (section == null) {
            Tag sectionTag = chunkTagMap.get(key);
            int sectionHeight = sectionY + 16 > blockStore.getHeight() ? (blockStore.getHeight() - sectionY) : DEFAULT_SIZE;
            
            int height; 
            if(sectionTag == null) {
                height = sectionY + 16 > blockStore.getHeight() ? (blockStore.getHeight() - sectionY) : DEFAULT_SIZE;
            } else {
                Map<String,Tag> map = (Map)sectionTag.getValue();
                if(map.containsKey("Height")) {
                    Tag lengthTag = map.get("Height");
                    height = (short) lengthTag.getValue();
                } else {
                    height = DEFAULT_SIZE;
                }
            }
            
            if(height <= 0) {
                throw new RuntimeException("Height was <= 0");
            }
            
            
            section = this.getSectionFactory().newSection(sectionTag, sectionY, sectionHeight);
            if (section == null) {
                throw new NullPointerException("BlockStoreSectionFactory returned null!");
            }
            sections.put(key, section);
        }
        return section;
    }

    @Override
    public BaseBlock getBlockAt(int x, int y, int z) {
        int sectionY = (y >> 4) * 16;
        IBlockStoreSection section = getSection(y);
        return section.getBlockAt(x, y - sectionY, z);
    }

    @Override
    public BaseBlock getBlockAt(Vector position) {
        return getBlockAt(position.getBlockX(), position.getBlockY(), position.getBlockZ());
    }

    @Override
    public void setBlockAt(int x, int y, int z, BaseBlock block) {
        int sectionY = (y >> 4) * 16;
        IBlockStoreSection section = getSection(y);
        section.setBlockAt(x, y - sectionY, z, block);
    }

    @Override
    public void setBlockAt(Vector vector, BaseBlock block) {
        setBlockAt(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ(), block);
    }
    
    @Override
    public Map<String, Tag> serialize() {
        Map<String, Tag> rootMap = Maps.newHashMap();
        Map<String, Tag> sectionsMap = Maps.newHashMap();

        // Add Sections
        Set<Entry<String,IBlockStoreSection>> sectionsSet = sections.entrySet();
        for (Iterator<Entry<String, IBlockStoreSection>> iterator = sectionsSet.iterator(); iterator.hasNext();) {
            Entry<String, IBlockStoreSection> next = iterator.next();
            sectionsMap.put(next.getKey(), new CompoundTag(next.getValue().serialize()));
        }
        
        rootMap.put("Sections", new CompoundTag(sectionsMap));
        
        // Add TileEntities
        List<Tag> tileEntitiesList = Lists.newArrayList();
        for (Map<String, Tag> t : tileEntitiesMap.values()) {
            tileEntitiesList.add(new CompoundTag(t));
        }
        rootMap.put("TileEntities", new ListTag(CompoundTag.class, tileEntitiesList));
        
        if(dimension.getBlockX() != DEFAULT_SIZE) {
            rootMap.put("Width", new ShortTag((short) dimension.getBlockX()));
        }
        
        if(dimension.getBlockZ() != DEFAULT_SIZE) {
            rootMap.put("Length", new ShortTag((short) dimension.getBlockZ()));
        }
        
        
        
        return rootMap;
    }

    @Override
    public boolean isEmpty() {
        return chunkTagMap.isEmpty() && sections.isEmpty();
    }

}
