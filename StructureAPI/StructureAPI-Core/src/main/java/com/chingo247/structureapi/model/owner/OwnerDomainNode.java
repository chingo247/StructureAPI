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

import com.chingo247.settlercraft.core.model.settler.Settler;
import com.chingo247.settlercraft.core.model.settler.SettlerNode;
import com.chingo247.structureapi.model.RelTypes;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.UUID;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.parboiled.common.Preconditions;

/**
 *
 * @author Chingo
 */
public class OwnerDomainNode {
    
    private final Node underlyingNode;

    public OwnerDomainNode(Node underlyingNode) {
        this.underlyingNode = underlyingNode;
    }

    public Node getUnderlyingNode() {
        return underlyingNode;
    }
    
    public boolean isOwner(UUID settler) {
        return getOwnership(settler) != null;
    }
    
    public boolean isOwnerOfType(UUID settler, OwnerType ownerType) {
        Ownership o = getOwnership(settler);
        return o != null && o.getOwnerType() == ownerType;
    }
    
    public Ownership getOwnership(UUID settler) {
        Ownership ownership = null;
        for (Relationship rel : underlyingNode.getRelationships(RelTypes.OWNED_BY, org.neo4j.graphdb.Direction.OUTGOING)) {
            SettlerNode ownerNode = new SettlerNode(rel.getOtherNode(underlyingNode));
            if (ownerNode.getUniqueId().equals(settler)) {
                ownership = new Ownership(ownerNode, rel);
                break;
            }
        }
        return ownership;
    }
    
    /**
     * Sets the ownership, if none exists a new one is created, if it does exist the current one is updated
     * @param settler The Settler of the ownership
     * @param ownerType The owner type to set, may not be null
     * @return True if there was an update
     */
    public boolean setOwnership(SettlerNode settler, OwnerType ownerType) {
        Preconditions.checkNotNull(ownerType, "Ownertype may not be null...");
        // if exists... update it
        for(Ownership o : getOwnerships()) {
            if(o.getOwner().getUniqueId().equals(settler.getUniqueId())) {
                OwnerType type = o.getOwnerType();
                if(type != ownerType) {
                    o.getRelation().setProperty("Type", ownerType.getTypeId()); 
                    return true;
                } else {
                    return false;
                }
            }
        }
        // otherwise create a new one
        Relationship r = underlyingNode.createRelationshipTo(settler.getNode(), RelTypes.OWNED_BY);
        r.setProperty("Type", ownerType.getTypeId());
        return true;
    }
    
    
    
    public boolean removeOwnership(Settler settler) {
        return removeOwnership(settler.getUniqueId());
    }
    
    public boolean removeOwnership(UUID settler) {
        for(Ownership o : getOwnerships()) {
            if(o.getOwner().getUniqueId().equals(settler)) {
                o.getRelation().delete();
                return true;
            }
        }
        return false;
    }
    
    public List<Ownership> getOwnerships() {
        List<Ownership> owners = Lists.newArrayList();
        for (Relationship rel : underlyingNode.getRelationships(RelTypes.OWNED_BY, org.neo4j.graphdb.Direction.OUTGOING)) {
            if (rel.getOtherNode(underlyingNode).hasLabel(SettlerNode.label())) {
                SettlerNode ownerNode = new SettlerNode(rel.getOtherNode(underlyingNode));
                owners.add(new Ownership(ownerNode, rel));
            }
        }
        return owners;
    }
    
    public List<SettlerNode> getOwners(OwnerType ownerType) {
        Preconditions.checkNotNull(ownerType, "OwnerType may not be null");

        List<SettlerNode> owners = Lists.newArrayList();
        for (Relationship rel : underlyingNode.getRelationships(RelTypes.OWNED_BY, org.neo4j.graphdb.Direction.OUTGOING)) {
            if (rel.hasProperty("Type")) {
                Integer typeId = (Integer) rel.getProperty("Type");
                OwnerType type = OwnerType.match(typeId);
                if (type == ownerType) {
                    SettlerNode ownerNode = new SettlerNode(rel.getOtherNode(underlyingNode));
                    owners.add(ownerNode);
                }
            }
        }
        return owners;
    }
    
}
