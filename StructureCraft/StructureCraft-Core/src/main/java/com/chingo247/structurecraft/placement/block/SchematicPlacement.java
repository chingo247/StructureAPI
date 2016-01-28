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

package com.chingo247.structurecraft.placement.block;

import com.chingo247.structurecraft.placement.FilePlacement;
import com.chingo247.structurecraft.placement.IExportablePlacement;
import com.chingo247.structurecraft.placement.PlacementTypes;
import com.chingo247.structurecraft.schematic.Schematic;
import com.chingo247.structurecraft.schematic.FastClipboard;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import java.io.File;

/**
 *
 * @author Chingo
 */
public class SchematicPlacement extends BlockPlacement implements FilePlacement, IBlockPlacement, IExportablePlacement {

    private final Schematic schematic;
    private FastClipboard clipboard;

    public SchematicPlacement(Schematic schematic) {
        this(schematic, 0, Vector.ZERO);
    }

    public SchematicPlacement(Schematic schematic, int rotation, Vector position) {
        super(rotation, position, schematic.getWidth(), schematic.getHeight(), schematic.getLength());
        this.schematic = schematic;
        this.clipboard = schematic.getClipboard();
//        Direction newDirection = WorldUtil.getDirection(getRotation());
        
//        int width = schematic.getWidth();
//        int length = schematic.getLength();
//        int height = schematic.getHeight();
        
//        if(((currentDirection == Direction.EAST || currentDirection == Direction.WEST) && (newDirection == Direction.NORTH || newDirection == Direction.SOUTH))
//                || ((currentDirection == Direction.NORTH || currentDirection == Direction.SOUTH) && (newDirection == Direction.WEST || newDirection == Direction.EAST))) {
//            int temp = schematic.getWidth();
//            width = schematic.getLength();
//            length = temp;
//        }
        
    }
    
    public Schematic getSchematic() {
        return schematic;
    }

    @Override
    public String getTypeName() {
        return PlacementTypes.SCHEMATIC;
    }

    @Override
    public File[] getFiles() {
        return new File[]{schematic.getFile()};
    }
    
    public boolean hasBlock(Vector position) {
        return clipboard.hasBlock(position.getBlockX(), position.getBlockY(), position.getBlockZ());
    }

    @Override
    public BaseBlock getBlock(Vector position) {
        BaseBlock b = clipboard.getBlock(position);
        return b;
    }

  
    
    

}
