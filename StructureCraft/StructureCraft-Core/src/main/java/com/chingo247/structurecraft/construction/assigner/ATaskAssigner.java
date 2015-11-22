/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.construction.assigner;

import com.chingo247.structurecraft.IStructureAPI;
import org.primesoft.asyncworldedit.api.IAsyncWorldEdit;

/**
 *
 * @author Chingo
 */
public abstract class ATaskAssigner implements ITaskAssigner {
    
    protected final IStructureAPI structureAPI;
    protected final IAsyncWorldEdit worldEdit;

    public ATaskAssigner(IStructureAPI structureAPI) {
        this.structureAPI = structureAPI;
        this.worldEdit = structureAPI.getAsyncWorldEditIntegration().getAsyncWorldEdit();
    }
    
    
    
    
}
