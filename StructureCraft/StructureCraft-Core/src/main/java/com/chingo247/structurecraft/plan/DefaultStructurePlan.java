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
package com.chingo247.structurecraft.plan;

import com.chingo247.structurecraft.placement.IPlacement;
import com.chingo247.structurecraft.plan.io.export.StructurePlanExporter;
import com.chingo247.structurecraft.plan.io.export.UnsupportedPlacementException;
import java.io.File;
import java.io.IOException;

/**
 * This class defines StructurePlans that exist of only one placement
 * @author Chingo
 */
public class DefaultStructurePlan extends AbstractStructurePlan{
    
    private final IPlacement placement;

    public DefaultStructurePlan(String id, File planFile, IPlacement placement) {
        super(id, planFile);
        this.placement = placement;
    }

    @Override
    public IPlacement getPlacement() {
        return placement;
    }

    @Override
    public synchronized void save() throws IOException, UnsupportedPlacementException {
        StructurePlanExporter exporter = new StructurePlanExporter();
        exporter.export(this, getFile().getParentFile(), getName() + ".xml", true);
    }
    
}
