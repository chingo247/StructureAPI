/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.placing.structure;

import com.chingo247.structurecraft.model.structure.IStructure;

/**
 *
 * @author Chingo
 */
public class StructurePlaceResult implements IStructurePlaceResult {
    
    private IStructure structure = null;
    private String errorMessage  = null;
    
    void setStructure(IStructure structure) {
        this.structure = structure;
    }
    
    void setError(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public IStructure getPlacedStructure() {
        return structure;
    }

    @Override
    public boolean succes() {
        return errorMessage != null;
    }

    @Override
    public String error() {
        return errorMessage;
    }
    
}
