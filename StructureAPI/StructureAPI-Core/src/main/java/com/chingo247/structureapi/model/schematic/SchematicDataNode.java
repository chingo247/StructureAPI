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
package com.chingo247.structureapi.model.schematic;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 *
 * @author Chingo
 */
public class SchematicDataNode  {
    
    public static final String LABEL_NAME = "SCHEMATIC_DATA";
    public static final Label LABEL = DynamicLabel.label(LABEL_NAME);
    public static final String WIDTH_PROPERTY = "width";
    public static final String HEIGHT_PROPERTY = "height";
    public static final String LENGTH_PROPERTY = "length";
    public static final String XXHASH_PROPERTY = "xxhash";
    public static final String NAME_PROPERTY = "name";
    public static final String LAST_IMPORT = "lastImport";
    public static final String AXIS_OFFSET_PROPERTY = "axisOffset";
    
    private final Node underlyingNode;

    public SchematicDataNode(Node node) {
        this.underlyingNode = node;
    }
    
    public Node getUnderlyingNode() {
        return underlyingNode;
    }
    
    public boolean hasRotation() {
        return underlyingNode.hasProperty(AXIS_OFFSET_PROPERTY);
    }

    public int getAxisOffset() {
        Integer rotation = underlyingNode.hasProperty(AXIS_OFFSET_PROPERTY) ? (Integer)underlyingNode.getProperty(AXIS_OFFSET_PROPERTY) : Integer.MIN_VALUE;
        return rotation;
    }
    
    public void setRotation(int rotation) {
        underlyingNode.setProperty(AXIS_OFFSET_PROPERTY, rotation);
    }
    
    public int getWidth() {
        return (int) underlyingNode.getProperty(WIDTH_PROPERTY);
    }
    
    public int getHeight() {
        return (int) underlyingNode.getProperty(HEIGHT_PROPERTY);
    }
    
    public int getLength() {
        return (int) underlyingNode.getProperty(LENGTH_PROPERTY);
    }
    
    public long getXXHash64() {
        return (long) underlyingNode.getProperty(XXHASH_PROPERTY);
    }
    
    public String getName() {
        return (String) underlyingNode.getProperty(NAME_PROPERTY);
    }
    
    public long getLastImport() {
        return (long) underlyingNode.getProperty(LAST_IMPORT);
    }
    
    public void setLastImport(long newImportDate) {
        this.underlyingNode.setProperty(LAST_IMPORT, newImportDate);
    }
    
    public void delete() {
        for(Relationship rel : underlyingNode.getRelationships()) {
            rel.delete();
        }
        underlyingNode.delete();
    }
    
}
