/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.event.structure.construction;

import com.chingo247.structureapi.event.structure.StructureEvent;
import com.chingo247.structureapi.model.structure.Structure;

/**
 *
 * @author Chingo
 */
public class StructureConstructionCancelledEvent extends StructureEvent{

    public StructureConstructionCancelledEvent(Structure structure) {
        super(structure);
    }
    
}
