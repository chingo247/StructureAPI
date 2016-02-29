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
package com.chingo247.structureapi.plan;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author Chingo
 */
public interface IStructurePlanProvider {
    
    /**
     * Reload a specific plan
     * @param plan The id of the plan
     */
    void reload(String plan);
    
    /**
     * Reloads all the plans
     */
    void reload();
    
    /**
     * Reloads all the plans
     */
    void reload(boolean verbose);
    
    /**
     * Gets all the structure plans
     * @return The structure plans
     */
    List<IStructurePlan> getPlans();
    
    
    
    
    
    /**
     * Returns the plan with the corresponding id or null
     * @param planId The planId
     * @return The plan
     */
    IStructurePlan getPlan(String planId);
    
}
