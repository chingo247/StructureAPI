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

import com.chingo247.structureapi.placement.RotationalPlacement;
import com.chingo247.structureapi.placement.IPlacement;
import com.chingo247.structureapi.placement.IExportablePlacement;
import com.chingo247.structureapi.plan.io.PlacementXMLConstants;
import com.sk89q.worldedit.Vector;
import org.dom4j.Element;
import org.dom4j.tree.BaseElement;

/**
 *
 * @author Chingo
 * @param <T>
 */
public abstract class AbstractPlacementXMLHandler<T extends IExportablePlacement> implements PlacementXMLHandler<T> {

    @Override
    public Element handle(T placement) {
        Element placementRoot = new BaseElement(PlacementXMLConstants.ROOT_ELEMENT);
//        d.add(placementRoot);

        Vector v = placement.getOffset();
        if (!v.equals(Vector.ZERO)) { // Not equal to default
            Element xElement = new BaseElement(PlacementXMLConstants.X_ELEMENT);
            Element yElement = new BaseElement(PlacementXMLConstants.Y_ELEMENT);
            Element zElement = new BaseElement(PlacementXMLConstants.Z_ELEMENT);

            xElement.setText(String.valueOf(v.getBlockX()));
            yElement.setText(String.valueOf(v.getBlockY()));
            zElement.setText(String.valueOf(v.getBlockZ()));

            placementRoot.add(xElement);
            placementRoot.add(yElement);
            placementRoot.add(zElement);
        }

        if (placement instanceof RotationalPlacement) {
            int rotation = ((RotationalPlacement) placement).getRotation();
            Element directionElement = new BaseElement(PlacementXMLConstants.ROTATION_ELEMENT);
            directionElement.setText(String.valueOf(rotation));
            placementRoot.add(directionElement);
        }
        
        Element typeElement = new BaseElement(PlacementXMLConstants.TYPE_ELEMENT);
        typeElement.setText(placement.getTypeName());
        placementRoot.add(typeElement);
        
        
        return placementRoot;
    }

}
