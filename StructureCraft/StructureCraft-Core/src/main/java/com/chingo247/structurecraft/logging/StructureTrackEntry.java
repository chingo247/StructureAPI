/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.logging;

import com.chingo247.structurecraft.model.structure.IStructure;

/**
 *
 * @author Chingo
 */
public class StructureTrackEntry {
    
    private IStructure structure;

    public StructureTrackEntry(IStructure structure) {
        this.structure = structure;
    }

    public IStructure getStructure() {
        return structure;
    }
    
}
