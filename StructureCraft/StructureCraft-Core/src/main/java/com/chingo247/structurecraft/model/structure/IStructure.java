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
package com.chingo247.structurecraft.model.structure;

import com.chingo247.settlercraft.core.Direction;
import com.chingo247.structurecraft.exeption.StructureException;
import com.chingo247.structurecraft.model.plot.IPlot;
import com.chingo247.structurecraft.plan.interfaces.IStructurePlan;
import com.sk89q.worldedit.Vector;
import java.io.File;
import java.util.Date;

/**
 *
 * @author Chingo
 */
public interface IStructure extends IPlot {

    /**
     * Gets the id of the structure.
     *
     * @return The id
     */
    Long getId();

    /**
     * Gets the name of the structure.
     *
     * @return The name of the structure
     */
    String getName();

    /**
     * Gets the origin of the structure.
     *
     * @return The origin
     */
    Vector getOrigin();

    double getPrice();

    Direction getDirection();

    /**
     * The construction status
     *
     * @return
     */
    ConstructionStatus getStatus();

    /**
     * Gets the structure plan
     *
     * @return The structure plan
     * @throws StructureException if structure doesnt have a plan
     */
    IStructurePlan getStructurePlan() throws StructureException;

    File getDirectory();

    Vector translateRelativeLocation(Vector position);
    
    Vector getRelativePosition(Vector position);
    
    Date getCreatedAt();
    
    Date getCompletedAt();
    
    Date getDeletedAt();

}
