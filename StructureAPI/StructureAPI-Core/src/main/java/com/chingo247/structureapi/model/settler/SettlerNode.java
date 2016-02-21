/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.model.settler;

import com.chingo247.structureapi.model.structure.StructureNode;
import com.chingo247.settlercraft.core.model.settler.BaseSettlerNode;
import com.chingo247.structureapi.model.Relations;
import com.chingo247.structureapi.model.owner.OwnerType;
import com.chingo247.structureapi.model.owner.StructureOwnership;
import com.chingo247.structureapi.model.structure.ConstructionStatus;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Result;

/**
 * Defines a StructureOwner, all operations in this class require an active Neo4j transaction.
 * @author Chingo
 */
public class SettlerNode extends BaseSettlerNode  {

    public SettlerNode(Node node) {
        super(node);
    }
    
    public List<StructureOwnership> getStructures() {
        return getStructures(-1, -1);
    }

    public List<StructureOwnership> getStructures(int skip, int limit) {
        List<StructureOwnership> ownerships = Lists.newArrayList();
        Map<String, Object> params = Maps.newHashMap();
        params.put("ownerId", getUniqueId().toString());
        if (skip > 0) {
            params.put("skip", skip);
        }

//        if(type != null) {
//            params.put("type", type.getTypeId());
//        }
//        
        if (limit > 0) {
            params.put("limit", limit);
        }

        String query;
        
//        if(type != null) {
//                query = " MATCH (settler:" + BaseSettlerNode.LABEL + " {" + BaseSettlerNode.UUID_PROPERTY + ": {ownerId} })"
//                + " WITH settler"
//                + " MATCH (settler)<-[:" + Relations.RELATION_OWNED_BY + "{Type:{type}}]-(s: " + StructureNode.LABEL + ")"
//                + " WHERE NOT s." + StructureNode.CONSTRUCTION_STATUS_PROPERTY + " = " + ConstructionStatus.REMOVED.getStatusId()
//                + " RETURN s"
//                + " ORDER BY s." + StructureNode.CREATED_AT_PROPERTY + " DESC ";
//        } else {
                query = " MATCH (settler:" + BaseSettlerNode.LABEL + " {" + BaseSettlerNode.UUID_PROPERTY + ": {ownerId} })"
                + " WITH settler"
                + " MATCH (settler)<-[r:" + Relations.RELATION_OWNED_BY + "]-(s: " + StructureNode.LABEL + ")"
                + " WHERE NOT s." + StructureNode.CONSTRUCTION_STATUS_PROPERTY + " = " + ConstructionStatus.REMOVED.getStatusId()
                + " RETURN r as result"
                + " ORDER BY s." + StructureNode.CREATED_AT_PROPERTY + " DESC ";
//        }

        if (skip > 0) {
            query += " SKIP {skip}";
        }

        if (limit > 0) {
            query += " LIMIT {limit}";
        }

        Result result = underlyingNode.getGraphDatabase().execute(query, params);
        while (result.hasNext()) {
            Map<String,Object> map = result.next();
            Relationship ownershipRel = (Relationship) map.get("result");
            ownerships.add(new StructureOwnership(this, ownershipRel));
        }
        return ownerships;
    }
    
    public int getStructureCount() {
        return underlyingNode.getDegree(DynamicRelationshipType.withName(Relations.RELATION_OWNED_BY), Direction.INCOMING);
    }

    

}
