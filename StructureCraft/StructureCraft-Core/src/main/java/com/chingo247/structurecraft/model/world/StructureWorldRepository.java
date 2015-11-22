/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.model.world;

import com.chingo247.settlercraft.core.model.world.IWorldRepository;
import com.chingo247.settlercraft.core.model.world.WorldNode;
import com.chingo247.settlercraft.core.model.world.WorldRepository;
import com.chingo247.xplatform.core.IWorld;
import java.util.UUID;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 *
 * @author Chingo
 */
public class StructureWorldRepository  implements IStructureWorldRepository {

    private IWorldRepository<WorldNode> worldRepository;
    
    public StructureWorldRepository(GraphDatabaseService graph) {
        this.worldRepository = new WorldRepository(graph);
    }

    @Override
    public StructureWorldNode findByUUID(UUID worldUUID) {
        WorldNode world = worldRepository.findByUUID(worldUUID); //To change body of generated methods, choose Tools | Templates.
        if(world != null) {
            return new StructureWorldNode(world.getNode());
        }
        return null;
    }

    @Override
    public StructureWorldNode addOrGet(String worldName, UUID worldUUID) {
        WorldNode world = worldRepository.addOrGet(worldName, worldUUID); //To change body of generated methods, choose Tools | Templates.
        return new StructureWorldNode(world.getNode());
    }
    
    
    
}
