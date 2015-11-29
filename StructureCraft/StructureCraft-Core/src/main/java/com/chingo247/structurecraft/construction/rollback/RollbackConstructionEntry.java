/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.construction.rollback;

import com.chingo247.structurecraft.construction.ConstructionEntry;
import com.chingo247.structurecraft.construction.IConstructionExecutor;
import com.chingo247.structurecraft.model.structure.IStructure;

/**
 *
 * @author Chingo
 */
public class RollbackConstructionEntry extends ConstructionEntry implements IRollbackConstructionEntry {
    
    
    
    protected RollbackConstructionEntry(IConstructionExecutor executor, IStructure structture) {
        super(executor, structture);
    }

    @Override
    public IRollbackPlacementSource getPlacementSource() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
