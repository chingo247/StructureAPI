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
package com.chingo247.structurecraft.construction.contract.safe;

import com.chingo247.settlercraft.core.Direction;
import com.chingo247.structurecraft.placement.block.BlockPlacement;
import com.chingo247.structurecraft.placement.options.PlaceOptions;
import com.chingo247.structurecraft.util.WorldUtil;
import com.google.common.collect.Maps;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.regions.CuboidRegion;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chingo
 */
class SchematicSafeData extends BlockPlacement {

    private static final Logger LOG = Logger.getLogger(SchematicSafeData.class.getName());

    private byte[] done;
    private byte[] ids;
    private byte[] data;
    private byte[] addId;
    private Map<Vector, Map<String, Tag>> tileEntities;
    private Direction direction;

    public SchematicSafeData(Vector size, Direction direction) {
        super(Vector.ZERO, size.getBlockX(), size.getBlockY(), size.getBlockZ());
        int blocks = width * height * length;
        
        System.out.println("Blocks: " + blocks);
        this.direction = direction;
        this.done = new byte[blocks];
        this.ids = new byte[blocks];
        this.data = new byte[blocks];
        this.tileEntities = Maps.newHashMap();
    }

    void setBlockIds(byte[] blockIds) {
        this.ids = blockIds;
    }

    void setAddBlocks(byte[] addBlocks) {
        this.addId = addBlocks;
    }

    void setData(byte[] data) {
        this.data = data;
    }

    void setDone(byte[] done) {
        this.done = done;
    }

    void setTileEntities(Map<Vector, Map<String, Tag>> tileEntities) {
        this.tileEntities = tileEntities;
    }

    byte[] getAddBlocks() {
        return addId;
    }

    byte[] getBlockIds() {
        return ids;
    }

    byte[] getData() {
        return data;
    }

    byte[] getDone() {
        return done;
    }

    public Direction getDirection() {
        return direction;
    }
    
//    @Override
//    public int getRotation() {
//        return WorldUtil.getYaw(direction);
//    }

    Map<Vector, Map<String, Tag>> getTileEntities() {
        return tileEntities;
    }

    @Override
    public BaseBlock getBlock(int x, int y, int z) {
        int index;
//        if (direction == Direction.EAST || direction == Direction.WEST) {
            index = (y * width * length) + (z * width) + x;
//        } else {
//            index = (y * width * length) + (x * length) + z;
//        }

    
        
        if (done[index] == 0) {
            return null;
        }

        
        
        int blockId;
        if ((index >> 1) >= addId.length) { // No corresponding AddBlocks index
                blockId = (short) (ids[index] & 0xFF);
            } else {
                if ((index & 1) == 0) {
                    blockId = (short) (((addId[index >> 1] & 0x0F) << 8) + (ids[index] & 0xFF));
                } else {
                    blockId = (short) (((addId[index >> 1] & 0xF0) << 4) + (ids[index] & 0xFF));
                }
            }
//        System.out.println("BaseBlock: " + blockId + ", BlockData: " + data[index]);


        BaseBlock b = new BaseBlock(blockId, data[index]);

        Map<String, Tag> tileMap = tileEntities.get(new BlockVector(x, y, z));
        if (tileMap != null) {
            b.setNbtData(new CompoundTag(tileMap));
        }

        return b;
    }

    /**
     * Sets the block, but only if it's not already set
     *
     * @param vector The position of the block
     * @param block The block
     * @return True if block has been set, false if block was already set
     */
    public boolean setBlock(Vector vector, BaseBlock block) {
        int index;
        if (direction == Direction.EAST || direction == Direction.WEST) {
            index = (vector.getBlockY() * width * length) + (vector.getBlockZ() * width) + vector.getBlockX();
        } else {
            index = (vector.getBlockY() * width * length) + (vector.getBlockX() * length) + vector.getBlockZ();
        }
        
        System.out.println("Used index: " + ((vector.getBlockY() * width * length) + (vector.getBlockZ() * width) + vector.getBlockX()));
        System.out.println("Other index: " + ((vector.getBlockY() * width * length) + (vector.getBlockX() * length) + vector.getBlockZ()));
        
        if (this.done[index] == 0) {
            this.data[index] = (byte) block.getData();
            this.ids[index] = (byte) block.getType();

            if (block.hasNbtData()) {
                CompoundTag tag = block.getNbtData();
                if (tag != null) {
                    Map<String, Tag> map = tag.getValue();
                    this.tileEntities.put(vector, map);
                }
            }
            
            
            

            if (block.getType() > 255) {
                if (addId == null) { // Lazily create section
                    addId = new byte[(ids.length >> 1) + 1];
                }
                addId[index >> 1] = (byte) (((index & 1) == 0)
                        ? addId[index >> 1] & 0xF0 | (block.getType() >> 8) & 0xF
                        : addId[index >> 1] & 0xF | ((block.getType() >> 8) & 0xF) << 4);
            }

            this.done[index] = (byte) 1;
            return true;
        }
        return false;
    }

    @Override
    public BaseBlock getBlock(Vector position) {
        return getBlock(position.getBlockX(), position.getBlockY(), position.getBlockZ());
    }

    @Override
    public void place(EditSession session, Vector pos, PlaceOptions option) {
        try {

            for (int x = 0; x < width; x++) {
                for (int z = 0; z < length; z++) {
                    for (int y = 0; y < height; y++) {
                        BaseBlock b = getBlock(x, y, z);

//                        System.out.println("rollback-block: " + b + ", pos: " + pos.add(x, y, z));

                        if (b != null) {
                            session.rawSetBlock(pos.add(x, y, z), b);
                        }
                    }
                }
            }
//            System.out.println("Width: " + width + ", Height: " + height + ", Length: " + length);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    @Override
    public Vector getOffset() {
        return Vector.ZERO;
    }

    @Override
    public Vector getSize() {
        return new BlockVector(width, height, length);
    }

    @Override
    public CuboidRegion getCuboidRegion() {
        return new CuboidRegion(Vector.ZERO, getSize());
    }

}
