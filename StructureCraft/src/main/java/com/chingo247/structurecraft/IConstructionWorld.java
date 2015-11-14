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
package com.chingo247.structurecraft;

import com.chingo247.structurecraft.exeption.StructureException;
import com.chingo247.structurecraft.exeption.ConstructionZoneException;
import com.chingo247.settlercraft.core.Direction;
import com.chingo247.structurecraft.model.structure.Structure;
import com.chingo247.structurecraft.model.zone.IConstructionZone;
import com.chingo247.structurecraft.placement.interfaces.IPlacement;
import com.chingo247.structurecraft.plan.interfaces.IStructurePlan;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import java.util.UUID;

/**
 *
 * @author Chingo
 */
public interface IConstructionWorld {
    
    Structure createStructure(IStructurePlan structurePlan, Vector position, Direction direction, UUID player) throws StructureException;
    
    Structure createStructure(IPlacement structurePlan, Vector position, Direction direction, UUID player) throws StructureException;
    
    IConstructionZone createConstructionZone(CuboidRegion region) throws ConstructionZoneException;
    
}
