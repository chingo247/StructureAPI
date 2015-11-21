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
package com.chingo247.structurecraft.model.owner;

import com.chingo247.structurecraft.model.settler.SettlerNode;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 *  Classes that implement this interface discribe the relation between one structure and an owner
 * @author Chingo
 */
public interface IOwnership {
    
    /**
     * Gets the owner
     * @return The owner
     */
    public SettlerNode getOwner();
    
    /**
     * Gets the ownership type
     * @return The ownershiptype
     */
    public OwnerType getOwnerType();
    
    /**
     * Gets the relation
     * @return The relation
     */
    public Relationship getRelation();
    
    /**
     * Gets the other node
     * @return The other node
     */
    public Node getOtherNode();
    
    /**
     * Sets the owner type
     * @param type The type to set
     */
    public void setOwnerType(OwnerType type);
    
}
