/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.worldguard.protection;

import com.chingo247.structureapi.model.RelTypes;
import com.chingo247.structureapi.model.structure.ConstructionStatus;
import com.chingo247.structureapi.model.structure.StructureNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    
    public long countActive() {
        String query = "MATCH (n:WORLDGUARD_REGION) WHERE NOT exists(n.expired) OR n.expired = false RETURN COUNT(n) as count";
        
        Result r = graph.execute(query);
        long count = 0;
        
        while(r.hasNext()) {
            Map<String, Object> map = r.next();
            count = (long) map.get("count");
        }
        return count;
    }
    
    public long countExpired() {
        String query = "MATCH (n:WORLDGUARD_REGION) WHERE exists(n.expired) AND n.expired = true RETURN COUNT(n) as count";
        
        Result r = graph.execute(query);
        long count = 0;
        
        while(r.hasNext()) {
            Map<String, Object> map = r.next();
            count = (long) map.get("count");
        }
        return count;
    }
    
    public long countRegionsWithoutStructure() {
        String query = "MATCH(:" + StructureNode.LABEL + ")-[r:" + RelTypes.PROTECTED_BY.name() + "]-(wg:WORLDGUARD_REGION) "
                + "WHERE r IS NULL "
                + "RETURN COUNT(wg) as count";
        
        Result r = graph.execute(query);
        long count = 0;
        
        while(r.hasNext()) {
            Map<String, Object> map = r.next();
            count = (long) map.get("count");
        }
        return count;
    }
    
    public long countStructuresWithoutRegion() {
        String query = "MATCH(structure:" + StructureNode.LABEL + ")-[r:" + RelTypes.PROTECTED_BY.name() + "]-(:WORLDGUARD_REGION) "
                + "WHERE r IS NULL AND NOT structure."+ StructureNode.CONSTRUCTION_STATUS_PROPERTY + "=" + ConstructionStatus.REMOVED.getStatusId() + " "
                + "RETURN COUNT(wg) as count";
        
        Result r = graph.execute(query);
        long count = 0;
        
        while(r.hasNext()) {
            Map<String, Object> map = r.next();
            count = (long) map.get("count");
        }
        return count;
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
    
    public List<Node> findStructuresWithoutRegion(long skip, long limit) {
        Map<String,Object> params = new HashMap<>();
        if(skip > 0) {
            params.put("skip", skip);
        }
        if(limit > 0) {
            params.put("limit", limit);
        }
        String query = "MATCH(structure:" + StructureNode.LABEL + ")-[r:" + RelTypes.PROTECTED_BY.name() + "]-(:WORLDGUARD_REGION) "
                + "WHERE r IS NULL "
                + "AND NOT structure."+ StructureNode.CONSTRUCTION_STATUS_PROPERTY + "=" + ConstructionStatus.REMOVED.getStatusId() + " "
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
    
    public List<Node> findActive(long skip, long limit) {
        Map<String,Object> params = new HashMap<>();
        if(skip > 0) {
            params.put("skip", skip);
        }
        if(limit > 0) {
            params.put("limit", limit);
        }
        
        String query = "MATCH (n:WORLDGUARD_REGION) WHERE NOT exists(n.expired) OR n.expired = false RETURN n as regions";
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
    
    public List<Node> findToBeExpired(long expirationTime, int limit, int skip) {
        Map<String,Object> params = new HashMap<>();
        if(skip > 0) {
            params.put("skip", skip);
        }
        if(limit > 0) {
            params.put("limit", limit);
        }
        
        params.put("date", System.currentTimeMillis());
        params.put("expirationTime", expirationTime);
        String query = "MATCH (wg:WORLDGUARD_REGION) WHERE EXISTS(wg.createdAt) AND ({date} - wg.createdAt) > {expirationTime} RETURN wg as regions";
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
    
    
}
