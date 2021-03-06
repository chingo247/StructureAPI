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
package com.chingo247.structureapi.construction.producer;

import com.chingo247.structureapi.exeption.StructureException;
import com.chingo247.structureapi.model.structure.Structure;
import com.chingo247.structureapi.placement.block.DemolishingPlacement;
import com.chingo247.structureapi.placement.block.IBlockPlacement;
import com.chingo247.structureapi.util.RegionUtil;

/**
 *
 * @author Chingo
 */
public class DemolitionPlacementProducer extends BlockPlacementProducer {

    @Override
    public IBlockPlacement produce(Structure structure) throws StructureException {
        DemolishingPlacement placement = new DemolishingPlacement(RegionUtil.getSize(structure.getCuboidRegion()));
        return placement;
    }
    
}
