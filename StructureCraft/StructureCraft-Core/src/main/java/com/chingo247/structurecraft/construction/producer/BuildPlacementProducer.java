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

import com.chingo247.structurecraft.exeption.StructureException;
import com.chingo247.structurecraft.exeption.StructurePlanException;
import com.chingo247.structurecraft.model.structure.IStructure;
import com.chingo247.structurecraft.placement.IPlacement;
import com.chingo247.structurecraft.placement.RotationalPlacement;
import com.chingo247.structurecraft.placement.block.IBlockPlacement;

/**
 *
 * @author Chingo
 */
public class BuildPlacementProducer extends BlockPlacementProducer {

    public BuildPlacementProducer() {
    }

    @Override
    public IBlockPlacement produce(IStructure structure) throws StructureException {
        try {
            IPlacement placement = structure.getStructurePlan().getPlacement();
            checkIsBlockPlacement(placement);

            if (placement instanceof RotationalPlacement) {
                RotationalPlacement rt = (RotationalPlacement) placement;
                rt.rotate(structure.getDirection().getRotation());
            }

            return (IBlockPlacement) placement;
        } catch (StructurePlanException ex) {
            throw new RuntimeException(ex);
        }
    }

}