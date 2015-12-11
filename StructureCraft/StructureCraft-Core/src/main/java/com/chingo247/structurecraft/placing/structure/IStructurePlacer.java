/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.placing.structure;

import com.chingo247.settlercraft.core.Direction;
import com.chingo247.structurecraft.model.structure.IStructure;
import com.chingo247.structurecraft.placement.IPlacement;
import com.chingo247.structurecraft.plan.IStructurePlan;
import com.chingo247.structurecraft.plan.io.export.UnsupportedPlacementException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author Chingo
 */
public interface IStructurePlacer {
    
    IStructurePlacer setPlacer(UUID placer);
    
    IStructurePlacer setCheckStructureRestrictions(boolean check);
    
    IStructurePlacer setCheckOwnerRestriction(boolean enable);
    
    IStructurePlacer setInheritOwnership(boolean inherit);
    
    IStructurePlacer addMaster(UUID player);
    
    IStructurePlacer addMasters(Set<UUID> players);

    IStructurePlacer addMember(UUID player);

    IStructurePlacer addMembers(Set<UUID> players);

    IStructurePlacer addOwner(UUID player);

    IStructurePlacer addOwners(Set<UUID> players);
    
    IStructurePlacer setPrice(double price);
    
    IStructurePlacer setName(String name);
    
    IStructurePlacer setParent(IStructure parent);
    
    IStructurePlaceResult place(CuboidRegion structureRegion) throws IOException, UnsupportedPlacementException;
    
    IStructurePlaceResult place(IPlacement placement, Vector position, Direction direction) throws IOException, UnsupportedPlacementException;
    
    IStructurePlaceResult place(IStructurePlan plan, Vector position, Direction direction) throws IOException, UnsupportedPlacementException;
    
    

}
