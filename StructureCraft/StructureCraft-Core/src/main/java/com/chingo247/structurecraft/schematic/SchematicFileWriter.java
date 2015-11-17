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

import com.google.common.collect.Lists;
import com.sk89q.jnbt.ByteArrayTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.NBTOutputStream;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import static javax.swing.Spring.height;
import static javax.swing.Spring.width;
import static org.parboiled.common.StringUtils.length;

/**
 *
 * @author Chingo
 */
public class SchematicFileWriter {

    private static final int MAX_SIZE = Short.MAX_VALUE - Short.MIN_VALUE;
    private int width, length, height;
    private byte[] blockIds;
    private byte[] blockData;
    private byte[] addBlocks;
    private List<Tag> tileEntities;

    public SchematicFileWriter(Vector size) throws SchematicWriteException {
        this.width = size.getBlockX();
        this.height = size.getBlockY();
        this.length = size.getBlockZ();
        
        this.tileEntities = Lists.newArrayList();
        this.blockIds = new byte[width * height * length];
        this.blockData = new byte[width * height * length];
        this.addBlocks = new byte[0];
        
        checkSizeTooBig(new CuboidRegion(Vector.ZERO, size));
        checkSizeTooSmall(new CuboidRegion(Vector.ZERO, size));
    }
    
    public SchematicFileWriter(CuboidRegion region) throws SchematicWriteException {
        this(region.getMaximumPoint().subtract(region.getMinimumPoint()).add(Vector.ONE));
    }
    
    public void setBlockId(int blockId, int x, int y, int z) {
        this.blockIds[index(x, y, z)] = (byte) blockId;
    }
    
    public void setBlockData(int blockData, int x, int y, int z) {
        this.blockIds[index(x, y, z)] = (byte) blockData;
    }
    
    private int index(int x, int y, int z) {
        return y * width * length + z * width + x;
    }

    public final void checkSizeTooSmall(CuboidRegion region) throws SchematicWriteException {
        Vector size = region.getMaximumPoint().subtract(region.getMinimumPoint()).add(Vector.ONE);
        int width = size.getBlockX();
        int height = size.getBlockY();
        int length = size.getBlockZ();

        if (width < 1) {
            throw new SchematicWriteException("Width too small for a schematic .schematic, (width < 1)");
        }

        if (height < 1) {
            throw new SchematicWriteException("Height too small for a schematic .schematic, (height < 1)");
        }

        if (length < 1) {
            throw new SchematicWriteException("Length too small for a schematic .schematic, (length < 1)");
        }

    }

    public final void checkSizeTooBig(CuboidRegion region) throws SchematicWriteException {
        Vector size = region.getMaximumPoint().subtract(region.getMinimumPoint()).add(Vector.ONE);
        int width = size.getBlockX();
        int height = size.getBlockY();
        int length = size.getBlockZ();

        if (width > MAX_SIZE) {
            throw new SchematicWriteException("Width of region too large for a .schematic");
        }
        if (height > MAX_SIZE) {
            throw new SchematicWriteException("Height of region too large for a .schematic");
        }
        if (length > MAX_SIZE) {
            throw new SchematicWriteException("Length of region too large for a .schematic");
        }

    }

    public void write(NBTOutputStream outputStream) throws IOException {
        HashMap<String, Tag> schematic = new HashMap<>();
        schematic.put("Width", new ShortTag((short) width));
        schematic.put("Length", new ShortTag((short) length));
        schematic.put("Height", new ShortTag((short) height));
        schematic.put("Blocks", new ByteArrayTag(blockIds));
        schematic.put("Data", new ByteArrayTag(blockData));
        CompoundTag schematicTag = new CompoundTag(schematic);
        outputStream.writeNamedTag("Schematic", schematicTag);
    }

    public static void main(String[] args) {
        byte[] bytes = new byte[1000];
        
        for(int i = 0; i < bytes.length; i++) {
            System.out.println("byte: " + bytes[i]);
        }
    }

}
