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
package com.chingo247.structureapi.plan.io.parser;

import com.chingo247.structureapi.placement.block.SchematicPlacement;
import com.chingo247.structureapi.plan.io.document.PlacementElement;
import com.chingo247.structureapi.plan.io.exception.PlanException;
import com.chingo247.structureapi.placement.PlacementTypes;
import com.chingo247.structureapi.schematic.Schematic;
import com.chingo247.structureapi.schematic.SchematicManager;
import com.sk89q.worldedit.Vector;
import java.io.File;
import org.dom4j.Document;

/**
 *
 * @author Chingo
 */
public class SchematicPlacementParser implements PlacementParser<SchematicPlacement> {

    @Override
    public SchematicPlacement parse(File xmlFile, Document d, PlacementElement placementElement) {
        String schematicPath = placementElement.getSchematic();
        if (schematicPath == null) {
            throw new PlanException("Element '" + placementElement.getElementName() + "' on line " + placementElement.getLine() + " doesn't have an element called 'Schematic', error occured in '" + placementElement.getFile().getAbsolutePath() + "'");
        }
        File schematicFile = new File(placementElement.getFile().getParent(), schematicPath);
        if (!schematicFile.exists()) {
            throw new PlanException("Error in '" + placementElement.getFile().getAbsolutePath() + "': File '" + schematicFile.getAbsolutePath() + "' defined in element '<Schematic>' does not exist!");
        }

        Vector pos = placementElement.getPosition();
        
        int direction = placementElement.getAxisOffset();
        
        
        
        SchematicManager sdm = SchematicManager.getInstance();
        Schematic schematic = sdm.getOrLoadSchematic(schematicFile);
        direction += schematic.getAxisOffset();
        
        return new SchematicPlacement(schematic, direction, pos);
    }

    @Override
    public String getHandlerType() {
        return PlacementTypes.SCHEMATIC;
    }

   
}
