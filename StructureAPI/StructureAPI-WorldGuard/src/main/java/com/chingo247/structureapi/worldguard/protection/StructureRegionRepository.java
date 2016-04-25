/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.worldguard.protection;

import com.chingo247.settlercraft.core.model.world.WorldNode;
import com.chingo247.structureapi.model.RelTypes;
import com.chingo247.structureapi.model.structure.ConstructionStatus;
import com.chingo247.structureapi.model.structure.Structure;
import com.chingo247.structureapi.model.structure.StructureNode;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;

/**
 *
 * @author Chingo
 */
public class StructureRegionRepository {
    
    public static final long ONE_SECOND = 1000;
    public static final long ONE_MINUTE = 60 * ONE_SECOND;
    
    
    private GraphDatabaseService graph;

    public StructureRegionRepository(GraphDatabaseService graph) {
        this.graph = graph;
    }
    
//    public long countActive() {
//        String query = "MATCH (n:WORLDGUARD_REGION) WHERE NOT exists(n.expired) OR n.expired = false RETURN COUNT(n) as count";
//        
//        Result r = graph.execute(query);
//        long count = 0;
//        
//        while(r.hasNext()) {
//            Map<String, Object> map = r.next();
//            count = (long) map.get("count");
//        }
//        return count;
//    }
//    
//    public long countExpired() {
//        String query = "MATCH (n:WORLDGUARD_REGION) WHERE exists(n.expired) AND n.expired = true RETURN COUNT(n) as count";
//        
//        Result r = graph.execute(query);
//        long count = 0;
//        
//        while(r.hasNext()) {
//            Map<String, Object> map = r.next();
//            count = (long) map.get("count");
//        }
//        return count;
//    }
//    
//    public long countRegionsWithoutStructure() {
//        String query = "MATCH(:" + StructureNode.LABEL + ")-[r:" + RelTypes.PROTECTED_BY.name() + "]-(wg:WORLDGUARD_REGION) "
//                + "WHERE r IS NULL "
//                + "RETURN COUNT(wg) as count";
//        
//        Result r = graph.execute(query);
//        long count = 0;
//        
//        while(r.hasNext()) {
//            Map<String, Object> map = r.next();
//            count = (long) map.get("count");
//        }
//        return count;
//    }
//    
//    
//    
//    public long countStructuresWithoutRegion() {
//        String query = "MATCH(structure:" + StructureNode.LABEL + ")-[r:" + RelTypes.PROTECTED_BY.name() + "]-(:WORLDGUARD_REGION) "
//                + "WHERE r IS NULL AND NOT structure."+ StructureNode.CONSTRUCTION_STATUS_PROPERTY + "=" + ConstructionStatus.REMOVED.getStatusId() + " "
//                + "RETURN COUNT(wg) as count";
//        
//        Result r = graph.execute(query);
//        long count = 0;
//        
//        while(r.hasNext()) {
//            Map<String, Object> map = r.next();
//            count = (long) map.get("count");
//        }
//        return count;
//    }
    
    public WorldGuardRegionNode findRegionById(String regionId) {
        WorldGuardRegionNode region = null;
        Map<String, Object> params = Maps.newHashMap();
        params.put("regionId", regionId);
        String query
                = " MATCH (wg:" + WorldGuardRegionNode.LABEL + " { " + WorldGuardRegionNode.REGION_PROPERTY + ": {regionId}})" 
                + " RETURN wg as region";

        Result result = graph.execute(query, params);

        if (result.hasNext()) {
            Node n = (Node) result.next().get("region");
            region = new WorldGuardRegionNode(n);
        }
        return region;
    }
    
    public List<Node> findRegionsWithoutStructure(long skip, long limit) {
        Map<String,Object> params = new HashMap<>();
        if(skip > 0) {
            params.put("skip", skip);
        }
        if(limit > 0) {
            params.put("limit", limit);
        }
        
        String query = "MATCH(:" + StructureNode.LABEL + ")-[r:" + RelTypes.PROTECTED_BY.name() + "]-(wg:WORLDGUARD_REGION) "
                + "WHERE r IS NULL "
                + "RETURN wg as regions";
        if(skip > 0) {
            query += " SKIP {skip}";
        }
        
        if(limit > 0) {
            query += " LIMIT {limit}";
        }
        
        Result r = graph.execute(query, params);
        List<Node> found = new ArrayList<>();
        while(r.hasNext()) {
            Map<String, Object> map = r.next();
            Node n = (Node)map.get("regions"); 
            found.add(n);
        }
        return found;
    }
    
