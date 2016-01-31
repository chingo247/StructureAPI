/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.event.structure.construction;

import com.chingo247.structurecraft.event.structure.StructureEvent;
import com.chingo247.structurecraft.model.structure.ConstructionStatus;
import com.chingo247.structurecraft.model.structure.IStructure;
import com.chingo247.structurecraft.util.IProgressable;

/**
 *
 * @author Chingo
 */
public class StructureProgressUpdateEvent extends StructureEvent {
    
    private ConstructionStatus status;
    private IProgressable progressable;

    public StructureProgressUpdateEvent(IStructure structure, IProgressable progressable, ConstructionStatus status) {
        super(structure);
        this.progressable = progressable;
        this.status = status;
    }

    public ConstructionStatus getStatus() {
        return status;
    }
    
    public IProgressable getProgressable() {
        return progressable;
    }
    
}