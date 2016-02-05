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
package com.chingo247.structureapi.model;

import com.chingo247.settlercraft.core.Direction;
import com.chingo247.structureapi.model.structure.StructureNode;
import com.chingo247.structureapi.util.WorldUtil;
import com.sk89q.worldedit.Vector;
import org.neo4j.graphdb.Node;

/**
 *
 * @author Chingo
 */
public abstract class StructureObject {
    
    public static final String RELATIVE_X_PROPERTY = "relativeX";
    public static final String RELATIVE_Y_PROPERTY = "relativeY";
    public static final String RELATIVE_Z_PROPERTY = "relativeZ";
    
    protected final Node underlyingNode;
    
    
    // Second level cache
    private Vector structurePosition;
    private Direction direction;
    private Vector relativePosition;
    private Vector position;

    public StructureObject(Node underlyingNode) {
        this.underlyingNode = underlyingNode;
    }

    public Node getNode() {
        return underlyingNode;
    }
    
    public abstract StructureNode getStructure();
    
    private Vector getStructurePosition() {
        if(structurePosition == null) {
            StructureNode structure = getStructure();
            structurePosition = structure.getOrigin();
        }
        return structurePosition;
    }
    
    private Direction getStructureDirection() {
        if(direction == null) {
            StructureNode structure = getStructure();
            direction = structure.getDirection();
        }
        return direction;
    }
    
    private Vector translateRelativeLocation(Vector offset) {
        Vector p = WorldUtil.translateLocation(getStructurePosition(), getStructureDirection(), offset.getX(), offset.getY(), offset.getZ());
        return new Vector(p.getBlockX(), p.getBlockY(), p.getBlockZ());
    }
    
    public int getRelativeX() {
        return (int) underlyingNode.getProperty(RELATIVE_X_PROPERTY);
    }
    
    public int getRelativeY() {
        return (int) underlyingNode.getProperty(RELATIVE_Y_PROPERTY);
    }
    
    public int getRelativeZ() {
        return (int) underlyingNode.getProperty(RELATIVE_Z_PROPERTY);
    }
    
    public Vector getRelativePosition() {
        if(relativePosition == null) {
            relativePosition = new Vector(getRelativeX(), getRelativeY(), getRelativeZ()); 
        }
        return relativePosition;
    }

    public double getX() {
        return getPosition().getX();
    }

    public double getY() {
        return getPosition().getY();
    }

    public double getZ() {
        return getPosition().getZ();
    }

    public int getBlockX() {
        return getPosition().getBlockX();
    }

    public int getBlockY() {
        return getPosition().getBlockY();
    }

    public int getBlockZ() {
        return getPosition().getBlockZ();
    }

    public Vector getPosition() {
        if(position == null) {
            position = translateRelativeLocation(getRelativePosition());
        }
        return position;
    }
    
    
    
    
    
}
