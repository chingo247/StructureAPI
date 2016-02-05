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
package com.chingo247.structureapi.construction;

import com.chingo247.structureapi.model.structure.IStructure;

/**
 *
 * @author chingo
 */
public interface IContractor {

    void submit(final IStructure structure, final IContract contract);

//    IConstruction newBuildPlan(IStructure structure) throws StructureException, StructurePlanException;
//
//    IConstruction newDemolitionPlan(IStructure structure) throws StructureException, StructurePlanException;
//
//    IConstruction newRollbackPlan(IStructure structure) throws StructureException, Exception;
//
//    IConstruction newSaveBuildPlan(IStructure structure) throws StructureException, StructurePlanException;
//
//    IConstruction newSaveDemolitionPlan(IStructure structure) throws StructureException, StructurePlanException;

    void purge(IStructure structure);

    void remove(IStructureEntry entry);

}
