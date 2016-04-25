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
package com.chingo247.structureapi.blockstore;

import static com.chingo247.structureapi.blockstore.NBTUtils.getChildTag;
import com.google.common.collect.Maps;
import com.sk89q.jnbt.ByteArrayTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.blocks.BaseBlock;
import java.util.Map;

/**
 *
 * @author Chingo
 */
public class BlockStoreSection implements IBlockStoreSection {

    private final int y;
    private final int sectionHeight;

    private Map<String, Tag> sectionMap;
    private byte[] ids;
    private byte[] data;
    private byte[] addId;
    private IBlockStoreChunk bsc;

    private boolean empty;

    protected BlockStoreSection(IBlockStoreChunk bsc, Map<String, Tag> sectionTagMap, int y, int sectionHeight) {
        this.y = y;
        this.bsc = bsc;
        this.sectionHeight = sectionHeight;
        this.sectionMap = sectionTagMap;
        if (sectionTagMap.isEmpty()) {
            this.empty = true;
        } else {
            this.addId = sectionMap.containsKey("AddId")
                    ? getChildTag(sectionMap, "AddId", ByteArrayTag.class).getValue() : new byte[0];
            this.ids = getChildTag(sectionMap, "Blocks", ByteArrayTag.class).getValue();
            this.data = getChildTag(sectionMap, "Data", ByteArrayTag.class).getValue();
            this.empty = false;
        }

    }

    private CompoundTag getTileEntityData(int x, int y, int z) {
        return bsc.getTileEntityData(x, this.y + y, z);
    }

    private void setTileEntityData(int x, int y, int z, CompoundTag tag) {
        bsc.setTileEntityData(x, this.y + y, z, tag);
    }

    protected final int getArrayIndex(int x, int y, int z) {
        Vector2D size = bsc.getDimension();
        return (y * size.getBlockX() * size.getBlockZ()) + (z * size.getBlockX()) + x;
    }

    @Override
    public boolean isEmpty() {
        return empty;
    }

    @Override
    public int getSectionY() {
        return y >> 4;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public BaseBlock getBlockAt(Vector position) {
        return getBlockAt(position.getBlockX(), position.getBlockY(), position.getBlockZ());
    }

    public int getBlockId(int x, int y, int z) {
        int index = getArrayIndex(x, y, z);
        int id;
        if ((index >> 1) >= addId.length) {
            id = ids[index] & 0xFF;
        } else {
            if ((index & 1) == 0) {
                id = (short) (((addId[index >> 1] & 0x0F) << 8) + (ids[index] & 0xFF));
            } else {
                id = (short) (((addId[index >> 1] & 0xF0) << 4) + (ids[index] & 0xFF));
            }
        }
        return id;
    }

    public int getData(int x, int y, int z) {
        return data[getArrayIndex(x, y, z)];
    }

    @Override
    public BaseBlock getBlockAt(int x, int y, int z) {
        if (empty) {
            return new BaseBlock(0);
        }
        int id = getBlockId(x, y, z);

        int blockData = getData(x, y, z);
        CompoundTag tag = getTileEntityData(x, y, z);

        if (id < 0 || blockData < 0) {
//            System.out.println("id: " + id + " data: " + blockData);
            return null;
        }

        return new BaseBlock(id, blockData, tag);
    }

    public int numBlocks() {
        Vector2D size = bsc.getDimension();
        return size.getBlockX() * sectionHeight * size.getBlockZ();
    }

    public final void setDirty(boolean dirty) {
        bsc.setDirty(dirty);
    }

    @Override
    public void setBlockAt(int x, int y, int z, BaseBlock block) {
        if (empty) {
            int blocks = numBlocks();
            this.ids = new byte[blocks];
            this.addId = new byte[blocks];
            this.data = new byte[blocks];
            this.empty = false;
        }

        int index = getArrayIndex(x, y, z);
        setDirty(true);

        this.ids[index] = (byte) block.getType();
        this.data[index] = (byte) block.getData();

        if (block.hasNbtData()) {
            setTileEntityData(x, y, z, block.getNbtData());
        }

        if (block.getType() > 255) {
            if (addId == null) { // Lazily create section
                addId = new byte[(ids.length >> 1) + 1];
            }
            addId[index >> 1] = (byte) (((index & 1) == 0)
                    ? addId[index >> 1] & 0xF0 | (block.getType() >> 8) & 0xF
                    : addId[index >> 1] & 0xF | ((block.getType() >> 8) & 0xF) << 4);
        }

    }

    @Override
    public void setBlockAt(Vector position, BaseBlock block) {
        this.setBlockAt(position.getBlockX(), position.getBlockY(), position.getBlockZ(), block);
    }

    @Override
    public Map<String, Tag> serialize() {
        Map<String, Tag> rootMap = Maps.newHashMap();

        if (!isEmpty()) {
            if (sectionHeight != BlockStoreRegion.DEFAULT_SIZE) {
                rootMap.put("Height", new ShortTag((short) sectionHeight));
            }

            rootMap.put("Blocks", new ByteArrayTag(ids));
            rootMap.put("Data", new ByteArrayTag(data));

            if (addId != null) {
                rootMap.put("AddId", new ByteArrayTag(addId));
            }
        }
        return rootMap;
    }

}
