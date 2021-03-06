/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.event.structure;

import com.chingo247.structureapi.model.structure.ConstructionStatus;
import com.chingo247.structureapi.model.structure.Structure;

/**
 * Fired after the structure has changed state
 * @author Chingo
 */
public class StructureStateChangeEvent extends StructureEvent {
    
    private ConstructionStatus newStatus;
    
    public StructureStateChangeEvent(Structure structure, ConstructionStatus newStatus) {
        super(structure);
        this.newStatus = newStatus;
    }

    public ConstructionStatus getNewStatus() {
        return newStatus;
    }
    
    

}
