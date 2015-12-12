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
package com.chingo247.structurecraft.construction.plan;

import com.chingo247.structurecraft.construction.IConstructionEntry;
import com.chingo247.structurecraft.construction.IConstructionExecutor;
import com.chingo247.structurecraft.construction.ITaskAssigner;
import com.chingo247.structurecraft.model.structure.IStructure;
import com.chingo247.structurecraft.placement.IPlacement;

/**
 *
 * @author Chingo
 */
public class RollbackPlan extends ConstructionPlan {

    public RollbackPlan(IConstructionExecutor executor, IStructure structure, ITaskAssigner assigner) {
        super(executor, structure, assigner);
    }

    @Override
    public IPlacement getPlacement(IStructure structure) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void register(IConstructionEntry entry) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
