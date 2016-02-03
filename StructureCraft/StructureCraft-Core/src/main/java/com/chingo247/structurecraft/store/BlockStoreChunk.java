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
package com.chingo247.structurecraft.store;

import static com.chingo247.structurecraft.store.BlockStore.DEFAULT_SIZE;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.NamedTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.blocks.BaseBlock;
import java.util.HashMap;
import java.util.Map;

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
    }

    public IBlockStoreSectionFactory<IBlockStoreSection> getSectionFactory() {
        return sectionFactory;
    }

    /**
     * Gets the blocksection, may never return null
     *
     * @param sectionTagOrNull The tag or a null value
     * @return The BlockStoreSection
     */
//    protected IBlockStoreSection makeBlockSection(Tag sectionTagOrNull) {
//        BlockStoreSection section;
//        if (sectionTagOrNull == null) {
//            section = new BlockStoreSection(this, new HashMap<String, Tag>());
//        } else {
//            section = new BlockStoreSection(this, (Map) sectionTagOrNull.getValue());
//        }
//        return section;
//    }
    @Override
    public CompoundTag getTileEntityData(int x, int y, int z) {
        Map<String, Tag> compoundData = tileEntitiesMap.get(new BlockVector(x, y, z));
        return compoundData != null ? new CompoundTag(compoundData) : null;
    }
    
    public void setTileEntityData(int x, int y, int z, CompoundTag tag) {
        this.tileEntitiesMap.put(new BlockVector(x, y, z), tag.getValue());
    }

    protected final String getSectionKey(int y) {
        int sectionY = y << 4;
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
            int sectionHeight = sectionY + 16 > blockStore.getHeight()? (blockStore.getHeight() - sectionY) : DEFAULT_SIZE;
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

    public static Tag asTag(BlockStoreChunk chunk) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public static BlockStoreChunk fromTag(Tag tag) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isEmpty() {
        return chunkTagMap.isEmpty() && sections.isEmpty();
    }

}