    public List<Node> findUnprotectedStructures(long limit) {
        Map<String,Object> params = new HashMap<>();
       
        if(limit > 0) {
            params.put("limit", limit);
        }
        String query = "MATCH(structure:" + StructureNode.LABEL + ")-[r:" + RelTypes.PROTECTED_BY.name() + "]-(region:WORLDGUARD_REGION) "
                + "WHERE r IS NULL OR region." + WorldGuardRegionNode.EXPIRED_PROPERTY + "= true "
                + "AND NOT structure."+ StructureNode.CONSTRUCTION_STATUS_PROPERTY + "=" + ConstructionStatus.REMOVED.getStatusId() + " "
                + "RETURN structure as structures";
      
        
        if(limit > 0) {
            query += " LIMIT {limit}";
        }
        
        Result r = graph.execute(query, params);
        List<Node> found = new ArrayList<>();
        while(r.hasNext()) {
            Map<String, Object> map = r.next();
            Node n = (Node)map.get("structures"); 
            found.add(n);
        }
        return found;
    }
    
    
    
    public List<Node> findExpired(long skip, long limit) {
        Map<String,Object> params = new HashMap<>();
        if(skip > 0) {
            params.put("skip", skip);
        }
        if(limit > 0) {
            params.put("limit", limit);
        }
        
        String query = "MATCH (n:WORLDGUARD_REGION) WHERE exists(n.expired) AND n.expired = true RETURN n as regions";
        if(skip > 0) {
            query += " SKIP {skip}";
        }
        
        if(limit > 0) {
            query += " LIMIT {limit}";
        }
        
        Result r = graph.execute(query, params);
        List<Node> found = new ArrayList<>();
        while(r.hasNext()) {
            Map<String, Object> map = r.next();
            Node n = (Node)map.get("regions"); 
            found.add(n);
        }
        return found;
    }
    
    
    public List<Node> findProtectedStructures(long skip, long limit) {
        Map<String,Object> params = new HashMap<>();
        if(skip > 0) {
            params.put("skip", skip);
        }
        if(limit > 0) {
            params.put("limit", limit);
        }
        
        String query = "MATCH (structure:"+StructureNode.LABEL+")-[r:"+RelTypes.PROTECTED_BY+"]->(n:WORLDGUARD_REGION) WHERE r IS NOT NULL RETURN structure as structures";
        if(skip > 0) {
            query += " SKIP {skip}";
        }
        
        if(limit > 0) {
            query += " LIMIT {limit}";
        }
        
        Result r = graph.execute(query, params);
        List<Node> found = new ArrayList<>();
        while(r.hasNext()) {
            Map<String, Object> map = r.next();
            Node n = (Node)map.get("structures"); 
            found.add(n);
        }
        return found;
    }
    
    public long countToBeExpired(UUID world, long expirationTime) {
        Map<String,Object> params = new HashMap<>();
        
        params.put("world", world.toString());
        
        params.put("date", System.currentTimeMillis());
        params.put("expirationTime", expirationTime);
        String query = "MATCH (s:STRUCTURE)-[:"+RelTypes.WITHIN+"]->(w:"+WorldNode.LABEL+" {"+WorldNode.UUID_PROPERTY+": {world}}) "
                + " WITH (s) "
                + " MATCH (w)(wg:WORLDGUARD_REGION) WHERE EXISTS(wg.createdAt) AND ({date} - wg.createdAt) > {expirationTime} RETURN wg as regions";
        
        Result r = graph.execute(query, params);
        long count = 0;
        while(r.hasNext()) {
            Map<String, Object> map = r.next();
            count = (long) map.get("count");
        }
        return count;
    }
    
    public List<Node> findToBeExpired(UUID world, long expirationTime, long limit) {
        Map<String,Object> params = new HashMap<>();
        
        if(limit > 0) {
            params.put("limit", limit);
        }
        params.put("world", world.toString());
        
        params.put("date", System.currentTimeMillis());
        params.put("expirationTime", expirationTime);
        String query = "MATCH (w:"+WorldNode.LABEL+" { "+WorldNode.UUID_PROPERTY+": {world}}) "
                + " WITH (w) "
                + " MATCH (w)<-[:"+RelTypes.WITHIN+"]-(s:STRUCTURE) "
                + " WITH (s) "
                + " MATCH (s)-[:"+RelTypes.PROTECTED_BY+"]->(wg:WORLDGUARD_REGION) WHERE EXISTS(wg.createdAt) AND ABS({date} - wg.createdAt) > {expirationTime} AND wg.expired = false RETURN s as structures";
        
        if(limit > 0) {
            query += " LIMIT {limit}";
        }
        
        Result r = graph.execute(query, params);
        
        List<Node> found = new ArrayList<>();
        while(r.hasNext()) {
            Map<String, Object> map = r.next();
            Node n = (Node)map.get("structures"); 
            found.add(n);
        }
        return found;
        
    }
    
    public StructureNode findById(Long id) {
        StructureNode structure = null;
        Map<String, Object> params = Maps.newHashMap();
        params.put("structureId", id);

        String query
                = " MATCH (s:" + StructureNode.LABEL + " { " + StructureNode.ID_PROPERTY + ": {structureId} })-[r:"+RelTypes.PROTECTED_BY+"]->(:WORLDGUARD_REGION) "
                + " WHERE NOT r IS NULL"
                + " RETURN s as structure";

        Result result = graph.execute(query, params);

        if (result.hasNext()) {
            Node n = (Node) result.next().get("structure");
            structure = new StructureNode(n);
        }

        return structure;
    }
    
    
}
