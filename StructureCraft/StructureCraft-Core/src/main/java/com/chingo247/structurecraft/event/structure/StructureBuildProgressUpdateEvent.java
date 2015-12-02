/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.event.structure;

import com.chingo247.structurecraft.model.structure.IStructure;
import com.chingo247.structurecraft.util.IProgressable;

/**
 *
 * @author Chingo
 */
public class StructureBuildProgressUpdateEvent extends StructureProgressUpdateEvent {

    public StructureBuildProgressUpdateEvent(IStructure structure, IProgressable progressable) {
        super(structure, progressable);
    }
    
}
