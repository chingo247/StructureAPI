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
package com.chingo247.structurecraft.model;

import com.chingo247.settlercraft.core.Direction;
import com.chingo247.structurecraft.model.structure.StructureNode;
import com.chingo247.structurecraft.util.WorldUtil;
import com.sk89q.worldedit.Vector;
import org.neo4j.graphdb.Node;

/**
 *
 * @author Chingo
 */
public abstract class StructureObject implements IStructureObject {
    
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

    @Override
    public Node getNode() {
        return underlyingNode;
    }
    
    private Vector getStructurePosition() {
        if(structurePosition == null) {
            StructureNode structure = new StructureNode(getStructure().getNode());
            structurePosition = structure.getOrigin();
        }
        return structurePosition;
    }
    
    private Direction getStructureDirection() {
        if(direction == null) {
            StructureNode structure = new StructureNode(getStructure().getNode());
            direction = structure.getDirection();
        }
        return direction;
    }
    
    private Vector translateRelativeLocation(Vector offset) {
        Vector p = WorldUtil.translateLocation(getStructurePosition(), getStructureDirection(), offset.getX(), offset.getY(), offset.getZ());
        return new Vector(p.getBlockX(), p.getBlockY(), p.getBlockZ());
    }
    
    @Override
    public int getRelativeX() {
        return (int) underlyingNode.getProperty(RELATIVE_X_PROPERTY);
    }
    
    @Override
    public int getRelativeY() {
        return (int) underlyingNode.getProperty(RELATIVE_Y_PROPERTY);
    }
    
    @Override
    public int getRelativeZ() {
        return (int) underlyingNode.getProperty(RELATIVE_Z_PROPERTY);
    }
    
    @Override
    public Vector getRelativePosition() {
        if(relativePosition == null) {
            relativePosition = new Vector(getRelativeX(), getRelativeY(), getRelativeZ()); 
        }
        return relativePosition;
    }

    @Override
    public double getX() {
        return getPosition().getX();
    }

    @Override
    public double getY() {
        return getPosition().getY();
    }

    @Override
    public double getZ() {
        return getPosition().getZ();
    }

    @Override
    public int getBlockX() {
        return getPosition().getBlockX();
    }

    @Override
    public int getBlockY() {
        return getPosition().getBlockY();
    }

    @Override
    public int getBlockZ() {
        return getPosition().getBlockZ();
    }

    @Override
    public Vector getPosition() {
        if(position == null) {
            position = translateRelativeLocation(getRelativePosition());
        }
        return position;
    }
    
    
    
    
    
}
