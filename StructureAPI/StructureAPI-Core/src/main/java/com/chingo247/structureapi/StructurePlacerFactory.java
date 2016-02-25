/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi;

import com.chingo247.structureapi.placing.structure.StructurePlacer;
import com.chingo247.xplatform.core.APlatform;
import com.chingo247.xplatform.core.IWorld;

/**
 *
 * @author Chingo
 */
public class StructurePlacerFactory  {

    private APlatform platform;

    public StructurePlacerFactory(APlatform platform) {
        this.platform = platform;
    }

    public StructurePlacer createPlacer(String worldName) {
        IWorld world = platform.getServer().getWorld(worldName);
        if (world == null) {
            throw new IllegalArgumentException("World with name '" + worldName + "' does not exist");
        }
        return new StructurePlacer(world);
    }

}
