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
package com.chingo247.structurecraft.construction.save.schematic;

import com.chingo247.structurecraft.placement.block.BlockPlacement;
import com.chingo247.structurecraft.placement.block.IBlockPlacement;
import com.chingo247.structurecraft.placement.options.PlaceOptions;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.regions.CuboidRegion;

/**
 *
 * @author Chingo
 */
public class SchematicSavePlacement extends BlockPlacement implements IBlockPlacement {
    
    private SchematicSaveData schematicSaveData;

    public SchematicSavePlacement(SchematicSaveData saveData) {
        super(saveData.getWidth(), saveData.getHeight(), saveData.getLength());
        this.schematicSaveData = saveData;
    }
    
    @Override
    public BaseBlock getBlock(Vector position) {
        return schematicSaveData.getBlock(position.getBlockX(), position.getBlockY(), position.getBlockZ());
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
