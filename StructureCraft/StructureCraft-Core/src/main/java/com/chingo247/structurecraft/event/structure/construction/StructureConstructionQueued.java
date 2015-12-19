/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.event.structure.construction;

import com.chingo247.structurecraft.event.structure.StructureEvent;
import com.chingo247.structurecraft.model.structure.IStructure;

/**
 *
 * @author Chingo
 */
public class StructureConstructionQueued extends StructureEvent {

    public StructureConstructionQueued(IStructure structure) {
        super(structure);
    }
    
}
