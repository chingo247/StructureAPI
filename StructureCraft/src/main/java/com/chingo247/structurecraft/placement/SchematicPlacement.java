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

package com.chingo247.structurecraft.placement;

import com.chingo247.structurecraft.placement.interfaces.IBlockPlacement;
import com.chingo247.structurecraft.placement.interfaces.FilePlacement;
import com.chingo247.settlercraft.core.Direction;
import com.chingo247.structurecraft.placement.schematic.Schematic;
import com.chingo247.structurecraft.placement.schematic.FastClipboard;
import com.chingo247.structurecraft.construction.options.BuildOptions;
import com.chingo247.structurecraft.util.WorldUtil;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.regions.CuboidRegion;
import java.io.File;

/**
 *
 * @author Chingo
 */
public class SchematicPlacement extends BlockPlacement<BuildOptions> implements FilePlacement<BuildOptions>, IBlockPlacement<BuildOptions> {

    private final Schematic schematic;
    private FastClipboard clipboard;
    private CuboidRegion clipboardRegion;
    private CuboidRegion placementRegion;

    public SchematicPlacement(Schematic schematic) {
        this(schematic, 0, Vector.ZERO);
    }

    public SchematicPlacement(Schematic schematic, int rotation, Vector position) {
        super(rotation, position, schematic.getWidth(), schematic.getHeight(), schematic.getLength());
        this.schematic = schematic;
        this.clipboard = schematic.getClipboard();
       
        
        Direction currentDirection = WorldUtil.getDirection(getRotation());
        
        this.rotate(clipboard.getyAxisOffset());
        
        Direction newDirection = WorldUtil.getDirection(getRotation());
        
        int width = schematic.getWidth();
        int length = schematic.getLength();
        int height = schematic.getHeight();
        
        if(((currentDirection == Direction.EAST || currentDirection == Direction.WEST) && (newDirection == Direction.NORTH || newDirection == Direction.SOUTH))
                || ((currentDirection == Direction.NORTH || currentDirection == Direction.SOUTH) && (newDirection == Direction.WEST || newDirection == Direction.EAST))) {
            int temp = schematic.getWidth();
            width = schematic.getLength();
            length = temp;
        }
        
        this.clipboardRegion = new CuboidRegion(Vector.ZERO, getSize());
        this.placementRegion = new CuboidRegion(Vector.ZERO, new Vector(width, height, length));
    }

    @Override
    public CuboidRegion getCuboidRegion() {
        return placementRegion;
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

    @Override
    public BaseBlock getBlock(Vector position) {
        if(clipboardRegion.contains(position)) {
            BaseBlock b = clipboard.getBlock(position);
            return b;
        }
        return null;
    }

}
