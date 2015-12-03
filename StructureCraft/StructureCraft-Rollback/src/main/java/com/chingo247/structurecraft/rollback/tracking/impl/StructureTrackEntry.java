/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.rollback.tracking.impl;

import com.chingo247.structurecraft.model.structure.IStructure;
import com.chingo247.structurecraft.rollback.tracking.IStructureTrackerEntry;

/**
 *
 * @author Chingo
 */
public class StructureTrackEntry implements IStructureTrackerEntry {
    
    private IStructure structure;

    public StructureTrackEntry(IStructure structure) {
        this.structure = structure;
    }

    @Override
    public IStructure getStructure() {
        return structure;
    }
    
}
