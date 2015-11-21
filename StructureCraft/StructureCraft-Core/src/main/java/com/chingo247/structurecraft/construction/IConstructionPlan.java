/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.construction;

import com.chingo247.structurecraft.construction.options.Traversal;
import com.chingo247.structurecraft.construction.options.PlaceOptions;
import com.chingo247.structurecraft.model.structure.IStructure;
import java.util.UUID;
import org.primesoft.asyncworldedit.worldedit.AsyncEditSession;

/**
 *
 * @author Chingo
 */
public interface IConstructionPlan {
    
    
    
    IConstructionPlan setForced(boolean forced);
    
    IConstructionPlan setStructureTraversal(Traversal traversal);
    
    IConstructionPlan setOptions(PlaceOptions options);
    
    /**
     * If enabled, will check if the player has the right privileges to execute
     * @param restrictive
     * @return 
     */
    IConstructionPlan setRestrictive(boolean restrictive);
    
    IConstructionPlan setRecursive(boolean recursive);
    
    IConstructionPlan setReversedOrder(boolean reversed);
    
    IConstructionPlan setPlayer(UUID player);
    
    IConstructionPlan setEditsession(AsyncEditSession aes);
    
    void execute();
    
    AsyncEditSession getEditSession();

    UUID getPlayer();
    
    ITaskAssigner getAssigner();

    IStructure getStructure();
    
    boolean isRecursive();

    boolean isRestrictive();

    boolean isReversed();
    
    boolean isForced();
    
    Traversal getStructureTraversal();
    
    PlaceOptions getOptions();
    
}
