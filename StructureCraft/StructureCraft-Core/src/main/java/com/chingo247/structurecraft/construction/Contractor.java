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
package com.chingo247.structurecraft.construction;

import com.chingo247.settlercraft.core.SettlerCraft;
import com.chingo247.settlercraft.core.concurrent.KeyPool;
import com.chingo247.structurecraft.IStructureAPI;
import com.chingo247.structurecraft.StructureAPI;
import com.chingo247.structurecraft.construction.awe.AWETaskAssigner;
import com.chingo247.structurecraft.construction.actions.Build;
import com.chingo247.structurecraft.construction.actions.Construction;
import com.chingo247.structurecraft.construction.actions.Demolish;
import com.chingo247.structurecraft.exeption.StructureException;
import com.chingo247.structurecraft.model.RelTypes;
import com.chingo247.structurecraft.model.structure.IStructure;
import com.chingo247.structurecraft.model.structure.IStructureRepository;
import com.chingo247.structurecraft.model.structure.Structure;
import com.chingo247.structurecraft.model.structure.StructureNode;
import com.chingo247.structurecraft.model.structure.StructureRepository;
import com.chingo247.xplatform.core.APlatform;
import com.chingo247.xplatform.core.IColors;
import com.chingo247.xplatform.core.ICommandSender;
import com.chingo247.xplatform.core.IPlayer;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.world.World;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.primesoft.asyncworldedit.worldedit.AsyncEditSession;
import com.chingo247.structurecraft.construction.actions.Rollback;
import com.chingo247.structurecraft.construction.save.schematic.SchematicSavingAssigner;
import com.chingo247.structurecraft.exeption.StructurePlanException;
import com.chingo247.structurecraft.placement.IPlacement;
import com.chingo247.structurecraft.placement.block.IBlockPlacement;
import com.chingo247.structurecraft.construction.actions.IConstruction;

/**
 *
 * @author Chingo
 */
public class Contractor implements IContractor {

    /**
     * The Logger.
     */
    private static final Logger LOG = Logger.getLogger(Contractor.class.getName());
    /**
     * The console UUID, used for non-players
     */
    private static final UUID CONSOLE = UUID.randomUUID();
    /**
     * The singleton instance
     */
    private static Contractor instance;
    /**
     * The mutex object for synchronisation
     */
    private final Object entryMutex = new Object();
    /**
     * The StructureRepository for database operations
     */
    private final IStructureRepository structureRepository;
    /**
     * The structure entries
     */
    private Map<Long, StructureEntry> entries;
    /**
     * The ThreadPool used to queue tasks for structures
     */
    private KeyPool<Long> structurePool;
    /**
     * The ExecutorService
     */
    private ExecutorService es;

    /**
     * Constructor.
     */
    private Contractor() {
        this.es = StructureAPI.getInstance().getExecutor();
        this.structurePool = new KeyPool<>(es);
        this.structureRepository = new StructureRepository(SettlerCraft.getInstance().getNeo4j());
        this.entries = Maps.newHashMap();
    }

    public static Contractor getInstance() {
        if (instance == null) {
            instance = new Contractor();
        }
        return instance;
    }

    StructureEntry getOrCreateEntry(IStructure structure, IConstruction plan) {
        synchronized (entryMutex) {
            StructureEntry entry = entries.get(structure.getId());
            if (entry == null) {
                entry = new StructureEntry(this, structure, plan);
                entries.put(structure.getId(), entry);
            }
            return entry;
        }
    }

    StructureEntry getEntry(IStructure structure) {
        synchronized (entryMutex) {
            return entries.get(structure.getId());
        }
    }

    void remove(Iterable<? extends IStructure> structures) {
        synchronized (entryMutex) {
            for (IStructure structure : structures) {
                entries.remove(structure.getId());
            }
        }
    }

    void remove(long id) {
        synchronized (entryMutex) {
            entries.remove(id);
        }
    }

