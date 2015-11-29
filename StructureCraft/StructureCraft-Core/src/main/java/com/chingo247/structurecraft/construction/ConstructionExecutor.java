/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.construction;

import com.chingo247.structurecraft.construction.options.IPlaceOptionsAssigner;
import com.chingo247.structurecraft.construction.assigner.awe.AWEAssignerFactory;
import com.chingo247.structurecraft.construction.assigner.ITaskAssigner;
import com.chingo247.structurecraft.construction.assigner.IAssignerFactory;
import com.chingo247.settlercraft.core.SettlerCraft;
import com.chingo247.settlercraft.core.concurrent.KeyPool;
import com.chingo247.structurecraft.IStructureAPI;
import com.chingo247.structurecraft.construction.options.PlaceOptions;
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

/**
 *
 * @author Chingo
 */
public class ConstructionExecutor implements IConstructionExecutor {

    private static final Logger LOG = Logger.getLogger(ConstructionExecutor.class.getName());
    private static final UUID CONSOLE = UUID.randomUUID();
    private final IStructureAPI structureAPI;
    private final Object entryMutex = new Object();
    private final IStructureRepository structureRepository;
    private Map<Long, ConstructionEntry> entries;
    private KeyPool<Long> structurePool;
    private ExecutorService es;
    private IConstructionPlanFactory planFactory;
    private IAssignerFactory assFactory;

    public ConstructionExecutor(IStructureAPI structureAPI, ExecutorService es) {
        this.structurePool = new KeyPool<>(es);
        this.es = es;
        this.structureAPI = structureAPI;
        this.assFactory = new AWEAssignerFactory(structureAPI);
        this.planFactory = new ConstructionPlanFactory(structureAPI, this);
        this.structureRepository = new StructureRepository(SettlerCraft.getInstance().getNeo4j());
        this.entries = Maps.newHashMap();
    }

    @Override
    public IAssignerFactory getAssignerFactory() {
        return assFactory;
    }

    ConstructionEntry getOrCreateEntry(IStructure structure) {
        synchronized (entryMutex) {
            ConstructionEntry entry = entries.get(structure.getId());
            if (entry == null) {
                entry = new ConstructionEntry(this, structure);
                entries.put(structure.getId(), entry);
            }
            return entry;
        }
    }

    ConstructionEntry getEntry(IStructure structure) {
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
    public void execute(final IConstructionPlan plan) {
        final IStructure structure = plan.getStructure();
        System.out.println("plan structure: " + plan.getStructure());
        System.out.println("plan structure id: " + plan.getStructure().getId());

        final APlatform platform = structureAPI.getPlatform();
        final IColors colors = platform.getChatColors();
        final IPlayer player = plan.getPlayer() != null ? platform.getPlayer(plan.getPlayer()) : null;
        final UUID playerOrRandomUUID;
        final ICommandSender sender;
        final IPlaceOptionsAssigner placeOptionsAssigner = plan.getOptionsAssigner();

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
                : (AsyncEditSession) (ply != null ? structureAPI.getSessionFactory().getEditSession(world, -1, ply)
                        : structureAPI.getSessionFactory().getEditSession(world, -1));

        es.execute(new Runnable() {

            @Override
            public void run() {
                try {

                    // Get lock on root node
                    final GraphDatabaseService graph = structureAPI.getGraphDatabase();
                    Transaction tx = null;
                    Long lockId = null;
                    try {
                        tx = graph.beginTx();
                        StructureNode structureNode = new StructureNode(structure.getUnderlyingNode());
                        System.out.println("Structure: " + structureNode);
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
                                            ConstructionEntry entry = getEntry(s);
                                            if (entry != null) {
                                                entry.purge();
                                            }
                                        }

                                        IConstructionEntry startEntry = null;
                                        if (plan.isRecursive()) {
                                            ConstructionEntry prevEntry = null;
                                            try {
                                                for (Structure s : structures) {
                                                    ConstructionEntry currentEntry = getOrCreateEntry(s);
                                                    if (startEntry == null) {
                                                        startEntry = currentEntry;
                                                    }
                                                    plan.getAssigner().assignTasks(editSession, playerOrRandomUUID, currentEntry, placeOptionsAssigner);
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
                                                Logger.getLogger(ConstructionExecutor.class.getName()).log(Level.SEVERE, null, ex);
                                            }
                                        } else {
                                            IConstructionEntry entry = getOrCreateEntry(structure);
                                            ITaskAssigner assigner = plan.getAssigner();
                                            try {
                                                assigner.assignTasks(editSession, playerOrRandomUUID, entry, placeOptionsAssigner);
                                                startEntry = entry;
                                            } catch (StructureException ex) {
                                                startEntry = null;
                                                sender.sendMessage(ex.getMessage());
                                                remove(entry.getStructure().getId());
                                            } catch (IOException ex) {
                                                startEntry = null;
                                                remove(entry.getStructure().getId());
                                                Logger.getLogger(ConstructionExecutor.class.getName()).log(Level.SEVERE, null, ex);
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
    public void remove(IConstructionEntry entry) {
        remove(entry.getStructure().getId());
    }

    @Override
    public IConstructionPlanFactory getConstructionPlanFactory() {
        return planFactory;
    }

}
