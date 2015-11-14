/*
 * Copyright (C) 2015 Chingo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.chingo247.structurecraft.platform.services.holograms;

import com.chingo247.settlercraft.core.SettlerCraft;
import com.chingo247.settlercraft.core.event.EventManager;
import com.chingo247.settlercraft.core.model.interfaces.IWorld;
import com.chingo247.structurecraft.event.StructureCreateEvent;
import com.chingo247.structurecraft.event.StructureStateChangeEvent;
import com.chingo247.structurecraft.model.hologram.StructureHologram;
import com.chingo247.structurecraft.model.hologram.StructureHologramNode;
import com.chingo247.structurecraft.model.hologram.StructureHologramRepository;
import com.chingo247.structurecraft.model.structure.ConstructionStatus;
import com.chingo247.structurecraft.model.structure.StructureNode;
import com.chingo247.structurecraft.model.hologram.IStructureHologram;
import com.chingo247.structurecraft.model.hologram.IStructureHologramRepository;
import com.chingo247.structurecraft.model.structure.Structure;
import com.chingo247.structurecraft.StructureAPI;
import com.chingo247.xplatform.core.APlatform;
import com.chingo247.xplatform.core.IColors;
import com.chingo247.xplatform.core.IPlugin;
import com.chingo247.xplatform.core.IScheduler;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.world.World;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

/**
 *
 * @author Chingo
 */
public class StructureHologramManager {

    private static StructureHologramManager instance;
    private static final String PLUGIN = "SettlerCraft-StructureAPI";
    private static final int STATUS_LINE = 2;

    private final Map<Long, List<Hologram>> holograms = Collections.synchronizedMap(new HashMap<Long, List<Hologram>>());
    private final APlatform platform;
    private final IColors color;
    private final IScheduler scheduler;
    private final GraphDatabaseService graph;
    private final IStructureHologramRepository structureHologramRepository;
    private IPlugin plugin;

    private HologramsProvider hologramsProvider;

    private StructureHologramManager() {
        this.platform = SettlerCraft.getInstance().getPlatform();
        this.color = platform.getChatColors();
        this.scheduler = platform.getServer().getScheduler(platform.getServer().getPlugin(PLUGIN));
        this.graph = SettlerCraft.getInstance().getNeo4j();
        this.structureHologramRepository = new StructureHologramRepository(graph);
    }

    public static StructureHologramManager getInstance() {
        if (instance == null) {
            instance = new StructureHologramManager();
        }
        return instance;
    }

    public void inititialize(IPlugin plugin) {
        this.plugin = plugin;
        invalidate();
        setupUnchecked();
        EventManager.getInstance().getEventBus().register(this); // Should be registered once...
    }

    public HologramsProvider getHologramsProvider() {
        return hologramsProvider;
    }

    public boolean hasHologramProvider() {
        return hologramsProvider != null;
    }

    public void setHologramProvider(HologramsProvider hologramsProvider) {
        Preconditions.checkNotNull(hologramsProvider);
        if (this.hologramsProvider == null && StructureAPI.getInstance().getConfig().isUseHolograms()) {
            this.hologramsProvider = hologramsProvider;
            initHolos();
        }
    }

    private void registerStructureHologram(Structure structure, Hologram hologram) {
        if (holograms.get(structure.getId()) == null) {
            holograms.put(structure.getId(), new ArrayList<Hologram>());
        }

        hologram.addLine("#" + color.gold() + String.valueOf(structure.getId()));
        hologram.addLine(color.blue() + structure.getName());
        hologram.addLine(getStatusString(structure));
        synchronized (holograms) {
            holograms.get(structure.getId()).add(hologram);
        }
    }

