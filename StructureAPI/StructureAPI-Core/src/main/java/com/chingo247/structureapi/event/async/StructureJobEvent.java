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
package com.chingo247.structureapi.event.async;

import com.chingo247.structureapi.model.structure.IStructure;
import com.chingo247.structureapi.model.structure.Structure;

/**
 *
 * @author Chingo
 */
public class StructureJobEvent  {
    
    private final int jobId;
    private final IStructure structure;

    public StructureJobEvent(IStructure structure, int jobid) {
        this.jobId = jobid;
        this.structure = structure;
    }

    public final IStructure getStructure() {
        return structure;
    }

    
    

    /**
     * Gets the JobId (AsyncWorldEdit's jobId
     * @return The jobId
     */
    public final int getJobId() {
        return jobId;
    }
    
}
