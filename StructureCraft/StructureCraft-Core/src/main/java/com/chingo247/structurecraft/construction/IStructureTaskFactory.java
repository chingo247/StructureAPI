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
package com.chingo247.structurecraft.construction;

import com.chingo247.structurecraft.model.structure.Structure;
import com.chingo247.structurecraft.construction.options.PlaceOptions;
import com.chingo247.structurecraft.exeption.StructureException;
import com.sk89q.worldedit.EditSession;
import java.util.UUID;

/**
 *
 * @author Chingo
 */
public interface IStructureTaskFactory {
    
    /**
     * Creates a task that will build the structure
     * @param session The editsession
     * @param player The player UUID or any other random UUID if no tracking is required
     * @param structure The structure
     * @return The structure task
     * @throws Exception 
     */
    public StructureTask build(EditSession session, UUID player, Structure structure) throws StructureException;
    
    /**
     * Creates a task that will build the structure
     * @param session The editsession
     * @param player The player UUID or any other random UUID if no tracking is required
     * @param structure The structure
     * @param options The buildOptions to use
     * @return The structure task
     * @throws Exception 
     */
    public StructureTask build(EditSession session, UUID player, Structure structure, PlaceOptions options) throws StructureException;
    
    /**
     * Creates a task that will demolish the structure. After the structure is demolished the structure will be removed
     * If available the area will be restored to the backup that was made before the structure was placed, otherwise the whole area will be removed.
     * @param session The editsession
     * @param player The player UUID or any other random UUID if no tracking is required
     * @param structure The structure
     * @return The structure task
     * @throws Exception 
     */
    public StructureTask demolish(EditSession session, UUID player, Structure structure) throws StructureException;
    
    /**
     * Creates a task that will demolish the structure. After the structure is demolished the structure will be removed.
     * If available the area will be restored to the backup that was made before the structure was placed, otherwise the whole area will be removed.
     * 
     * @param session The editsession
     * @param player The player UUID or any other random UUID if no tracking is required
     * @param structure The structure
     * @param options
     * @return The structure task
     * @throws Exception 
     */
    public StructureTask demolish(EditSession session, UUID player, Structure structure, PlaceOptions options) throws StructureException;
    

    
}
