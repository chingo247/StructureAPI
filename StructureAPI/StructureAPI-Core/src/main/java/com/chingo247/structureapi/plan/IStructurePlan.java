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

import com.chingo247.structureapi.placement.Placement;
import com.chingo247.structureapi.plan.flag.Flag;
import java.io.File;
import java.util.Map;

/**
 *
 * @author Chingo
 */
public interface IStructurePlan {
    
    
    
    /**
     * Gets the id of this StructurePlan. Note that the id of the StructurePlan is a hash based on the relative path of the file which
     * represents this StructurePlan and might differ at the next server startup
     * @return The id of this StructurePlan
     */
    String getId();
    /**
     * Gets the description
     * @return The description of this StructurePlan
     */
    String getDescription();
    
    /**
     * Sets the description of this StructurePlan
     * @param description The description to set
     */
    void setDescription(String description);
    
    /**
     * Sets the name of this StructurePlan
     * @param name The StructurePlan
     */
    void setName(String name);
    
    /**
     * Gets the name of the StructurePlan
     * @return The name of the StructurePlan
     */
    String getName();
    
    /**
     * Gets the Category
     * @return The category
     */
    String getCategory();
    
    /**
     * Set the category of this StructurePlan
     * @param category The category to set
     */
    void setCategory(String category);
    
    /**
     * Gets the price of the StructurePlan
     * @return The price of this StructurePlan
     */
    double getPrice();
    
    /**
     * Sets the price of the StructurePlan
     * @param price The price of the StructurePlan
     */
    void setPrice(double price);
    
    /**
     * Gets the placement
     * @return The placement of this StructurePlan
     */
    Placement getPlacement();
    
    
    /**
     * Gets the File that represents this StructurePlan or null if file doesn't exist...
     * @return The file
     */
    File getFile();
    
    boolean hasFlags();
    
    /**
     * Gets a flag
     * @param flag The name of the flag to get
     * @return The flag
     */
    Flag<?> getFlag(String flag);
    
    /**
     * Gets a copy of the flags map
     * @return The flags map
     */
    Map<String, Flag<?>> getFlags();
    
    /**
     * Sets a flag
     * @param flag The flag to set 
     */
    void setFlag(Flag<?> flag);
    
    void save() throws Exception;
    
}
