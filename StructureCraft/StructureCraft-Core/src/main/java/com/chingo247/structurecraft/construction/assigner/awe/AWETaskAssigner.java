/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.construction.assigner.awe;

import com.chingo247.structurecraft.IStructureAPI;
import com.chingo247.structurecraft.StructureAPI;
import com.chingo247.structurecraft.construction.IConstructionEntry;
import com.chingo247.structurecraft.construction.options.IPlaceOptionsAssigner;
import com.chingo247.structurecraft.construction.ITaskCallback;
import com.chingo247.structurecraft.construction.assigner.ITaskAssigner;
import com.chingo247.structurecraft.construction.awe.AWEPlacementTask;
import com.chingo247.structurecraft.construction.options.PlaceOptions;
import com.chingo247.structurecraft.exeption.StructureException;
import com.chingo247.structurecraft.placement.interfaces.IPlacement;
import com.chingo247.structurecraft.placement.interfaces.RotationalPlacement;
import com.sk89q.worldedit.Vector;
import java.io.IOException;
import java.util.UUID;
import org.primesoft.asyncworldedit.api.IAsyncWorldEdit;
import org.primesoft.asyncworldedit.worldedit.AsyncEditSession;

/**
 *
 * @author Chingo
 */
public abstract class AWETaskAssigner<T extends IConstructionEntry> implements ITaskAssigner<T> {
    
    protected abstract IPlacement getPlacementFor(final IConstructionEntry entry) throws StructureException;

    protected abstract ITaskCallback getCallbackFor(final IConstructionEntry entry);

    @Override
    public void assignTasks(AsyncEditSession session, UUID playerOrRandomUUID, IConstructionEntry constructionEntry, IPlaceOptionsAssigner optionsAssigner) throws StructureException, IOException {
        IStructureAPI structureAPI = StructureAPI.getInstance();
        IAsyncWorldEdit asyncWorldEdit = structureAPI.getAsyncWorldEditIntegration().getAsyncWorldEdit();
        Vector position = constructionEntry.getStructure().getMin(); // Always place from the min position... 
        ITaskCallback callback = getCallbackFor(constructionEntry);
        IPlacement placement = getPlacementFor(constructionEntry);
        
        if (placement instanceof RotationalPlacement) {
            RotationalPlacement rt = (RotationalPlacement) placement;
            rt.rotate(constructionEntry.getStructure().getDirection().getRotation());
        }
        
        AWEPlacementTask task = new AWEPlacementTask(
                        asyncWorldEdit,
                        constructionEntry,
                        placement,
                        playerOrRandomUUID,
                        session,
                        position,
                        callback
                );
        PlaceOptions options = optionsAssigner != null ? optionsAssigner.getPlaceOptionsFor(constructionEntry) : new PlaceOptions();
        task.setOptions(options != null ? options : new PlaceOptions());
        constructionEntry.addTask(task);
    }

}
