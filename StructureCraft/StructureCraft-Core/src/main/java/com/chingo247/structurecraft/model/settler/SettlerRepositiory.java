/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.model.settler;

import com.chingo247.settlercraft.core.model.BaseSettlerRepository;
import com.chingo247.settlercraft.core.model.interfaces.IBaseSettler;
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
    public Settler findByUUID(UUID uuid) {
        IBaseSettler settler = super.findByUUID(uuid);
        if(settler != null) {
            return new Settler(settler.getNode());
        }
        return null;
    }

    @Override
    public Settler findById(Long id) {
        IBaseSettler settler = super.findById(id);
        if(settler != null) {
            return new Settler(settler.getNode());
        }
        return null;
    }
    
    
   
    
}
