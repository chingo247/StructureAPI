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
package com.chingo247.structureapi.plan.io.handlers;

import com.chingo247.structureapi.placement.block.SchematicPlacement;
import com.chingo247.structureapi.plan.io.PlacementXMLConstants;
import org.dom4j.Element;
import org.dom4j.tree.BaseElement;

/**
 *
 * @author Chingo
 */
public class SchematicPlacementXMLHandler extends AbstractPlacementXMLHandler<SchematicPlacement> {

    
    @Override
    public Element handle(SchematicPlacement placement) {
       Element placementRoot = super.handle(placement);
       
       // add the schematic
       Element schematicElement = new BaseElement(PlacementXMLConstants.SCHEMATIC_ELEMENT);
       schematicElement.setText(placement.getSchematic().getFile().getName());
       placementRoot.add(schematicElement);
       return placementRoot;
    }
    
}
