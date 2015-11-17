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
package com.chingo247.structurecraft.plan.io.document;

import com.sk89q.worldedit.Vector;
import java.io.File;
import org.dom4j.Element;

/**
 * A representation of a Vector in XML
 * @author Chingo
 */
public class PositionElement extends LineElement {

    PositionElement(File file, Element element) {
        super(file, element);
    }
    
    private double getPosValue(String xpath) {
        checkNotNull(xpath);
        LineElement element = new LineElement(getFile(), (Element)le.selectSingleNode(xpath));
        element.checkNotEmpty();
        return element.getDoubleValue();
    }
    
    
    
    public double getX() {
        return getPosValue("X");
    }
    
    public double getY() {
        return getPosValue("Y");
    }
    
    public double getZ() {
        return getPosValue("Z");
    }
    
    public Vector getPosition() {
        return new Vector(getX(), getY(), getZ());
    }
    
}
