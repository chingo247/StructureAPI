/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.model.world;

import com.chingo247.structureapi.model.structure.StructureNode;
import com.chingo247.settlercraft.core.model.world.WorldNode;
import com.chingo247.structureapi.model.RelTypes;
import com.chingo247.structureapi.model.zone.ConstructionZoneNode;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 * @author Chingo
 */
public class StructureWorldNode extends WorldNode  {

    public StructureWorldNode(Node worldNode) {
        super(worldNode);
    }

    public void addStructure(StructureNode structure) {
        structure.getNode().createRelationshipTo(getNode(), RelTypes.WITHIN);
    }
    
    
    public boolean deleteStructure(long id) {
        Node rawNode = getNode();
        for (Relationship rel : rawNode.getRelationships(RelTypes.WITHIN)) {
            StructureNode otherNode = new StructureNode(rel.getOtherNode(getNode()));
            if (otherNode.getNode().hasLabel(StructureNode.label()) && otherNode.getId() == id) {
                rel.delete();
                return true;
            }
        }
        return false;
    }
    
    
    public void addZone(ConstructionZoneNode zone) {
        zone.getNode().createRelationshipTo(getNode(), RelTypes.WITHIN);
    }
    

    public boolean deleteZone(long id) {
        Node rawNode = getNode();
        for (Relationship rel : rawNode.getRelationships(RelTypes.WITHIN)) {
            ConstructionZoneNode otherNode = new ConstructionZoneNode(rel.getOtherNode(getNode()));
            if (otherNode.getNode().hasLabel(ConstructionZoneNode.label()) && otherNode.getId() == id) {
                rel.delete();
                return true;
            }
        }
        return false;
    }

}
