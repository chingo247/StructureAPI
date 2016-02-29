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

package com.chingo247.structureapi.plan.io.export;

import com.chingo247.structureapi.plan.DefaultStructurePlan;
import com.chingo247.structureapi.placement.IPlacement;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author Chingo
 */
public class PlacementExporter {
    
    public void export(IPlacement p, File destinationDirectory, String fileName, boolean prettyprint) throws IOException, UnsupportedPlacementException {
        File placementPlanFile = new File(destinationDirectory, fileName);
        DefaultStructurePlan plan = new PlacementPlan(placementPlanFile.getAbsolutePath(), placementPlanFile, p);
        plan.setCategory("Other");
        plan.setDescription("None");
        plan.setPrice(0.0);
        plan.setName(fileName);
        StructurePlanExporter spe = new StructurePlanExporter();
        spe.export(plan, destinationDirectory, fileName, prettyprint);
    }
    
    private class PlacementPlan extends DefaultStructurePlan {
        
        PlacementPlan(String id, File planFile, IPlacement placement) {
            super(id, planFile, placement);
        }
        
    }
    
}
