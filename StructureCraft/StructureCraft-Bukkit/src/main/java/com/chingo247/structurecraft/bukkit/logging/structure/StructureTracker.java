/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.bukkit.logging.structure;

import com.chingo247.structurecraft.construction.awe.AWEJobManager;
import com.chingo247.structurecraft.construction.awe.AWEPlacementTask;
import com.chingo247.structurecraft.event.async.StructureJobAddedEvent;
import com.chingo247.structurecraft.event.task.StructureTaskCancelledEvent;
import com.chingo247.structurecraft.event.task.StructureTaskCompleteEvent;
import com.chingo247.structurecraft.model.structure.IStructure;
import com.google.common.collect.Maps;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.primesoft.asyncworldedit.playerManager.PlayerEntry;

/**
 *
 * @author Chingo
 */
class StructureTracker {

    private static final Logger LOG = Logger.getLogger(StructureTracker.class.getName());
    private Map<String, Map<Long, IStructure>> tracking;
    private Map<Long, String> playerStructure;
    private final Object mutex = new Object();

    public StructureTracker() {
        this.tracking = Maps.newHashMap();
        this.playerStructure = Maps.newHashMap();
    }
    

    @Subscribe
    @AllowConcurrentEvents
    public void onStructureJobAdded(StructureJobAddedEvent addedEvent) {
        try {
            System.out.println("[StructureTracker]: On Structure Job Added");
            PlayerEntry playerEntry = addedEvent.getPlayerEntry();
            String player = playerEntry.getName();
            IStructure structure = addedEvent.getStructure();

            synchronized (mutex) {
                Map<Long, IStructure> structures = tracking.get(player);
                if (structures == null) {
                    structures = new HashMap<>();
                    tracking.put(player, structures);
                }
                playerStructure.put(structure.getId(), player);
                structures.put(structure.getId(), structure);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    @Subscribe
    @AllowConcurrentEvents
    public void onTaskCompleted(StructureTaskCompleteEvent stce) {
        try {
            System.out.println("[StructureTracker]: On Structure Job Completed");
            if (stce.getTask() instanceof AWEPlacementTask) {
                AWEPlacementTask task = (AWEPlacementTask) stce.getTask();
                IStructure structure = AWEJobManager.getInstance().getStructure(task.getUUID());
                remove(structure);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    @Subscribe
    @AllowConcurrentEvents
    public void onTaskCancelled(StructureTaskCancelledEvent stce) {
        System.out.println("[StructureTracker]: On Structure Job Cancelled");
        try {
            if (stce.getTask() instanceof AWEPlacementTask) {
                AWEPlacementTask task = (AWEPlacementTask) stce.getTask();
                IStructure structure = AWEJobManager.getInstance().getStructure(task.getUUID());
                remove(structure);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    private void remove(IStructure structure) {
        if (structure != null) {
            synchronized (mutex) {
                String player = playerStructure.get(structure.getId());
                if (player != null) {
                    Map<Long, IStructure> structures = tracking.get(player);
                    if (structures != null) {
                        structures.remove(structure.getId());
                    }
                }
            }
        }
    }

    public IStructure getStructure(String player, String world, int x, int y, int z) {
        synchronized (mutex) {
            Map<Long, IStructure> structures = tracking.get(player);
            if (structures != null) {
                Vector v = new BlockVector(x, y, z);
                for (IStructure structure : structures.values()) {
                    if (structure.getCuboidRegion().contains(v)) {
                        String worldName = structure.getWorldName();
                        if (worldName.equals(world)) {
                            return structure;
                        }
                    }
                }
            }
        }
        return null;
    }

}