    @Subscribe
    @AllowConcurrentEvents
    public void onStructureCreate(StructureCreateEvent structureCreateEvent) {
        try {
            if (hologramsProvider == null || !StructureAPI.getInstance().getConfig().isUseHolograms()) {
                return;
            }
            final Structure structure = structureCreateEvent.getStructure();

            // Assures non-async behavior - Fixes concurrent exceptions that could be thrown
            scheduler.runSync(new Runnable() {

                @Override
                public void run() {

                    try {
                        World w = SettlerCraft.getInstance().getWorld(structure.getWorldUUID());
                        createHologramForStructure(PLUGIN, w, structure.translateRelativeLocation(new Vector(0, 2, 0)), structure);
                    } catch (Exception ex) {
                        Logger.getLogger(StructureHologramManager.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    }

                }
            });
        } catch (Exception ex) {
            Logger.getLogger(StructureHologramManager.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    @Subscribe
    @AllowConcurrentEvents
    public void onStructureStateChange(StructureStateChangeEvent changeEvent) {
        try {
            if (hologramsProvider == null || !StructureAPI.getInstance().getConfig().isUseHolograms()) {
                return;
            }
            final Structure structure = changeEvent.getStructure();
            scheduler.runSync(new Runnable() {

                @Override
                public void run() {

                    try {
                        List<Hologram> holos = holograms.get(structure.getId());
                        if (holos != null && !holos.isEmpty()) {
                            synchronized (holos) {
                                ConstructionStatus status = structure.getStatus();
                                if (status == ConstructionStatus.REMOVED) {
                                    for (Hologram holo : holos) {
                                        holo.delete();
                                    }
                                    synchronized (holograms) {
                                        holograms.remove(structure.getId());
                                    }
                                } else {
                                    String statusString = getStatusString(structure);
                                    for (Hologram holo : holos) {
                                        holo.removeLine(STATUS_LINE);
                                        holo.addLine(statusString);
                                    }

                                }
                            }
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(StructureHologramManager.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    }

                }
            });
        } catch (Exception ex) {
            Logger.getLogger(StructureHologramManager.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }

    }

    private String getStatusString(Structure structure) {
        ConstructionStatus state = structure.getStatus();
        String statusString;
        switch (state) {
            case DEMOLISHING:
                statusString = color.yellow();
                statusString += state.name();
                break;
            case BUILDING:
                statusString = color.yellow();
                statusString += state.name();
                break;
            case COMPLETED:
                statusString = "";
                break;
            case STOPPED:
            case ON_HOLD:
                statusString = color.red();
                statusString += state.name();
                break;
            default:
                statusString = color.white();
                statusString += state.name();
                break;
        }
        return statusString;
    }

    private void initHolos() {
        final Queue<StructureHologram> hologramQueue = new LinkedList<>();
        try (Transaction tx = graph.beginTx()) {
            Collection<StructureHologramNode> structureHolograms = structureHologramRepository.findAll();

            for (StructureHologramNode shn : structureHolograms) {
                if (shn.getStructure() != null) {
                    hologramQueue.add(new StructureHologram(shn));
                }
            }

            tx.success();
        }
        int count = 0;
        while (hologramQueue.peek() != null) {
            if (count % 100 == 0) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Logger.getLogger(StructureHologramManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            final IStructureHologram hologram = hologramQueue.poll();
            final Structure structure = hologram.getStructure();
            final Vector position = structure.translateRelativeLocation(new Vector(hologram.getRelativeX(), hologram.getRelativeY(), hologram.getRelativeZ()));

            Structure s = hologram.getStructure();
            if (s == null) {
                continue;
            }
            UUID worldUUID = s.getWorldUUID();
            final World w = SettlerCraft.getInstance().getWorld(worldUUID);

            if (w != null) {
                scheduler.runSync(new Runnable() {

                    @Override
                    public void run() {
                        Hologram h = hologramsProvider.createHologram(plugin.getName(), w, position);
                        registerStructureHologram(structure, h);
                    }
                });
            }
            count++;
        }
    }

    private void setupUnchecked() {
        // Find unchecked structures without holos
        List<StructureNode> structures = Lists.newArrayList();
        try (Transaction tx = graph.beginTx()) {

            Map<String, Object> params = Maps.newHashMap();
            params.put("checkedHolograms", true);

            String query = "MATCH (s:" + StructureNode.LABEL + ") "
                    + "WHERE NOT s." + StructureNode.CHECKED_HOLOGRAM_PROPERTY + " = {checkedHolograms} "
                    + "RETURN s";

            Result r = graph.execute(query, params);

            while (r.hasNext()) {
                Map<String, Object> map = r.next();
                for (Object o : map.values()) {
                    Node sn = (Node) o;
                    StructureNode structure = new StructureNode(sn);
                    structures.add(structure);
                }
            }
            tx.success();
        }
        try (Transaction tx = graph.beginTx()) {

            for (StructureNode structureNode : structures) {
                World w = SettlerCraft.getInstance().getWorld(structureNode.getWorldUUID());
                if (w != null) {
                    Vector position = structureNode.translateRelativeLocation(Vector.ZERO.add(0, 2, 0));
                    createWithoutTransactionHologram(plugin.getName(), w, position, new Structure(structureNode));
                }
            }
            tx.success();
        }
    }

    private Hologram createHologramForStructure(String plugin, World world, Vector position, Structure structure) {
        Hologram h;
        try (Transaction tx = graph.beginTx()) {
            h = createWithoutTransactionHologram(plugin, world, position, structure);
            tx.success();
        }
        StructureHologramManager.getInstance().registerStructureHologram(structure, h);

        return h;
    }

    private Hologram createWithoutTransactionHologram(String plugin, World world, Vector position, Structure structure) {
        structureHologramRepository.addHologram(new StructureNode(structure.getNode()), structure.getRelativePosition(position));
        return hologramsProvider.createHologram(plugin, world, position);
    }

    private void invalidate() {
        try (Transaction tx = graph.beginTx()) {
            String query = "MATCH (h:Hologram)<-[r:" + StructureHologramNode.RELATION_HAS_HOLOGRAM.name() + "]-(:" + StructureHologramNode.LABEL.name() + ") "
                    + "WHERE r is null "
                    + "RETURN h";

            Result r = graph.execute(query);
            while (r.hasNext()) {
                Map<String, Object> map = r.next();
                for (Object o : map.values()) {
                    Node n = (Node) o;
                    for (Relationship rel : n.getRelationships()) {
                        rel.delete();
                    }
                    n.delete();
                }
            }

            tx.success();
        }
    }

    /**
     * Removes all active holograms
     */
    public void shutdown() {
        for (List<Hologram> hl : holograms.values()) {
            for (Hologram h : hl) {
                h.delete();
            }
        }
    }

}
