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
package com.chingo247.structureapi.model.hologram;

import com.chingo247.structureapi.model.structure.Structure;
import com.sk89q.worldedit.Vector;
import org.neo4j.graphdb.Node;

/**
 *
 * @author Chingo
 */
public final class StructureHologram implements IStructureHologram {
    
    private Node underlyingNode;
    private String name;
    private Vector relativePosition;
    private Structure structure;
    private Vector position;

    public StructureHologram(Node underlyingNode) {
        this(new StructureHologramNode(underlyingNode));
    }
    
    public StructureHologram(StructureHologramNode structureHologramNode) {
        this.underlyingNode = structureHologramNode.getNode();
        this.name = structureHologramNode.getName();
        this.structure = new Structure(structureHologramNode.getStructure());
        this.relativePosition = structureHologramNode.getRelativePosition();
        this.position = getPosition();
    }

    @Override
    public Node getNode() {
        return underlyingNode;
    }

    @Override
    public StructureHologramNode getHologramNode() {
        return new StructureHologramNode(underlyingNode);
    }
    
    @Override
    public Structure getStructure() {
        return structure;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public double getX() {
        return position.getX();
    }

    @Override
    public double getY() {
        return position.getY();
    }

    @Override
    public double getZ() {
        return position.getZ();
    }

    @Override
    public int getBlockX() {
        return position.getBlockX();
    }

    @Override
    public int getBlockY() {
        return position.getBlockY();
    }

    @Override
    public int getBlockZ() {
        return position.getBlockZ();
    }

    @Override
    public int getRelativeX() {
        return relativePosition.getBlockX();
    }

    @Override
    public int getRelativeY() {
        return relativePosition.getBlockY();
    }

    @Override
    public int getRelativeZ() {
        return relativePosition.getBlockZ();
    }

    @Override
    public Vector getPosition() {
        if(position == null) {
            position = structure.translateRelativeLocation(relativePosition);
        }
        return position;
    }

    @Override
    public Vector getRelativePosition() {
        return relativePosition;
    }
    
}
