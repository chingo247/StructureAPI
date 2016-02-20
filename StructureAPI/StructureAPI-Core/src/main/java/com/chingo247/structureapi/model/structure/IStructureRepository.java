/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.model.structure;

import com.chingo247.settlercraft.core.Direction;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import java.util.Collection;
import java.util.UUID;

/**
 *
 * @author Chingo
 */
public interface IStructureRepository {
    
    StructureNode findById(Long id);
    
    /**
     * Adds a structure to the graph
     * @param name The name of the structure
     * @param position The position
     * @param region The region
     * @param direction The direction
     * @param price The price of the structure
     * @return 
     */
    StructureNode addStructure(String name, Vector position, CuboidRegion region, Direction direction, double price);
    
    Collection<StructureNode> findByWorld(UUID worldUUID);
    
    Collection<StructureNode> findByOwner(UUID settlerUUID);
    
    Collection<StructureNode> findStructuresDeletedAfter(UUID worldUUID, long date);
    
    Collection<StructureNode> findCreatedAfter(UUID worldUUID, long date);
    
    Collection<StructureNode> findStructuresWithin(UUID worldUUID, CuboidRegion region, int limit);
    
    /**
     * Finds the smallest structure on point. As structures may have substructures, the smallest structure on a point is considered a 'leaf' structure.
     * This method is used to check if a new structure is qualified as substructure, as substructures must fit completely within their parent or placed completely outside.
     * @param worldUUID
     * @param position
     * @return The structure
     */
    StructureNode findStructureOnPosition(UUID worldUUID, Vector position);
    
    int countStructuresOfSettler(UUID settlerUUID);
    
    int countStructuresWithinWorld(UUID worldUUID);
    
    boolean hasStructuresWithin(UUID worldUUID, CuboidRegion region);
    
}
