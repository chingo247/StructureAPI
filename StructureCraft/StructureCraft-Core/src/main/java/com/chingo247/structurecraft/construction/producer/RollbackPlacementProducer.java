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
package com.chingo247.structurecraft.construction.producer;

import com.chingo247.structurecraft.construction.contract.safe.SchematicSaveData;
import com.chingo247.structurecraft.construction.contract.safe.SchematicSavePlacement;
import com.chingo247.structurecraft.exeption.StructureException;
import com.chingo247.structurecraft.model.structure.IStructure;
import com.chingo247.structurecraft.placement.block.IBlockPlacement;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author Chingo
 */
public class RollbackPlacementProducer extends BlockPlacementProducer {

    @Override
    public IBlockPlacement produce(IStructure structure) throws StructureException {
        File rollbackSchematic = structure.getRollbackData().getRollbackSchematic();
        if(!rollbackSchematic.exists()) {
            throw new StructureException("Structure doesn't have a rollback schematic");
        }
        SchematicSaveData schematicSaveData;
        try {
            schematicSaveData = SchematicSaveData.load(rollbackSchematic);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return new SchematicSavePlacement(schematicSaveData);
    }
    
}
