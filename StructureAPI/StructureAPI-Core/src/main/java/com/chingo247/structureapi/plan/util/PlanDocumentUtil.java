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
package com.chingo247.structureapi.plan.util;

import com.chingo247.structureapi.plan.io.exception.PlanException;
import fiber.core.impl.xml.located.LocatedElement;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.math.NumberUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

/**
 *
 * @author Chingo
 */
public class PlanDocumentUtil {
    
    private PlanDocumentUtil() {
    }
    
    public static final String ROOT_ELEMENT = "StructurePlan";
    public static final String SCHEMATIC_ELEMENT = "Schematic";
    public static final String STRUCTURELOT_ELEMENT = "StructureLot";
    public static final String GENERATED_STRUCTURE_ELEMENT = "Generated";
    public static final String WIDTH_ELEMENT = "Width";
    public static final String HEIGHT_ELEMENT = "Height";
    public static final String LENGTH_ELEMENT = "Length";
    public static final String SUBSTRUCTURES = "SubStructures";
    public static final String SUBSTRUCTURE = "SubStructure";
    public static final String POSITION = "Position";
    public static final String DIRECTION = "Direction";
    
    private static void checkLocated(Element d) {
        if(!(d instanceof LocatedElement)) {
            throw new IllegalArgumentException("Element was not of type LocatedElement, use "+DocumentUtil.class.getSimpleName()+"#read() to retrieve an allowed instance of document");
        }
    }
    
    public void checkHasNumber(Element e) {
        checkLocated(e);
        if(!NumberUtils.isNumber(e.getText())) {
           LocatedElement le = (LocatedElement) e;
           throw new IllegalArgumentException("Value of element <"+e.getName()+"> on line " + le.getLine() + " is not a number");
        }
    }
    
    public void checkNotNull(Element e, String xpath) {
        checkLocated(e);
        Node notNull = e.selectSingleNode(xpath);
        
        if(notNull == null) {
            String element = xpath.substring(xpath.lastIndexOf("/"));
            LocatedElement le = (LocatedElement) e;
            throw new PlanException("Expected element <"+element+"> within <" + e.getName() +"> on line " +le.getLine());
        }
    }
    
    public void checkNullOrNotEmpty(Element e, String xpath) {
        checkLocated(e);
        Node notNull = e.selectSingleNode(xpath);
        
        if(notNull != null && notNull.getText().trim().isEmpty()) {
            String element = xpath.substring(xpath.lastIndexOf("/"));
            LocatedElement le = (LocatedElement) e;
            throw new PlanException("No value for element <"+element+"> within <" + e.getName() +"> on line " +le.getLine());
        }
    }
    
    

    

    public static boolean isStructurePlan(Document d) {
        return d.getRootElement().getName().equals(ROOT_ELEMENT);
    }

    public static boolean isStructurePlan(File structurePlanFile) {
        SAXReader reader = new SAXReader();
        try {
            Document d = reader.read(structurePlanFile);
            return isStructurePlan(d);
        } catch (DocumentException ex) {
            Logger.getLogger(PlanDocumentUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

  
    

    
}
