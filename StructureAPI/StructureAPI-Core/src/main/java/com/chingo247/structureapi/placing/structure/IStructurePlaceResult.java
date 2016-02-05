/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.placing.structure;

import com.chingo247.structureapi.model.structure.IStructure;
import com.chingo247.structureapi.placing.IPlaceResult;

/**
 *
 * @author Chingo
 */
public interface IStructurePlaceResult extends IPlaceResult {
    
    /**
     * Gets the structure that was placed. May return null if placing was not successful.
     * @return The structure.
     */
    IStructure getPlacedStructure();
    
}
