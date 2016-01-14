/*
 * Copyright (C) 2016 ching
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
package com.chingo247.structurecraft.construction;

import com.chingo247.structurecraft.construction.actions.Construction;
import com.chingo247.structurecraft.construction.actions.IConstruction;
import com.chingo247.structurecraft.exeption.StructureException;
import com.chingo247.structurecraft.exeption.StructurePlanException;
import com.chingo247.structurecraft.model.structure.IStructure;

/**
 *
 * @author ching
 */
public interface IContractor {

    void execute(final Construction plan);

    IConstruction newBuildPlan(IStructure structure) throws StructureException, StructurePlanException;

    IConstruction newDemolitionPlan(IStructure structure) throws StructureException, StructurePlanException;

    IConstruction newRollbackPlan(IStructure structure) throws StructureException, Exception;

    IConstruction newSaveBuildPlan(IStructure structure) throws StructureException, StructurePlanException;

    IConstruction newSaveDemolitionPlan(IStructure structure) throws StructureException, StructurePlanException;

    void purge(IStructure structure);

    void remove(IStructureEntry entry);

    /**
     * Checks if a structure supports construction
     * @param structure The structure
     * @return True if it supports construction
     * @throws StructurePlanException May throw exception if the reading of the StructurePlan fails
     */
    boolean supportsConstruction(IStructure structure) throws StructurePlanException;
    
}
