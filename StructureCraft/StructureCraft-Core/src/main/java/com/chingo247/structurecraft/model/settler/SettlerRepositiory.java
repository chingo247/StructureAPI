/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.model.settler;

import com.chingo247.settlercraft.core.model.settler.BaseSettlerNode;
import com.chingo247.settlercraft.core.model.settler.BaseSettlerRepository;
import com.chingo247.settlercraft.core.model.settler.IBaseSettler;
import java.util.UUID;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 * Defines methods to find nodes that represent a StructureOwner. All methods in this class require an active Transaction.
 * @author Chingo
 */
public class SettlerRepositiory extends BaseSettlerRepository implements ISettlerRepository {


    public SettlerRepositiory(GraphDatabaseService graph) {
        super(graph);
    }

    @Override
    public SettlerNode findByUUID(UUID uuid) {
        BaseSettlerNode settler = super.findByUUID(uuid);
        if(settler != null) {
            return new SettlerNode(settler.getNode());
        }
        return null;
    }

    @Override
    public SettlerNode findById(Long id) {
        BaseSettlerNode settler = super.findById(id);
        if(settler != null) {
            return new SettlerNode(settler.getNode());
        }
        return null;
    }
    
    
   
    
}
