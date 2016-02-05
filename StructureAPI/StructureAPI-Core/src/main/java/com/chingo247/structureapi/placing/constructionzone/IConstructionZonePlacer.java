/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.placing.constructionzone;

import com.chingo247.structureapi.model.AccessType;
import com.sk89q.worldedit.regions.CuboidRegion;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author Chingo
 */
public interface IConstructionZonePlacer {
    
    IConstructionZonePlacer addMaster(UUID player);
    
    IConstructionZonePlacer addMasters(Set<UUID> players);

    IConstructionZonePlacer addMember(UUID player);

    IConstructionZonePlacer addMembers(Set<UUID> players);

    IConstructionZonePlacer addOwner(UUID player);

    IConstructionZonePlacer addOwners(Set<UUID> players);

    IConstructionZonePlacer setAccess(AccessType accessType);

    IConstructionZonePlaceResult place(CuboidRegion region);

}
