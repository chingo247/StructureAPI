/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.construction.assigner.awe;

import com.chingo247.structurecraft.construction.assigner.awe.SimpleAWEBuildAss;
import com.chingo247.structurecraft.construction.assigner.awe.SimpleAWEDemolitionAss;
import com.chingo247.structurecraft.IStructureAPI;
import com.chingo247.structurecraft.construction.assigner.IAssignerFactory;
import com.chingo247.structurecraft.construction.assigner.ITaskAssigner;

/**
 *
 * @author Chingo
 */
public class AWEAssignerFactory implements IAssignerFactory {

    private IStructureAPI structureAPI;

    public AWEAssignerFactory(IStructureAPI structureAPI) {
        this.structureAPI = structureAPI;
    }
    
    
    @Override
    public ITaskAssigner newSimpleBuildAssigner() {
        return new SimpleAWEBuildAss();
    }

    @Override
    public ITaskAssigner newSimpleDemolitionAssigner() {
        return new SimpleAWEDemolitionAss();
    }

    @Override
    public ITaskAssigner newSimpleRestoreAssigner() {
        return new SimpleAWERollbackAss();
    }
    
}