    @Override
    public void execute(final Construction plan) {
        final IStructure structure = plan.getStructure();
        final APlatform platform = StructureAPI.getInstance().getPlatform();
        final IColors colors = platform.getChatColors();
        final IPlayer player = plan.getPlayer() != null ? platform.getPlayer(plan.getPlayer()) : null;
        final UUID playerOrRandomUUID;
        final ICommandSender sender;

        // Set the person who should receive a message if anything fails
        if (player != null) {
            playerOrRandomUUID = player.getUniqueId();
            sender = player;
        } else {
            playerOrRandomUUID = CONSOLE;
            sender = platform.getConsole();
        }

        World world = SettlerCraft
                .getInstance()
                .getWorld(
                        structure.getWorldName()
                );
        Player ply = SettlerCraft.getInstance().getPlayer(playerOrRandomUUID);
        final AsyncEditSession editSession = plan.getEditSession() != null ? plan.getEditSession()
                : (AsyncEditSession) (ply != null ? StructureAPI.getInstance().getSessionFactory().getEditSession(world, -1, ply)
                        : StructureAPI.getInstance().getSessionFactory().getEditSession(world, -1));

        es.execute(new Runnable() {

            @Override
            public void run() {
                try {

                    // Get lock on root node
                    final GraphDatabaseService graph = StructureAPI.getInstance().getGraphDatabase();
                    Transaction tx = null;
                    Long lockId = null;
                    try {
                        tx = graph.beginTx();
                        StructureNode structureNode = new StructureNode(structure.getUnderlyingNode());
                        StructureNode rootNode = structureNode.getRoot();
                        lockId = rootNode.getId();
                    } catch (Exception ex) {
                        if (tx != null) {
                            tx.failure();
                        }
                        if (player != null) {
                            sender.sendMessage(colors.red() + "[StructureCraft]: An error ocurrerd... See console");
                        }
                        LOG.log(Level.SEVERE, ex.getMessage(), ex);
                    } finally {
                        if (tx != null) {
                            tx.close();
                        }
                    }

                    if (lockId != null) {
                        structurePool.execute(lockId, new Runnable() {

                            @Override
                            public void run() {
                                List<Structure> structures = null;
                                Transaction tx = null;
                                try {

                                    try {
                                        tx = graph.beginTx();

                                        StructureNode structureNode = structureRepository.findById(structure.getId());

                                        // Traverse the structures from the database/graph
                                        TraversalDescription traversal = graph.traversalDescription()
                                                .relationships(RelTypes.SUBSTRUCTURE_OF, Direction.INCOMING)
                                                .breadthFirst();

                                        if (plan.isReversed()) {
                                            traversal = traversal.reverse();
                                        }

                                        Iterable<Node> nodes = traversal
                                                .traverse(structureNode.getNode())
                                                .nodes();

                                        structures = Lists.newArrayList();
                                        for (Node sn : nodes) {
                                            structures.add(new Structure(sn));
                                        }

                                        tx.success();
                                    } catch (Exception ex) {
                                        if (tx != null) {
                                            tx.failure();
                                        }
                                        if (player != null) {
                                            sender.sendMessage(colors.red() + "[StructureAPI]: An error ocurrerd... See console");
                                        }
                                        LOG.log(Level.SEVERE, ex.getMessage(), ex);
                                        structures = null;
                                    } finally {
                                        if (tx != null) {
                                            tx.close();
                                        }
                                    }

                                    if (structures != null) {
                                        // STOP ALL
                                        for (Structure s : structures) {
                                            StructureEntry entry = getEntry(s);
                                            if (entry != null) {
                                                entry.purge();
                                            }
                                        }

                                        IStructureEntry startEntry = null;
                                        if (plan.isRecursive()) {
                                            StructureEntry prevEntry = null;
                                            try {
                                                for (Structure s : structures) {
                                                    StructureEntry currentEntry = getOrCreateEntry(s, plan);

                                                    plan.register(currentEntry);

                                                    if (startEntry == null) {
                                                        startEntry = currentEntry;
                                                    }
                                                    plan.getAssigner().assignTasks(editSession, playerOrRandomUUID, currentEntry);
                                                    if (prevEntry != null) {
                                                        prevEntry.setNextEntry(currentEntry);
                                                    }
                                                    prevEntry = currentEntry;
                                                }
                                            } catch (StructureException ex) {
                                                startEntry = null;
                                                remove(structures);
                                                sender.sendMessage(ex.getMessage());
                                            } catch (IOException ex) {
                                                startEntry = null;
                                                if (player != null) {
                                                    sender.sendMessage("[StructureAPI]: An error occured... See console");
                                                }
                                                remove(structures); // Cleanup entries
                                                Logger.getLogger(Contractor.class.getName()).log(Level.SEVERE, null, ex);
                                            }
                                        } else {
                                            IStructureEntry entry = getOrCreateEntry(structure, plan);
                                            plan.register(entry);

                                            ITaskAssigner assigner = plan.getAssigner();
                                            try {
                                                assigner.assignTasks(editSession, playerOrRandomUUID, entry);
                                                startEntry = entry;
                                            } catch (StructureException ex) {
                                                startEntry = null;
                                                sender.sendMessage(ex.getMessage());
                                                remove(entry.getStructure().getId());
                                            } catch (IOException ex) {
                                                startEntry = null;
                                                remove(entry.getStructure().getId());
                                                Logger.getLogger(Contractor.class.getName()).log(Level.SEVERE, null, ex);
                                            }
                                        }

                                        if (startEntry != null) {
                                            startEntry.proceed();
                                        }

                                    }
                                } catch (Exception ex) {
                                    if (player != null) {
                                        player.sendMessage(colors.red() + "[StructureAPI]: An error occured... see console");
                                    }
                                    LOG.log(Level.SEVERE, ex.getMessage(), ex);
                                }
                            }

                        });
                    }
                } catch (Exception ex) {
                    if (player != null) {
                        player.sendMessage(colors.red() + "[StructureAPI]: An error occured... see console");
                    }
                    LOG.log(Level.SEVERE, ex.getMessage(), ex);
                }
            }
        });
    }

