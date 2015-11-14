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
package com.chingo247.structurecraft.model.plot;

import com.chingo247.settlercraft.core.model.WorldNode;
import com.chingo247.structurecraft.model.RelTypes;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import java.util.UUID;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 *
 * @author Chingo
 */
public class Plot implements IPlot {
    
    private Node underlyingNode;
    private Vector min, max;
    private UUID worldUUID;
    private String worldName;

    public Plot(PlotNode plotNode) {
        this.underlyingNode = plotNode.getNode();
        this.min = plotNode.getMin();
        this.max = plotNode.getMax();
        this.worldUUID = plotNode.getWorldUUID();
        this.worldName = plotNode.getWorldName();
    }

    public Plot(Node node) {
        this(new PlotNode(node));
    }
    
    public WorldNode getWorldNode() {
        Iterable<Relationship> rels = underlyingNode.getRelationships(RelTypes.WITHIN, Direction.OUTGOING);
        for(Relationship rel : rels) {
            if(rel.getOtherNode(underlyingNode).hasLabel(WorldNode.label())) {
                WorldNode worldNode = new WorldNode(rel.getOtherNode(underlyingNode));
                return worldNode;
            }
        }
        return null;
    }
    
    @Override
    public Node getNode() {
        return underlyingNode;
    }

    @Override
    public Vector getMin() {
        return min;
    }

    @Override
    public Vector getMax() {
        return max;
    }

    @Override
    public CuboidRegion getCuboidRegion() {
        return new CuboidRegion(min, max);
    }

    @Override
    public UUID getWorldUUID() {
        return worldUUID;
    }

    @Override
    public String getWorldName() {
        return worldName;
    }
    
    
    
    
    
    
}
