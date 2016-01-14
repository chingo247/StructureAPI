/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.construction.awe;

import com.chingo247.structurecraft.IStructureAPI;
import com.chingo247.structurecraft.StructureAPI;
import com.chingo247.structurecraft.construction.ITaskAssigner;
import com.chingo247.structurecraft.placement.block.IBlockPlacement;
import com.chingo247.structurecraft.placement.options.PlaceOptions;
import com.chingo247.structurecraft.placement.IPlacement;
import com.chingo247.structurecraft.plan.io.export.UnsupportedPlacementException;
import com.sk89q.worldedit.Vector;
import java.util.UUID;
import org.primesoft.asyncworldedit.api.IAsyncWorldEdit;
import org.primesoft.asyncworldedit.worldedit.AsyncEditSession;
import com.chingo247.structurecraft.construction.IStructureEntry;

/**
 *
 * @author Chingo
 */
public class AWETaskAssigner implements ITaskAssigner {

    @Override
    public void assignTasks(AsyncEditSession session, UUID playerOrRandomUUID, IStructureEntry constructionEntry) throws Exception {
        IStructureAPI structureAPI = StructureAPI.getInstance();
        IAsyncWorldEdit asyncWorldEdit = structureAPI.getAsyncWorldEditIntegration().getAsyncWorldEdit();
        Vector position = constructionEntry.getStructure().getMin(); // Always place from the min position... 
        IPlacement placement = constructionEntry.getConstructionDescription().getPlacement(constructionEntry.getStructure());
        
        System.out.println("Min: " + position);
        
        if(!(placement instanceof IBlockPlacement)) {
            throw new UnsupportedPlacementException(
                    AWETaskAssigner.class.getName() 
                            + " Only supports placements that are instance of " 
                            + IBlockPlacement.class.getName()
            );
        }
        
        System.out.println("Placement: " + placement.getClass().getSimpleName());
        
        
        AWEPlacementTask task = new AWEPlacementTask(
                        asyncWorldEdit,
                        constructionEntry,
                        placement,
                        playerOrRandomUUID,
                        session,
                        position
                );
        task.setOptions(new PlaceOptions());
        constructionEntry.addTask(task);
    }

}
