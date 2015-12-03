/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.rollback;

import com.chingo247.settlercraft.core.platforms.services.IEconomyProvider;
import com.chingo247.structurecraft.AStructureEventListener;
import com.chingo247.structurecraft.StructureAPI;
import com.chingo247.structurecraft.model.structure.ConstructionStatus;
import static com.chingo247.structurecraft.model.structure.ConstructionStatus.ROLLING_BACK;
import com.chingo247.structurecraft.model.structure.IStructure;
import com.chingo247.structurecraft.rollback.event.StructureRollbackCompleteEvent;
import com.chingo247.structurecraft.rollback.event.StructureRollbackEvent;
import com.chingo247.structurecraft.rollback.event.StructureRollbackProgressUpdateEvent;
import com.chingo247.xplatform.core.IColors;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

/**
 *
 * @author Chingo
 */
public class RollbackEventListener extends AStructureEventListener {

    public RollbackEventListener(IEconomyProvider economyProvider) {
        super(economyProvider);
    }
    
    
    
    @AllowConcurrentEvents
    @Subscribe
    public void onStructureRollbackComplete(StructureRollbackCompleteEvent bce) {
        IColors colors = StructureAPI.getInstance().getPlatform().getChatColors();
        IStructure structure = bce.getStructure();
        String message = colors.red() + "ROLLBACK COMPLETE " + colors.reset() + getStructureString(structure);
        handleStructure(bce.getStructure(), message, ConstructionStatus.REMOVED);
    }

    @AllowConcurrentEvents
    @Subscribe
    public void onStructureRollback(StructureRollbackEvent sbe) {
        IColors colors = StructureAPI.getInstance().getPlatform().getChatColors();
        IStructure structure = sbe.getStructure();
        String message = colors.yellow() + "ROLLING BACK " + colors.reset() + getStructureString(structure);
        handleStructure(structure, message, ROLLING_BACK);
    }
    
    @AllowConcurrentEvents
    @Subscribe
    public void onStructureRollbackUpdate(StructureRollbackProgressUpdateEvent sde) {
        IColors colors = StructureAPI.getInstance().getPlatform().getChatColors();
        IStructure structure = sde.getStructure();
        String progress = TWO_DECIMAL.format(sde.getProgressable().getProgress());
        String message = colors.yellow() + "ROLLING BACK " + colors.reset() + progress + "% " + getStructureString(structure);
        handleStructure(structure, message, ROLLING_BACK);
    }

    
}
