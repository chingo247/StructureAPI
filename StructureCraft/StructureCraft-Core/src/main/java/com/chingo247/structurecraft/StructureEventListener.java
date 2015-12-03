/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft;

import com.chingo247.settlercraft.core.platforms.services.IEconomyProvider;
import com.chingo247.structurecraft.event.structure.StructureBuildCompleteEvent;
import com.chingo247.structurecraft.event.structure.StructureBuildProgressUpdateEvent;
import com.chingo247.structurecraft.event.structure.StructureBuildingEvent;
import com.chingo247.structurecraft.event.structure.StructureConstructionCancelledEvent;
import com.chingo247.structurecraft.event.structure.StructureConstructionQueued;
import com.chingo247.structurecraft.event.structure.StructureDemolishEvent;
import com.chingo247.structurecraft.event.structure.StructureDemolitionCompleteEvent;
import com.chingo247.structurecraft.event.structure.StructureDemolitionProgressUpdateEvent;
import com.chingo247.structurecraft.model.structure.ConstructionStatus;
import static com.chingo247.structurecraft.model.structure.ConstructionStatus.BUILDING;
import static com.chingo247.structurecraft.model.structure.ConstructionStatus.DEMOLISHING;
import static com.chingo247.structurecraft.model.structure.ConstructionStatus.QUEUED;
import static com.chingo247.structurecraft.model.structure.ConstructionStatus.REMOVED;
import static com.chingo247.structurecraft.model.structure.ConstructionStatus.ROLLING_BACK;
import static com.chingo247.structurecraft.model.structure.ConstructionStatus.STOPPED;
import com.chingo247.structurecraft.model.structure.IStructure;
import com.chingo247.xplatform.core.IColors;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import java.util.logging.Logger;

/**
 *
 * @author Chingo
 */
class StructureEventListener extends AStructureEventListener {

    private static final Logger LOG = Logger.getLogger(StructureEventListener.class.getName());

    public StructureEventListener(IEconomyProvider economyProvider) {
        super(economyProvider);
    }


    


    @AllowConcurrentEvents
    @Subscribe
    public void onStructureBuildComplete(StructureBuildCompleteEvent bce) {
        IColors colors = StructureAPI.getInstance().getPlatform().getChatColors();
        IStructure structure = bce.getStructure();
        String message = colors.green() + "COMPLETE " + colors.reset() + getStructureString(structure);
        handleStructure(bce.getStructure(), message, ConstructionStatus.COMPLETED);
    }

    @AllowConcurrentEvents
    @Subscribe
    public void onStructureBuilding(StructureBuildingEvent sbe) {
        IColors colors = StructureAPI.getInstance().getPlatform().getChatColors();
        IStructure structure = sbe.getStructure();
        String message = colors.yellow() + "BUILDING " + colors.reset() + getStructureString(structure);
        handleStructure(structure, message, BUILDING);
    }
    
    @AllowConcurrentEvents
    @Subscribe
    public void onStructureBuildingUpdate(StructureBuildProgressUpdateEvent sde) {
        IColors colors = StructureAPI.getInstance().getPlatform().getChatColors();
        IStructure structure = sde.getStructure();
        String progress = TWO_DECIMAL.format(sde.getProgressable().getProgress());
        String message = colors.yellow() + "BUILDING " + colors.reset() + progress + "% " + getStructureString(structure);
        handleStructure(structure, message, BUILDING);
    }

    @AllowConcurrentEvents
    @Subscribe
    public void onStructureDemolitionComplete(StructureDemolitionCompleteEvent dce) {
        IColors colors = StructureAPI.getInstance().getPlatform().getChatColors();
        IStructure structure = dce.getStructure();
        String message = colors.red() + "REMOVED " + colors.reset() + getStructureString(structure);
        handleStructure(structure, message, REMOVED);
    }

    @AllowConcurrentEvents
    @Subscribe
    public void onStructureDemolition(StructureDemolishEvent sde) {
        IColors colors = StructureAPI.getInstance().getPlatform().getChatColors();
        IStructure structure = sde.getStructure();
        String message = colors.yellow() + "DEMOLISHING " + colors.reset() + getStructureString(structure);
        handleStructure(structure, message, DEMOLISHING);
    }
    
    @AllowConcurrentEvents
    @Subscribe
    public void onStructureDemolitionUpdate(StructureDemolitionProgressUpdateEvent sde) {
        IColors colors = StructureAPI.getInstance().getPlatform().getChatColors();
        IStructure structure = sde.getStructure();
        String progress = TWO_DECIMAL.format(sde.getProgressable().getProgress());
        String message = colors.yellow() + "DEMOLISHING " + colors.reset() + progress + "% " + getStructureString(structure);
        handleStructure(structure, message, DEMOLISHING);
    }

    @AllowConcurrentEvents
    @Subscribe
    public void onStructureConstructionQueued(StructureConstructionQueued scq) {
        IColors colors = StructureAPI.getInstance().getPlatform().getChatColors();
        IStructure structure = scq.getStructure();
        String message = colors.yellow() + "QUEUED " + colors.reset() + getStructureString(structure);
        handleStructure(structure, message, QUEUED);
    }

    @AllowConcurrentEvents
    @Subscribe
    public void onStructureConstructionCancelled(StructureConstructionCancelledEvent scq) {
        IColors colors = StructureAPI.getInstance().getPlatform().getChatColors();
        IStructure structure = scq.getStructure();
        String message = colors.red() + "STOPPED " + colors.reset() + getStructureString(structure);
        handleStructure(structure, message, STOPPED);
    }
    
    
}
