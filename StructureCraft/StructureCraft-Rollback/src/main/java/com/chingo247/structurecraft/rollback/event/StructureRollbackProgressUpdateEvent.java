/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.rollback.event;

import com.chingo247.structurecraft.event.structure.StructureProgressUpdateEvent;
import com.chingo247.structurecraft.model.structure.IStructure;
import com.chingo247.structurecraft.util.IProgressable;

/**
 *
 * @author Chingo
 */
public class StructureRollbackProgressUpdateEvent extends StructureProgressUpdateEvent {

    public StructureRollbackProgressUpdateEvent(IStructure structure, IProgressable progressable) {
        super(structure, progressable);
    }
    
}
