/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.logging;

import com.chingo247.structurecraft.IStructureAPI;
import com.chingo247.structurecraft.model.structure.IStructure;
import com.chingo247.structurecraft.model.structure.StructureNode;
import com.chingo247.xplatform.core.IWorld;
import com.chingo247.xplatform.platforms.bukkit.BukkitWorld;
import java.util.List;
import java.util.concurrent.ExecutorService;
import org.PrimeSoft.blocksHub.api.IBlockLogger;
import org.bukkit.Location;
import org.bukkit.World;

/**
 *
 * @author Chingo
 */
public class StructureBlockLogger extends BatchBlockLogger<StructureBlockLogEntry, StructureBlockLogTask> implements IBlockLogger {

    private StructureTracker tracker;
    private IStructureAPI structureAPI;

    public StructureBlockLogger(String name, ExecutorService executorService, IStructureAPI structureAPI) {
        super(name, executorService);

        this.structureAPI = structureAPI;
        this.tracker = new StructureTracker();
        structureAPI.getAsyncEventBus().register(tracker);
    }

    @Override
    protected StructureBlockLogEntry makeEntry(int x, int y, int z, String player, IWorld world, int oldMaterial, byte oldData, int newMaterial, byte newData) {
        IStructure structure = tracker.getStructure(player, world.getName(), x, y, z);
        if (structure != null) {
            return new StructureBlockLogEntry(
                    new StructureNode(structure.getUnderlyingNode()),
                    world.getUUID(),
                    x,
                    y,
                    z,
                    oldMaterial,
                    oldData,
                    newMaterial,
                    newData
            );
        }
        return null;
    }

    @Override
    protected StructureBlockLogTask makeTask(List<StructureBlockLogEntry> blocks) {
        return new StructureBlockLogTask(structureAPI.getGraphDatabase(), blocks, getBatchSize());
    }

    @Override
    public void logBlock(Location lctn, String player, World world, int oldMaterial, byte oldData, int newMaterial, byte newData) {
        logBlock(lctn.getBlockX(), lctn.getBlockY(), lctn.getBlockZ(), player, new BukkitWorld(world), oldMaterial, oldData, newMaterial, newData);
    }

}
