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

import com.chingo247.settlercraft.core.util.XXHasher;
import java.io.File;

/**
 *
 * @author Chingo
 */
public abstract class AbstractStructurePlan implements IStructurePlan {

    private final String id;
    private String name, category, description;
    private double price;
    private final String hash;
    private final File file;
    
    protected AbstractStructurePlan(File planFile) {
        this(String.valueOf(new XXHasher().hash32String(planFile.getAbsolutePath())), planFile);
    }

    protected AbstractStructurePlan(String id, File planFile) {
        this.file = planFile;
        this.hash = String.valueOf(new XXHasher().hash32String(planFile.getAbsolutePath()));
        this.price = 0.0d;
        this.category = "Default";
        this.id = id;
    }
    
    @Override
    public final String getId() {
        return id;
    }
    
    @Override
    public String getDescription() {
        if(description == null) {
            return "None";
        }
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }
    

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getCategory() {
        return category;
    }

    @Override
    public void setCategory(String category) {
        this.category = category;
    }
    
    @Override
    public double getPrice() {
        return price;
    }
    
    @Override
    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public File getFile() {
        return file;
    }
    
   
    protected String getPathHash() {
        return hash;
    }

    protected String hash(File f) {
        return String.valueOf(new XXHasher().hash32String(f.getAbsolutePath()));
    }

}
