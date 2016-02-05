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
package com.chingo247.settlercraft.worldguard.protecttion;

import com.chingo247.structurecraft.model.structure.Structure;
import fiber.core.api.service.Service;

/**
 * An interface for plugins that protect regions (e.g. worldguard)
 * @author Chingo
 */
public interface IStructureProtector extends Service {
    
    /**
     * Name of the plugin/service that will protect the structure
     * @return The name
     */
    public String getName();
    
    /**
     * Protects a Structure
     * @param structure 
     */
    public void protect(Structure structure);
    
    /**
     * Removes protection from a structure, requires an active NEO4J transaction
     * @param structure 
     */
    public void removeProtection(Structure structure);
    
    /**
     * Checks whether a structure is protected
     * @param structure
     * @return True if Structure was protected
     */
    public boolean hasProtection(Structure structure);
    
}
