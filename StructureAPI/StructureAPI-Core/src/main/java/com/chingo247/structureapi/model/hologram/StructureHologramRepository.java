/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.model.hologram;

import com.chingo247.structureapi.model.structure.ConstructionStatus;
import com.chingo247.structureapi.model.structure.StructureNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sk89q.worldedit.Vector;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;

/**
 *
 * @author Chingo
 */
public class StructureHologramRepository implements IStructureHologramRepository {
    
    private GraphDatabaseService graph;

    public StructureHologramRepository(GraphDatabaseService graph) {
        this.graph = graph;
    }
    
    @Override
    public StructureHologramNode addHologram(StructureNode structure, Vector relativePosition) {
        Node n = graph.createNode(StructureHologramNode.LABEL);
        n.setProperty(StructureHologramNode.RELATIVE_X_PROPERTY, relativePosition.getBlockX());
        n.setProperty(StructureHologramNode.RELATIVE_Y_PROPERTY, relativePosition.getBlockY());
        n.setProperty(StructureHologramNode.RELATIVE_Z_PROPERTY, relativePosition.getBlockZ());
        structure.getNode().setProperty(StructureNode.CHECKED_HOLOGRAM_PROPERTY, true);
        structure.getNode().createRelationshipTo(n, StructureHologramNode.RELATION_HAS_HOLOGRAM);
        return new StructureHologramNode(n);
    }
    
    @Override
    public Collection<StructureHologramNode> findAll() {
        Map<String,Object> params = Maps.newHashMap();
        params.put("removed", (Integer) ConstructionStatus.REMOVED.getStatusId());
        
        String query = "MATCH (h:"+StructureHologramNode.LABEL.name()+")"
                + "<-[r:"+StructureHologramNode.RELATION_HAS_HOLOGRAM.name()+"]-"
                + "(s:"+StructureNode.LABEL+") "
                + "WHERE NOT s." + StructureNode.CONSTRUCTION_STATUS_PROPERTY + " = {removed}"
                + "RETURN h";
        Result r = graph.execute(query, params);
        List<StructureHologramNode> holograms = Lists.newArrayList();
        
        while(r.hasNext()) {
            Map<String,Object> map = r.next();
            for(Object o : map.values()) {
                Node n = (Node) o;
                StructureHologramNode shn = new StructureHologramNode(n);
                holograms.add(shn);
            }
        }
        return holograms;
    }
    
}
