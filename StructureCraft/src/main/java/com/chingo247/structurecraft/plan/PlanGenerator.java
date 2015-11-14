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

import com.chingo247.structurecraft.placement.PlacementTypes;
import com.chingo247.structurecraft.plan.io.PlacementXMLConstants;
import com.chingo247.structurecraft.plan.io.StructurePlanXMLConstants;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.BaseElement;

/**
 *
 * @author Chingo
 */
public class PlanGenerator {
    
    public static void generate(File directory) {
        Iterator<File> fileIterator = FileUtils.iterateFiles(directory, new String[]{"schematic"}, true);
        
        while(fileIterator.hasNext()) {
            File schemaFile = fileIterator.next();
            try {
                generatePlanFromSchematic(schemaFile, directory);
            } catch (IOException ex) {
                Logger.getLogger(PlanGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private static void generatePlanFromSchematic(File file, File rootDirectory) throws IOException {
        String name = FilenameUtils.getBaseName(file.getName());
        File directory = file.getParentFile();
        
        Document d = DocumentHelper.createDocument();
        Element root = new BaseElement(StructurePlanXMLConstants.STRUCTURE_PLAN_ROOT_ELEMENT);
        d.add(root);
        
       
        
        Element nameElement = new BaseElement(StructurePlanXMLConstants.STRUCTURE_PLAN_NAME_ELEMENT);
        nameElement.setText(name);
        root.add(nameElement);
        
        Element priceElement = new BaseElement(StructurePlanXMLConstants.STRUCTURE_PLAN_PRICE_ELEMENT);
        priceElement.setText("0");
        root.add(priceElement);
        
        Element description = new BaseElement(StructurePlanXMLConstants.STRUCTURE_PLAN_DESCRIPTION_ELEMENT);
        description.setText("None");
        root.add(description);
        
        
        Element categoryElement = new BaseElement(StructurePlanXMLConstants.STRUCTURE_PLAN_CATEGORY_ELEMENT);
        String category = rootDirectory.getName().equals(directory.getName()) ? "Default" : directory.getName();
        categoryElement.setText(category);
        root.add(categoryElement);
        
        Element placementElment = new BaseElement(StructurePlanXMLConstants.STRUCTURE_PLAN_PLACEMENT);
        
        Element typeElement = new BaseElement(PlacementXMLConstants.TYPE_ELEMENT);
        typeElement.setText(PlacementTypes.SCHEMATIC);
        
        Element schematicElement = new BaseElement(PlacementXMLConstants.SCHEMATIC_ELEMENT);
        schematicElement.setText(file.getName());
        
        placementElment.add(typeElement);
        placementElment.add(schematicElement);
        
        root.add(placementElment);
        
        File planFile = new File(directory, name + ".xml");
        OutputFormat format = OutputFormat.createPrettyPrint();
        XMLWriter writer = new XMLWriter(new FileWriter(planFile), format);
        writer.write( d );
        writer.close();
    }
    
   
    
}
