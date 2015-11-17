/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft;

import com.chingo247.structurecraft.placing.structure.StructurePlacer;
import com.chingo247.structurecraft.placing.structure.IStructurePlacer;
import com.chingo247.structurecraft.placing.structure.IStructurePlacerFactory;
import com.chingo247.xplatform.core.APlatform;
import com.chingo247.xplatform.core.IWorld;

/**
 * 
 * @author Chingo
 */
class StructurePlacerFactory implements IStructurePlacerFactory {
    
    private StructureAPI structureAPI;

    StructurePlacerFactory(StructureAPI structureAPI) {
        this.structureAPI = structureAPI;
    }

    @Override
    public IStructurePlacer createPlacer(String worldName) {
        APlatform platform = structureAPI.getPlatform();
        IWorld world = platform.getServer().getWorld(worldName);
        if(world == null) {
            throw new IllegalArgumentException("World with name '" + worldName + "' does not exist");
        }
        
        return new StructurePlacer(
                world
        );
    }
    
    
    
    
    
}
