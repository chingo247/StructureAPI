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
package com.chingo247.structureapi.model.plot;

import com.chingo247.settlercraft.core.model.world.WorldNode;
import com.chingo247.structureapi.model.RelTypes;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import java.util.UUID;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 * Defines a region that is 'ownable' 
 * @author Chingo
 */
public class PlotNode {

    public static final String MIN_X_PROPERTY = "minX", MIN_Y_PROPERTY = "minY", MIN_Z_PROPERTY = "minZ", MAX_X_PROPERTY = "maxX", MAX_Y_PROPERTY = "maxY", MAX_Z_PROPERTY = "maxZ";
    public static final String PLOT_TYPE_PROPERTY = "plotType";
    public static final String LABEL_PLOT = "PLOT";
    
    protected final Node underlyingNode;

    public static Label plotLabel() {
        return DynamicLabel.label(LABEL_PLOT);
    }
    
    public PlotNode(Node node) {
        this.underlyingNode = node;
    }
    
    public WorldNode getWorldNode() {
        if(!underlyingNode.hasRelationship(RelTypes.WITHIN, Direction.OUTGOING)) {
            return null;
        }
        Iterable<Relationship> rels = underlyingNode.getRelationships(RelTypes.WITHIN, Direction.OUTGOING);
        for(Relationship rel : rels) {
            if(rel.getOtherNode(underlyingNode).hasLabel(WorldNode.label())) {
                WorldNode worldNode = new WorldNode(rel.getOtherNode(underlyingNode));
                return worldNode;
            }
        }
        return null;
    }
    
    public Node getNode() {
        return underlyingNode;
    }

    public UUID getWorldUUID() {
        Iterable<Relationship> rels = underlyingNode.getRelationships(RelTypes.WITHIN, Direction.OUTGOING);
        for(Relationship rel : rels) {
            if(rel.getOtherNode(underlyingNode).hasLabel(WorldNode.label())) {
                WorldNode worldNode = new WorldNode(rel.getOtherNode(underlyingNode));
                return worldNode.getUUID();
            }
        }
        return null;
    }

    public void setMinX(int x) {
        underlyingNode.setProperty(MIN_X_PROPERTY, x);
    }

    public void setMinY(int y) {
        underlyingNode.setProperty(MIN_Y_PROPERTY, y);
    }

    public void setMinZ(int z) {
        underlyingNode.setProperty(MIN_Z_PROPERTY, z);
    }

    public void setMaxX(int x) {
        underlyingNode.setProperty(MAX_X_PROPERTY, x);
    }

    public void setMaxY(int y) {
        underlyingNode.setProperty(MAX_Y_PROPERTY, y);
    }

    public void setMaxZ(int z) {
        underlyingNode.setProperty(MAX_Z_PROPERTY, z);
    }
    
    public int getMinX() {
        return (int) underlyingNode.getProperty(MIN_X_PROPERTY);
    }

    public int getMinY() {
        return (int) underlyingNode.getProperty(MIN_Y_PROPERTY);
    }

    public int getMinZ() {
        return (int) underlyingNode.getProperty(MIN_Z_PROPERTY);
    }

    public int getMaxX() {
        return (int) underlyingNode.getProperty(MAX_X_PROPERTY);
    }

    public int getMaxY() {
        return (int) underlyingNode.getProperty(MAX_Y_PROPERTY);
    }

    public int getMaxZ() {
        return (int) underlyingNode.getProperty(MAX_Z_PROPERTY);
    }

    public Vector getMin() {
        return new BlockVector(getMinX(), getMinY(), getMinZ());
    }

    public Vector getMax() {
        return new BlockVector(getMaxX(), getMaxY(), getMaxZ());
    }
    
    public CuboidRegion getCuboidRegion() {
        int minX = (int) underlyingNode.getProperty(MIN_X_PROPERTY);
        int minY = (int) underlyingNode.getProperty(MIN_Y_PROPERTY);
        int minZ = (int) underlyingNode.getProperty(MIN_Z_PROPERTY);
        int maxX = (int) underlyingNode.getProperty(MAX_X_PROPERTY);
        int maxY = (int) underlyingNode.getProperty(MAX_Y_PROPERTY);
        int maxZ = (int) underlyingNode.getProperty(MAX_Z_PROPERTY);
        return new CuboidRegion(new Vector(minX, minY, minZ), new Vector(maxX, maxY, maxZ));
    }

    public String getWorldName() {
        Iterable<Relationship> rels = underlyingNode.getRelationships(RelTypes.WITHIN, Direction.OUTGOING);
        for(Relationship rel : rels) {
            if(rel.getOtherNode(underlyingNode).hasLabel(WorldNode.label())) {
                WorldNode worldNode = new WorldNode(rel.getOtherNode(underlyingNode));
                return worldNode.getName();
            }
        }
        return null;
    }

    

}
