/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.rollback.logging.impl;

import com.chingo247.structurecraft.model.structure.StructureNode;
import java.util.UUID;

/**
 *
 * @author Chingo
 */
public class StructureBlockLogEntry extends BlockLogEntry {
    
    private final StructureNode structureNode;

    public StructureBlockLogEntry(StructureNode structure, UUID world, int x, int y, int z, int oldMaterial, byte oldData, int newMaterial, byte newData) {
        super(world, x, y, z, oldMaterial, oldData, newMaterial, newData);
        
        this.structureNode = structure;
    }

    public StructureNode getStructureNode() {
        return structureNode;
    }
    
    
    
    
}
