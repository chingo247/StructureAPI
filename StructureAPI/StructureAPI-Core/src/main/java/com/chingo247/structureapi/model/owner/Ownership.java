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
package com.chingo247.structureapi.model.owner;

import com.chingo247.settlercraft.core.model.settler.SettlerNode;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 * Defines a relation between a Structure and an (Structure)Owner. All operations in this class require an active transaction.
 * @author Chingo
 */
public class Ownership {
    
    private final SettlerNode owner;
    private final Relationship rel;

    public Ownership(SettlerNode owner, Relationship relation) {
        this.owner = owner;
        this.rel = relation;
    }
    
    public Node getOtherNode() {
        return rel.getOtherNode(owner.getNode());
    }

    public void setOwnerType(OwnerType ownerType) {
        rel.setProperty("Type", ownerType.getTypeId()); 
    }

    public SettlerNode getOwner() {
        return owner;
    }

    public OwnerType getOwnerType() {
        if(rel.hasProperty("Type")) {
            Integer typeProp = (Integer) rel.getProperty("Type");
            OwnerType t = OwnerType.match(typeProp);
            return t;
        } else {
            return null;
        }
    }

    public Relationship getRelation() {
        return rel;
    }
    
}
