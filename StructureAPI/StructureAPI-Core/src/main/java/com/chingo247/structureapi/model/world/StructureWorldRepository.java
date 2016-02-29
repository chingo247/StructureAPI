/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.model.world;

import com.chingo247.settlercraft.core.model.world.WorldNode;
import com.chingo247.settlercraft.core.model.world.WorldRepository;
import java.util.UUID;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 *
 * @author Chingo
 */
public class StructureWorldRepository  {

    private WorldRepository worldRepository;
    
    public StructureWorldRepository(GraphDatabaseService graph) {
        this.worldRepository = new WorldRepository(graph);
    }

    public StructureWorldNode findByUUID(UUID worldUUID) {
        WorldNode world = worldRepository.findByUUID(worldUUID); //To change body of generated methods, choose Tools | Templates.
        if(world != null) {
            return new StructureWorldNode(world.getNode());
        }
        return null;
    }

    public StructureWorldNode addOrGet(String worldName, UUID worldUUID) {
        WorldNode world = worldRepository.addOrGet(worldName, worldUUID); //To change body of generated methods, choose Tools | Templates.
        return new StructureWorldNode(world.getNode());
    }
    
    
    
}
