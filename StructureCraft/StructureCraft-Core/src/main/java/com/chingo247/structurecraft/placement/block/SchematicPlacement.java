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
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import java.io.File;

/**
 *
 * @author Chingo
 */
public class SchematicPlacement extends BlockPlacement implements FilePlacement, IBlockPlacement, IExportablePlacement {

    /**
     * Earlier since SettlerCraft 2.1 there is an option to give an extra offset. Unfortunately the offset was always wrong...
     * as it was meant for schematics that have an orientation of 0 degrees, while in reality schematics have an orientation of -90 degrees by default.
     * To not screw up structures, this fix has been implemented...
     */
    private static final int DEFAULT_FIXED_OFFSET = 90;
    private final Schematic schematic;
    private FastClipboard clipboard;

    public SchematicPlacement(Schematic schematic) {
        this(schematic, 0, Vector.ZERO);
    }

    public SchematicPlacement(Schematic schematic, int axisOffset, Vector position) {
        super(DEFAULT_FIXED_OFFSET + axisOffset, position, schematic.getWidth(), schematic.getHeight(), schematic.getLength());
        this.schematic = schematic;
        this.clipboard = schematic.getClipboard();
    }
    
    @Override
    public Vector getSize() {
        return new BlockVector(clipboard.getWidth(), clipboard.getHeight(), clipboard.getLength()); //To change body of generated methods, choose Tools | Templates.
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
