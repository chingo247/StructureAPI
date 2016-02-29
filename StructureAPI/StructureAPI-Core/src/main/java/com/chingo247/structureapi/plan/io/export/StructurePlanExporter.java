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

import com.chingo247.structureapi.placement.IPlacement;
import com.chingo247.structureapi.placement.IExportablePlacement;
import com.chingo247.structureapi.placement.PlacementAPI;
import com.chingo247.structureapi.plan.IStructurePlan;
//import com.chingo247.structureapi.structure.plan.SubStructuresPlan;
import java.io.File;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.tree.BaseElement;
import com.chingo247.structureapi.plan.io.StructurePlanXMLConstants;
import com.google.common.base.Preconditions;
import java.io.FileWriter;
import java.io.IOException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

/**
 *
 * @author Chingo
 */
public class StructurePlanExporter {
    
    public void export(IStructurePlan plan, File destinationDirectory, String fileName, boolean prettyPrint) throws IOException, UnsupportedPlacementException {
        Preconditions.checkArgument(destinationDirectory.isDirectory());
        
        IPlacement placement = plan.getPlacement();
        if( !(placement instanceof IExportablePlacement)) {
            throw new UnsupportedPlacementException("Placement does not implement IWriteablePlacement");
        }
        
        
        
        Document d = DocumentHelper.createDocument();
        
        Element root = new BaseElement(StructurePlanXMLConstants.STRUCTURE_PLAN_ROOT_ELEMENT);
        d.add(root);
        
        Element nameElement = new BaseElement(StructurePlanXMLConstants.STRUCTURE_PLAN_NAME_ELEMENT);
        nameElement.setText(plan.getName());
        root.add(nameElement);
        
        Element priceElement = new BaseElement(StructurePlanXMLConstants.STRUCTURE_PLAN_PRICE_ELEMENT);
        priceElement.setText(String.valueOf(plan.getPrice()));
        root.add(priceElement);
        
        Element categoryElement = new BaseElement(StructurePlanXMLConstants.STRUCTURE_PLAN_CATEGORY_ELEMENT);
        categoryElement.setText(plan.getCategory() == null ? "None" : plan.getCategory());
        root.add(categoryElement);
        
        Element descriptionElement = new BaseElement(StructurePlanXMLConstants.STRUCTURE_PLAN_DESCRIPTION_ELEMENT);
        descriptionElement.setText(plan.getDescription() == null ? "None" : plan.getDescription());
        root.add(descriptionElement);
        
        
        
        Element placementElement = PlacementAPI.getInstance().handle((IExportablePlacement) plan.getPlacement());
        root.add(placementElement);
        
//        if (plan instanceof SubStructuresPlan) {
//            SubStructuresPlan ssp = (SubStructuresPlan) plan;
//            
//            Element substructuresElement = new BaseElement(StructurePlanXMLConstants.STRUCTURE_PLAN_SUBSTRUCTURES);
//            root.add(substructuresElement);
//            
//            for(Placement p : ssp.getSubPlacements()) {
//                try {
//                    Element e = PlacementAPI.getInstance().handle(p);
//                    e.setName(StructurePlanXMLConstants.STRUCTURE_PLAN_SUBSTRUCTURE);
//                    substructuresElement.add(e);
//                } catch (PlacementException ex) {
//                    System.err.println(ex.getMessage());
//                }
//            }
//            
//            int index = 0;
//            for(StructurePlan p : ssp.getSubStructurePlans()) {
//                File exportPlan = new File(destinationDirectory, p.getFile().getName() + "-" + index);
//                
//                try {
//                    export(plan, destinationDirectory, exportPlan.getName(), prettyPrint);
//                } catch (Exception e){
//                    continue;
//                }
//                
//                Element substructureElement = new BaseElement(StructurePlanXMLConstants.STRUCTURE_PLAN_SUBSTRUCTURE);
//                
//                // TODO add position + direction
//                
//                Element typeElement = new BaseElement(PlacementXMLConstants.PLACEMENT_TYPE_ELEMENT);
//                typeElement.setText(PlacementTypes.EMBEDDED);
//                substructureElement.add(typeElement);
//                
//                Element pathElement = new BaseElement(StructurePlanXMLConstants.STRUCTURE_PLAN_RELATIVE_PATH_ELEMENT);
//                pathElement.setText(exportPlan.getName());
//                substructureElement.add(pathElement);
//                
//                substructuresElement.add(substructureElement);
//                
//            }
//            
//        }
        
      
            OutputFormat format;
            if(prettyPrint) {
                format = OutputFormat.createPrettyPrint();
            } else {
                format = OutputFormat.createCompactFormat();
            }
            XMLWriter writer = new XMLWriter(new FileWriter(new File(destinationDirectory, fileName)), format);
            writer.write(d);
            writer.close();
        
        
        
    }
    
}
