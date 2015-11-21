/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.model.world;

import com.chingo247.settlercraft.core.model.interfaces.IWorldRepository;
import java.util.UUID;

/**
 *
 * @author Chingo
 */
public interface IStructureWorldRepository extends IWorldRepository  {
    
    /**
     * Finds a StructureWorld by UUID
     * @param worldUUID The worlds UUID
     * @return The BaseWorld
     */
    @Override
    public StructureWorldNode findByUUID(UUID worldUUID);

    /**
     * Adds a world if not already added
     * @param worldName The name of the world
     * @param worldUUID The worldUUID
     * @return The world that has been created or the world that already existed with the same UUID
     */
    @Override
    public StructureWorldNode addOrGet(String worldName, UUID worldUUID);
    
    
}
