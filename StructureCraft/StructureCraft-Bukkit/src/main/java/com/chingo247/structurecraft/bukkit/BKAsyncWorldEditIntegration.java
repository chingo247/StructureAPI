/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.bukkit;

import com.chingo247.structurecraft.IAsyncWorldEditIntegration;
import com.chingo247.structurecraft.StructureAPI;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.primesoft.asyncworldedit.AsyncWorldEditMain;
import org.primesoft.asyncworldedit.api.IAsyncWorldEdit;
import org.primesoft.asyncworldedit.blockPlacer.BlockPlacer;
import org.primesoft.asyncworldedit.playerManager.PlayerEntry;

/**
 *
 * @author Chingo
 */
public class BKAsyncWorldEditIntegration implements IAsyncWorldEditIntegration {

    @Override
    public IAsyncWorldEdit getAsyncWorldEdit() {
        return AsyncWorldEditMain.getInstance();
    }

    @Override
    public boolean isQueueLocked(UUID player) {
        BlockPlacer blockPlacer = (BlockPlacer) AsyncWorldEditMain.getInstance().getBlockPlacer();
        Class blockPlacerClass = blockPlacer.getClass();
        Field f;
        try {
            f = blockPlacerClass.getDeclaredField("m_lockedQueues");
            f.setAccessible(true);

            HashSet s = (HashSet) f.get(blockPlacer);

            PlayerEntry playerEntry = new PlayerEntry(null, player);
            return s.contains(playerEntry);

        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(StructureAPI.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    
    
}
