/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.placing.structure;

import com.chingo247.structureapi.model.structure.IStructure;
import com.google.common.base.Preconditions;

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
        return structure != null;
    }

    @Override
    public String error() {
        return errorMessage;
    }
    
}
