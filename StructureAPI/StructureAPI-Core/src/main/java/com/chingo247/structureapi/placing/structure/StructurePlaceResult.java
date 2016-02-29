/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.placing.structure;

import com.chingo247.structureapi.model.structure.Structure;
import com.chingo247.structureapi.placing.IPlaceResult;

/**
 *
 * @author Chingo
 */
public class StructurePlaceResult implements IPlaceResult<Structure> {
    
    private Structure structure = null;
    private String errorMessage  = null;
    
    void setStructure(Structure structure) {
        this.structure = structure;
    }
    
    void setError(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public Structure getPlaced() {
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
