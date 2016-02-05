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
package com.chingo247.structureapi.updates;

import com.chingo247.settlercraft.core.persistence.neo4j.Neo4jHelper;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

/**
 *
 * @author Chingo
 */
public class StructureAPI_Update_2_2_0 implements IStructureAPIUpdate{
    
    private GraphDatabaseService graph;

    public StructureAPI_Update_2_2_0(GraphDatabaseService graph) {
        this.graph = graph;
    }
    
    @Override
    public void update() {
        final String UPDATE_VERSION = "2.2.0";
        final Label  UPDATE_LABEL = DynamicLabel.label("STRUCTURE_API_UPDATE");
        final String UPDATE_KEY = "version";
        
        try (Transaction tx = graph.beginTx()) {
            Neo4jHelper.createIndexIfNotExist(graph, UPDATE_LABEL, UPDATE_KEY);
            tx.success();
        } 
        
        System.out.println("[SettlerCraft]: updating model to " + UPDATE_VERSION);
        try(Transaction tx = graph.beginTx()) {
            Node n = graph.findNode(UPDATE_LABEL, UPDATE_KEY, UPDATE_VERSION);
            if(n == null) {
                // Update 'StructureHologram' to 'STRUCTURE_HOLOGRAM'
                graph.execute("MATCH (s:SchematicData) SET s:SCHEMATIC_DATA REMOVE s:SchematicData");
                // Update 'StructureHologram' to 'STRUCTURE_HOLOGRAM'
                graph.execute("MATCH (s:StructureHologram) SET s:STRUCTURE_HOLOGRAM REMOVE s:StructureHologram");
                // Update 'Structure' to 'Structure'
                // Update add label 'PLOT' to Structure
                graph.execute("MATCH (s:Structure) SET s:STRUCTURE:PLOT REMOVE s:Structure");
                // Add property 'plotType' to structure
                graph.execute("MATCH (s:STRUCTURE) SET s.plotType = 'Structure'");
                // Update 'World' to 'WORLD'
                graph.execute("MATCH (w:World) SET w:WORLD REMOVE w:World");
                // Update 'within' to 'WITHIN'
                graph.execute("MATCH (a:WORLD)<-[r:Within]-(b:STRUCTURE) CREATE (a)<-[:WITHIN]-(b) DELETE r");
                // Update 'within' to 'WITHIN'
                graph.execute("MATCH (a:STRUCTURE)<-[r:SubstructureOf]-(b:STRUCTURE) CREATE (a)<-[:SUBSTRUCTURE_OF]-(b) DELETE r");
                // Update 'hasHologram' to 'HAS_HOLOGRAM'
                graph.execute("MATCH (a:STRUCTURE_HOLOGRAM)<-[r:hasHologram]-(b:STRUCTURE) CREATE (a)<-[:HAS_HOLOGRAM]-(b) DELETE r");
                // Update 'ownedBy' to 'OWNED_BY'
                graph.execute("MATCH (a)<-[r:OwnedBy]-(b:STRUCTURE) CREATE (a)<-[:OWNED_BY]-(b) DELETE r");
                
                graph.execute("MATCH (a:STRUCTURE) "
                        +     "WHERE NOT a.WGRegion IS NULL AND NOT (a)-[:PROTECTED_BY]->(:WORLDGUARD_REGION)"
                        +     "CREATE (a)-[:PROTECTED_BY]->(w:WORLDGUARD_REGION {region: a.WGRegion}) "
                        +     "REMOVE a.WGRegion"
                );
                
                n = graph.createNode(UPDATE_LABEL);
                n.setProperty(UPDATE_KEY, UPDATE_VERSION);
            } 
            
            tx.success();
        }
        
    }
    
}
