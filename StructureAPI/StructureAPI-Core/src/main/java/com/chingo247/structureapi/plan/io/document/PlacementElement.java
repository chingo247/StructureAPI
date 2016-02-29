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
package com.chingo247.structureapi.plan.io.document;

import com.chingo247.structureapi.plan.io.PlacementXMLConstants;
import com.chingo247.structureapi.plan.io.StructurePlanXMLConstants;
import com.sk89q.worldedit.Vector;
import java.io.File;
import org.dom4j.Element;
import org.dom4j.Node;

/**
 * An element that contains attributes of a placement. Note that this class is 
 * a helper class for retrieving placement attributes, therefore some of these methods may return null
 * @author Chingo
 */
public class PlacementElement extends LineElement {

    
    
    PlacementElement(File file, Element element) {
        super(file, element);
    }
    
    public String getPlacementReferenceId() {
        Node n = le.selectSingleNode(StructurePlanXMLConstants.STRUCTURE_PLAN_ID_ELEMENT);
        if(n == null) return null;
        LineElement simpleElement = new LineElement(getFile(), (Element) n);
        simpleElement.checkNotEmpty();
        return simpleElement.getTextValue().trim();
    }
    
    public String getPlacementReferencePath() {
        Node n = le.selectSingleNode(StructurePlanXMLConstants.STRUCTURE_PLAN_RELATIVE_PATH_ELEMENT);
        if(n == null) return null;
        LineElement simpleElement = new LineElement(getFile(), (Element) n);
        simpleElement.checkNotEmpty();
        return simpleElement.getTextValue().trim();
    }

    public String getType() {
        checkNotNull(PlacementXMLConstants.TYPE_ELEMENT);
        Node n = le.selectSingleNode(PlacementXMLConstants.TYPE_ELEMENT);
        LineElement simpleElement = new LineElement(getFile(), (Element) n);
        simpleElement.checkNotEmpty();
        return simpleElement.getTextValue().trim();
    }

    public int getAxisOffset() {
        Node dirNode = le.selectSingleNode(PlacementXMLConstants.ROTATION_ELEMENT);
        if (dirNode != null) {
            LineElement dirElement = new LineElement(getFile(), (Element) dirNode);
            dirElement.checkNotEmpty();
            int direction = dirElement.getIntValue();
            return direction;
        }
        return 0; // Default
    }
    
    private static float normalizeYaw(float yaw) {
        float ya = yaw;
        if(yaw > 360) {
            int times = (int)((ya - (ya % 360)) / 360);
            int normalizer = times * 360;
            ya -= normalizer;
        } else if (yaw < -360) {
            ya = Math.abs(ya);
            int times = (int)((ya - (ya % 360)) / 360);
            int normalizer = times * 360;
            ya = yaw + normalizer;
        }
        return ya;
    } 

    public Vector getPosition() {
        Node posNode = le.selectSingleNode(PlacementXMLConstants.POSITION_ELEMENT);
        if (posNode != null) {
            PositionElement posElement = new PositionElement(getFile(), (Element) posNode);
            return posElement.getPosition();
        }
        return Vector.ZERO;
    }

    public String getSchematic() {
        Node schematicNode = le.selectSingleNode(PlacementXMLConstants.SCHEMATIC_ELEMENT);
        if (schematicNode == null) {
            return null;
        }
        LineElement simpleElement = new LineElement(getFile(), (Element) schematicNode);
        simpleElement.checkNotEmpty();
        return simpleElement.getTextValue().trim();
    }

}
