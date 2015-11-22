/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.construction.assigner;

import com.chingo247.structurecraft.IStructureAPI;
import com.chingo247.structurecraft.construction.IConstructionEntry;
import com.chingo247.structurecraft.construction.awe.AWEPlacementTask;
import com.chingo247.structurecraft.exeption.StructureException;
import com.sk89q.worldedit.Vector;
import java.io.IOException;
import java.util.UUID;
import org.primesoft.asyncworldedit.playerManager.PlayerEntry;
import org.primesoft.asyncworldedit.worldedit.AsyncEditSession;

/**
 *
 * @author Chingo
 */
public class AssignerFactory implements IAssignerFactory{

    private IStructureAPI structureAPI;

    public AssignerFactory(IStructureAPI structureAPI) {
        this.structureAPI = structureAPI;
    }
    
    
    @Override
    public ITaskAssigner newSimpleBuildAssigner() {
        return new SimpleAWEBuildAss(structureAPI);
    }

    @Override
    public ITaskAssigner newSimpleDemolitionAssigner() {
        return new SimpleAWEDemolitionAss(structureAPI);
    }
    
}