    @Override
    public void purge(IStructure structure) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void remove(IStructureEntry entry) {
        remove(entry.getStructure().getId());
    }

    /**
     * Checks if a structure supports construction
     * @param structure The structure
     * @return True if it supports construction
     * @throws StructurePlanException May throw exception if the reading of the StructurePlan fails
     */
    @Override
    public boolean supportsConstruction(IStructure structure) throws StructurePlanException  {
        IPlacement placement = structure.getStructurePlan().getPlacement();
        return (placement instanceof IBlockPlacement);
    }

    @Override
    public IConstruction newRollbackPlan(IStructure structure) throws StructureException, Exception {
        ITaskAssigner assigner = new AWETaskAssigner();
        return new Rollback(this, structure, assigner);
    }

    @Override
    public IConstruction newBuildPlan(IStructure structure) throws StructureException, StructurePlanException {
        if (!supportsConstruction(structure)) {
            throw new StructureException("Structure doesn't support build");
        }

        ITaskAssigner assigner = new AWETaskAssigner();
        return new Build(this, structure, assigner);
    }

    @Override
    public IConstruction newDemolitionPlan(IStructure structure) throws StructureException, StructurePlanException {
        if (!supportsConstruction(structure)) {
            throw new StructureException("Structure doesn't support demolishment");
        }
        ITaskAssigner assigner = new AWETaskAssigner();
        return new Demolish(this, structure, assigner);
    }

    @Override
    public IConstruction newSaveBuildPlan(IStructure structure) throws StructureException, StructurePlanException {
        if (!supportsConstruction(structure)) {
            throw new StructureException("Structure doesn't support build");
        }

        ITaskAssigner assigner = new SchematicSavingAssigner();
        return new Build(this, structure, assigner);
    }

    @Override
    public IConstruction newSaveDemolitionPlan(IStructure structure) throws StructureException, StructurePlanException {
        if (!supportsConstruction(structure)) {
            throw new StructureException("Structure doesn't support demolishment");
        }
        ITaskAssigner assigner = new SchematicSavingAssigner();
        return new Demolish(this, structure, assigner);
    }

}
