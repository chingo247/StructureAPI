package com.chingo247.structureapi.plan;
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

import com.chingo247.structureapi.placement.IPlacement;
import java.util.Collection;

/**
 *
 * @author Chingo
 */
public interface ISubstructurePlan extends IStructurePlan {
    
    /**
     * Removes a Placement from this StructurePlan
     * @param placement The Placement to remove
     * @return True if Placement was successfully removed
     */
    public boolean removePlacement(IPlacement placement);
    
    /**
     * Adds a placement to this StructurePlan
     * @param placement The placement to add
     */
    public void addPlacement(IPlacement placement);
    
    /**
     * Adds a StructurePlan to this StructurePlan
     * @param plan The StructurePlan to add
     */
    public void addStructurePlan(IStructurePlan plan);
    
    /**
     * Removes a StructurePlan from this StructurePlan
     * @param plan The StructurePlan to remove
     * @return True if StructurePlan was successfully removed
     */
    public boolean removeStructurePlan(IStructurePlan plan);
    
    
    /**
     * Gets all the SubstructurePlans of this StructurePlan.
     * @return The SubstructurePlans
     */
    public Collection<? extends IStructurePlan> getSubStructurePlans();
    
    /**
     * Gets all the SubPlacement of this StructurePlan
     * @return The SubPlacements
     */
    public Collection<? extends IPlacement> getSubPlacements();
    
    
    public ISubstructurePlan getParent();
    
}
