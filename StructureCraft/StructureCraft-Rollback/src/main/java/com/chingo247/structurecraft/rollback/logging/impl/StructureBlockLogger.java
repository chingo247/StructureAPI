/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.rollback.logging.impl;

import com.chingo247.structurecraft.rollback.tracking.impl.StructureTracker;
import com.chingo247.structurecraft.IStructureAPI;
import com.chingo247.structurecraft.StructureAPI;
import com.chingo247.structurecraft.model.structure.IStructure;
import com.chingo247.structurecraft.model.structure.StructureNode;
import com.chingo247.structurecraft.rollback.logging.ABatchBlockLogger;
import com.chingo247.structurecraft.rollback.logging.IStructureCraftBlockLogger;
import com.chingo247.xplatform.core.IWorld;
import com.chingo247.xplatform.platforms.bukkit.BukkitWorld;
import java.util.List;
import java.util.concurrent.ExecutorService;
import org.PrimeSoft.blocksHub.api.IBlockLogger;
import org.bukkit.Location;
import org.bukkit.World;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 *
 * @author Chingo
 */
public class StructureBlockLogger extends ABatchBlockLogger<StructureBlockLogEntry, StructureBlockLogTask> implements IBlockLogger,IStructureCraftBlockLogger {

    private StructureTracker tracker;
    private GraphDatabaseService graph;

    public StructureBlockLogger(ExecutorService executorService, GraphDatabaseService graph) {
        super(executorService, graph);
        this.tracker = new StructureTracker();
        StructureAPI.getInstance().getAsyncEventBus().register(tracker);
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
        return new StructureBlockLogTask(graph, blocks, getBatchSize());
    }

    @Override
    public void logBlock(Location lctn, String player, World world, int oldMaterial, byte oldData, int newMaterial, byte newData) {
        logBlock(lctn.getBlockX(), lctn.getBlockY(), lctn.getBlockZ(), player, new BukkitWorld(world), oldMaterial, oldData, newMaterial, newData);
    }

    @Override
    public String getName() {
        return "StructureCraft-StructureLogger";
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean reloadConfiguration() {
        return true;
    }

}
