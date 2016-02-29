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

import com.chingo247.settlercraft.core.util.XXHasher;
import com.chingo247.structureapi.placement.PlacementException;
import com.chingo247.structureapi.plan.flag.BooleanFlag;
import com.chingo247.structureapi.plan.flag.DoubleFlag;
import com.chingo247.structureapi.plan.flag.Flag;
import com.chingo247.structureapi.plan.flag.IntegerFlag;
import com.chingo247.structureapi.plan.flag.StringFlag;
import com.google.common.base.Preconditions;
import fiber.core.impl.BuildContextImpl;
import fiber.core.impl.xml.ModelReader;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import com.chingo247.structureapi.plan.io.StructurePlanXMLConstants;
import com.chingo247.structureapi.plan.io.exception.PlanException;
import com.google.common.collect.Maps;
import java.util.Map;
import org.apache.commons.lang.math.NumberUtils;

/**
 *
 * @author Chingo
 */
public class StructurePlanDocument extends LineElement {

    private final File file;
    private final Document doc;

    StructurePlanDocument(File f, Document d) {
        super(f, d.getRootElement());
        Preconditions.checkNotNull(f);
        Preconditions.checkNotNull(d);

        this.file = f;
        this.doc = d;
    }

    public Map<String, Flag<?>> getFlags() {
        Map<String, Flag<?>> flags = Maps.newHashMap();
        List<?> nodeList = le.selectNodes(StructurePlanXMLConstants.STRUCTURE_PLAN_FLAGS_ELEMENT + "/" + StructurePlanXMLConstants.STRUCTURE_PLAN_FLAG_ELEMENT);
        for (Object o : nodeList) {
            LineElement flagNode = new LineElement(file, (Element) o);
            Flag<?> f = makeFlag(flagNode);
            if(flags.get(f.getName()) != null) {
                throw new PlanException("Duplicate flag '" + f.getName() + "' in '" + getFile().getAbsolutePath() + "'");
            }
            flags.put(f.getName(), f);
        }
        return flags;
    }

    private Flag<?> makeFlag(LineElement node) {
        String name;
        String value;

        Element e = node.getElement();
        if (e.attribute("name") != null) {
            if(e.attribute("value") == null) {
                throw new PlanException("No 'value' attribute for element in '" + getFile().getAbsolutePath() + "' on line " + node.getLine());
            }
            
            name = e.attributeValue("name");
            value = e.attributeValue("value");

        } else if (e.selectSingleNode("Name") != null) {
            if(e.selectSingleNode("Name") == null) {
                throw new PlanException("No 'Value' element within element of '" + getFile().getAbsolutePath() + "' on line " + node.getLine());
            }
            
            Node nameNode = e.selectSingleNode("Name");
            Node valueNode = e.selectSingleNode("Value");
            
            name = nameNode.getStringValue();
            value = valueNode.getStringValue();
        } else {
            throw new PlanException("Invalid flag element in '" + getFile().getAbsolutePath() + "' on line " + node.getLine() + ": No 'Name' or 'Value' defined");
        }
        
        
        if(name.trim().isEmpty()) {
            throw new PlanException("Name was emtpy in '" + getFile().getAbsolutePath() + "' on line " + node.getLine());
        }
        
        if(value.trim().isEmpty()) {
            throw new PlanException("Value was emtpy in '" + getFile().getAbsolutePath() + "' on line " + node.getLine());
        }
        

        if (NumberUtils.isNumber(value)) {
            try {
                double d = Double.parseDouble(value);
                return new DoubleFlag(name, d);
            } catch (NumberFormatException nfe) {
                Integer i = Integer.parseInt(value);
                return new IntegerFlag(name, i);
            }
        } else if (value.equalsIgnoreCase("false") || value.equalsIgnoreCase("true")) {
            return new BooleanFlag(name, value.equalsIgnoreCase("true"));
        } else {
            return new StringFlag(name, value);
        }
    }

    public boolean hasSubStructureElements() {
        return le.selectSingleNode(StructurePlanXMLConstants.STRUCTURE_PLAN_SUBSTRUCTURES) != null;
    }

    public List<SubStructureElement> getSubStructureElements() {
        List<Node> nodes = le.selectNodes(StructurePlanXMLConstants.STRUCTURE_PLAN_SUBSTRUCTURES + "/" + StructurePlanXMLConstants.STRUCTURE_PLAN_SUBSTRUCTURE);
        List<SubStructureElement> elements = new ArrayList<>(nodes.size());
        for (Node n : nodes) {
            elements.add(new SubStructureElement(getFile(), (Element) n));
        }
        return elements;
    }

    public String getReferenceId() {
        Node idNode = doc.selectSingleNode(StructurePlanXMLConstants.STRUCTURE_PLAN_ROOT_ELEMENT + "/" + StructurePlanXMLConstants.STRUCTURE_PLAN_ID_ELEMENT);
        if (idNode == null) {
            String path = file.getAbsolutePath();
            return String.valueOf(new XXHasher().hash32String(path));
        }
        LineElement element = new LineElement(getFile(), (Element) idNode);
        element.checkNotEmpty();
        return element.getTextValue();
    }

    public String getDescription() {
        Node descNode = doc.selectSingleNode(StructurePlanXMLConstants.STRUCTURE_PLAN_ROOT_ELEMENT + "/" + StructurePlanXMLConstants.STRUCTURE_PLAN_DESCRIPTION_ELEMENT);
        if (descNode == null) {
            return "None";
        }
        LineElement element = new LineElement(getFile(), (Element) descNode);
        element.checkNotEmpty();
        return element.getTextValue();
    }

    public String getName() {
        Node nameNode = doc.selectSingleNode(StructurePlanXMLConstants.STRUCTURE_PLAN_ROOT_ELEMENT + "/" + StructurePlanXMLConstants.STRUCTURE_PLAN_NAME_ELEMENT);
        if (nameNode == null) {
            return FilenameUtils.getBaseName(file.getName());
        }
        LineElement element = new LineElement(getFile(), (Element) nameNode);
        element.checkNotEmpty();
        return element.getTextValue();
    }

    public double getPrice() {
        Node priceNode = doc.selectSingleNode(StructurePlanXMLConstants.STRUCTURE_PLAN_ROOT_ELEMENT + "/" + StructurePlanXMLConstants.STRUCTURE_PLAN_PRICE_ELEMENT);
        if (priceNode == null) {
            return 0.0;
        }
        LineElement priceElement = new LineElement(getFile(), (Element) priceNode);
        return priceElement.getDoubleValue();
    }

    public PlacementElement getPlacementElement() {
        checkNotNull(StructurePlanXMLConstants.STRUCTURE_PLAN_PLACEMENT);
        Node n = le.selectSingleNode(StructurePlanXMLConstants.STRUCTURE_PLAN_PLACEMENT);
        return new PlacementElement(getFile(), (Element) n);
    }

    public static StructurePlanDocument read(File xmlFile) throws DocumentException {
        ModelReader reader = ModelReader.getNonValidatingInstance(new BuildContextImpl());
        Document d = reader.read(xmlFile);
        return new StructurePlanDocument(xmlFile, d);
    }

    public String getCategory() {
        Node categoryNode = doc.selectSingleNode(StructurePlanXMLConstants.STRUCTURE_PLAN_ROOT_ELEMENT + "/" + StructurePlanXMLConstants.STRUCTURE_PLAN_CATEGORY_ELEMENT);
        if (categoryNode == null) {
            return null;
        }
        LineElement element = new LineElement(getFile(), (Element) categoryNode);
        element.checkNotEmpty();
        return element.getTextValue();
    }

}
