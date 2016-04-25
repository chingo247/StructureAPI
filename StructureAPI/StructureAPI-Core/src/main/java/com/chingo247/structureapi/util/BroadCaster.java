/*
 * Copyright (C) 2016 Chingo
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
package com.chingo247.structureapi.util;

import com.chingo247.settlercraft.core.SettlerCraft;
import com.chingo247.settlercraft.core.model.settler.SettlerNode;
import com.chingo247.structureapi.exeption.StructureNotFoundException;
import com.chingo247.structureapi.model.structure.StructureNode;
import com.chingo247.structureapi.model.structure.StructureRepository;
import com.chingo247.xplatform.core.IPlayer;
import java.util.UUID;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

/**
 *
 * @author Chingo
 */
public class BroadCaster {
    
    private GraphDatabaseService graph;
    private StructureRepository structureRepository;

    public BroadCaster(GraphDatabaseService graph) {
        this.graph = graph;
        this.structureRepository = new StructureRepository(graph);
    }
    
    /**
     * Broadcasts a message to the owners of a structure
     * @param structureId The id of the structure
     * @param message The message to send
     * 
     * NOTE: This method creates and terminates a transaction. Therefore don't use this method within 
     * a active transaction!
     */
    public void broadcastToOwners(long structureId, String message) throws StructureNotFoundException {
        try (Transaction tx = graph.beginTx()) {
            StructureNode structure = structureRepository.findById(structureId);
             
            if(structure == null) {
                throw new StructureNotFoundException("Coulnd't find structure with id '" + structureId + "'");
            }
            broadcastToOwners(structure, message);
            tx.success();
        }
    }
    
    /**
     * Broadcasts a message to owners. NOTE: This method <b>requires</b> an active transaction!
     * @param structurenode The structureNode
     * @param message The message to send
     */
    public void broadcastToOwners(StructureNode structurenode, String message) {
        for(SettlerNode sl : structurenode.getOwnerDomain().getOwners()) {
           UUID uuid = sl.getUniqueId();
           IPlayer player = SettlerCraft.getInstance().getPlatform().getPlayer(uuid);
           if(player != null) {
               player.sendMessage(message);
           }
        }
    }
        
    
    
}
