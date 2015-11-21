/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.construction.assigner;

import com.chingo247.structurecraft.IStructureAPI;
import com.chingo247.structurecraft.construction.ICallback;
import com.chingo247.structurecraft.construction.IConstructionEntry;
import com.chingo247.structurecraft.construction.awe.AWEPlacementTask;
import com.chingo247.structurecraft.event.construction.BuildCompleteEvent;
import com.chingo247.structurecraft.event.construction.DemolitionCompleteEvent;
import com.chingo247.structurecraft.exeption.StructureException;
import com.chingo247.structurecraft.model.structure.IStructure;
import com.chingo247.structurecraft.placement.interfaces.IPlacement;
import com.sk89q.worldedit.Vector;
import java.io.IOException;
import java.util.UUID;
import org.primesoft.asyncworldedit.api.IAsyncWorldEdit;
import org.primesoft.asyncworldedit.worldedit.AsyncEditSession;

/**
 *
 * @author Chingo
 */
public abstract class AWETaskAssigner extends ATaskAssigner {

    public AWETaskAssigner(IStructureAPI structureAPI) {
        super(structureAPI);
    }

    protected abstract IPlacement getPlacementFor(final IConstructionEntry entry) throws StructureException;

    protected abstract ICallback getCallbackFor(final IConstructionEntry entry);

    @Override
    public void assignTasks(AsyncEditSession session, UUID playerOrRandomUUID, IConstructionEntry constructionEntry) throws StructureException, IOException {
        IAsyncWorldEdit asyncWorldEdit = structureAPI.getAsyncWorldEditIntegration().getAsyncWorldEdit();
        Vector position = constructionEntry.getStructure().getMin(); // Always place from the min position... 
        ICallback callback = getCallbackFor(constructionEntry);
        IPlacement placement = getPlacementFor(constructionEntry);
        constructionEntry.addTask(
                new AWEPlacementTask(
                        asyncWorldEdit,
                        constructionEntry,
                        placement,
                        playerOrRandomUUID,
                        session,
                        position,
                        callback
                )
        );
    }

}
