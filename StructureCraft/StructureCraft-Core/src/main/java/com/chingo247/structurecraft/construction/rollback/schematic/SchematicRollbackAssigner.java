/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.construction.rollback.schematic;

import com.chingo247.structurecraft.construction.IConstructionEntry;
import com.chingo247.structurecraft.construction.ITaskAssigner;
import com.chingo247.structurecraft.construction.rollback.IRollbackAssigner;
import com.chingo247.structurecraft.exeption.StructureException;
import com.chingo247.structurecraft.model.structure.IStructure;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import org.primesoft.asyncworldedit.worldedit.AsyncEditSession;

/**
 *
 * @author Chingo
 */
public class SchematicRollbackAssigner implements IRollbackAssigner {
    

    @Override
    public void assignTasks(AsyncEditSession session, UUID playerOrRandomUUID, IConstructionEntry constructionEntry) throws StructureException, IOException {
        IStructure structure = constructionEntry.getStructure();
        if (!canRollback(structure)) {
            throw new StructureException("Unable to rollback structure #" + structure.getId() + ": no available backups...");
        }
        
        
        
    }
    
    

    @Override
    public boolean canRollback(IStructure structure) {
        File schematic = structure.getRollbackFile(RollbackType.SCHEMATIC);
        return schematic.exists();
    }
    
}
