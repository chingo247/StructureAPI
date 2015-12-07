/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.construction.safe.schematic;

import com.chingo247.structurecraft.construction.ITaskAssigner;

/**
 *
 * @author Chingo
 */
public class SchematicSavingAssignerFactory {
    
    /**
     * Creates a task assigner that will assign tasks that will ensure a backup of the area is made before placing.
     * The backup written to a schematic file.
     * @return The task assigner
     */
    public ITaskAssigner createSavingSchematicBuildAssigner() {
        return new SchematicSavingBuildAssigner();
    }
    
    /**
     * Creates a task assigner that will assign tasks that will ensure a backup of the area is made before placing.
     * The backup written to a schematic file.
     * @return The task assigner
     */
    public ITaskAssigner createSavingSchematicDemolitionAssigner() {
        return new SchematicSavingDemolishAssigner();
    }
    
    
    
}
