/*
 * Copyright (C) 2016 Chingo
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
package com.chingo247.structureapi.construction;

import com.chingo247.settlercraft.core.SettlerCraft;
import com.chingo247.settlercraft.core.concurrent.KeyPool;
import com.chingo247.structureapi.StructureAPI;
import com.chingo247.structureapi.construction.contract.Contract;
import com.chingo247.structureapi.exeption.StructureException;
import com.chingo247.structureapi.model.structure.ConstructionStatus;
import com.chingo247.structureapi.model.structure.Structure;
import com.chingo247.structureapi.model.structure.StructureNode;
import com.chingo247.structureapi.model.structure.StructureRepository;
import com.chingo247.structureapi.placement.options.PlaceOptions;
import com.chingo247.structureapi.watchers.PhysicsWatch;
import com.chingo247.xplatform.core.APlatform;
import com.chingo247.xplatform.core.IColors;
import com.chingo247.xplatform.core.ICommandSender;
import com.chingo247.xplatform.core.IPlayer;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.World;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.primesoft.asyncworldedit.worldedit.AsyncEditSession;

/**
 * Contractor, like in someone who takes on contracts to build them for you.
 * Once a contract is submitted it will be executed.
 *
 * @author Chingo
 */
public class Contractor implements IContractor {

    /**
     * The Logger.
     */
    private static final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(Contractor.class.getName());
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
    private final StructureRepository structureRepository;
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
     * Private Constructor.
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

    private StructureEntry getOrCreateEntry(Structure structure, Contract plan) {
        synchronized (entryMutex) {
            StructureEntry entry = entries.get(structure.getId());
            if (entry == null) {
                entry = new StructureEntry(this, structure, plan);
                entries.put(structure.getId(), entry);
            }
            return entry;
        }
    }

    private StructureEntry getEntry(Structure structure) {
        synchronized (entryMutex) {
            return entries.get(structure.getId());
        }
    }
    
    private void recursiveAdd(StructureNode parent, List<StructureNode> holder) {
        holder.add(parent);
        for(StructureNode s : parent.getSubstructures()) {
            recursiveAdd(s, holder);
        }
    }

    /**
     * Submits a contract for execution
     *
     * @param contract
     */
    @Override
    public void submit(final Structure structure, final Contract constract) {
        final APlatform platform = StructureAPI.getInstance().getPlatform();
        final IColors colors = platform.getChatColors();
        final IPlayer player = constract.getPlayer() != null ? platform.getPlayer(constract.getPlayer()) : null;
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
        
        constract.setPlayer(playerOrRandomUUID);
       
        // Set default editsession if null
        if (constract.getEditSessionFactory() == null) {
            // Set the editsession if the editsession was null
            constract.setEditSessionFactory(new DefaultStructureEditSessionFactory());
        }

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
                            sender.sendMessage(colors.red() + "[StructureCraft]: An error occurred... See console");
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
                                Map<Long, List<CuboidRegion>> substructureMap = Maps.newHashMap();
                                Transaction tx = null;
                                try {

                                    try {
                                        tx = graph.beginTx();

                                        StructureNode structureNode = structureRepository.findById(structure.getId());


                                        structures = Lists.newArrayList();
                                        
                                        List<StructureNode> nodes = Lists.newArrayList();
                                        recursiveAdd(structureNode, nodes);
                                        
                                        if(constract.isReversed()) {
                                            Collections.reverse(nodes);
                                        }
                                        
                                        
                                        
                                        
                                        for (StructureNode sn : nodes) {
                                            
                                            
                                            
                                            // ASSURE THIS WON'T HAPPEN
                                            if(sn.getStatus() == ConstructionStatus.REMOVED) {
                                                continue;
                                            }
                                            
                                            structures.add(new Structure(sn));
                                            sn.setStatus(ConstructionStatus.QUEUED);
                                            
                                            if(sn.hasSubstructures()) {
                                                List<CuboidRegion> substructures = new ArrayList<>();
                                                
                                                
                                                for(StructureNode sub : sn.getSubstructures()) {
                                                    substructures.add(sub.getCuboidRegion());
                                                }
                                                substructureMap.put(sn.getId(), substructures);
                                            }
                                            
                                            
                                        }

                                        tx.success();
                                    } catch (Exception ex) {
                                        if (tx != null) {
                                            tx.failure();
                                        }
                                        if (player != null) {
                                            sender.sendMessage(colors.red() + "[StructureAPI]: An error occurred... See console");
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

                                        PhysicsWatch watcher = StructureAPI.getInstance().getPhysicsWatcher();
                                        StructureEntry startEntry = null;
                                        if (constract.isRecursive()) {
                                            StructureEntry prevEntry = null;
                                            try {

                                                for (Structure s : structures) {
                                                    StructureEntry currentEntry = getOrCreateEntry(s, constract);
                                                    watcher.register(structure);

//                                                    constract.registerListeners(currentEntry);
                                                    if (startEntry == null) {
                                                        startEntry = currentEntry;
                                                    }
                                                    
                                                    PlaceOptions p = constract.getPlaceOptionsFactory().makeOptions(structure);
                                                    
                                                    List<CuboidRegion> subregionsToIgnore = substructureMap.get(s.getId());
                                                    if(subregionsToIgnore != null) {
                                                        p.addIgnore(subregionsToIgnore);
                                                    }
                                                    
//                                                    constract.getAssigner().assignTasks(editSession, playerOrRandomUUID, currentEntry);
                                                    constract.apply(currentEntry, p);

                                                    if (prevEntry != null) {
                                                        prevEntry.setNextEntry(currentEntry);
                                                    }
                                                    prevEntry = currentEntry;
                                                }
                                            } catch (StructureException ex) {
                                                startEntry = null;
                                                remove(structures);
                                                sender.sendMessage(ex.getMessage());
                                            } catch (Exception ex) {
                                                startEntry = null;
                                                if (player != null) {
                                                    sender.sendMessage(colors.red() + "[StructureAPI]: An error occurred... See console");
                                                }
                                                remove(structures); // Cleanup entries
                                                Logger.getLogger(Contractor.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                                            }
                                        } else {
                                            StructureEntry entry = getOrCreateEntry(structure, constract);
//                                            constract.registerListeners(entry);
                                            watcher.register(structure);
                                            PlaceOptions p = constract.getPlaceOptionsFactory().makeOptions(structure);
                                                    
                                            try {
                                                constract.apply(entry, p);
                                                startEntry = entry;
                                            } catch (StructureException ex) {
                                                startEntry = null;
                                                sender.sendMessage(ex.getMessage());
                                                remove(entry.getStructure().getId());
                                            } catch (Exception ex) {
                                                startEntry = null;
                                                remove(entry.getStructure().getId());
                                                Logger.getLogger(Contractor.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                                            }
                                        }

                                        if (startEntry != null) {
                                            startEntry.proceed();
                                        }

                                    }
                                } catch (Exception ex) {
                                    if (player != null) {
                                        player.sendMessage(colors.red() + "[StructureAPI]: An error occurred... see console");
                                    }
                                    LOG.log(Level.SEVERE, ex.getMessage(), ex);
                                }
                            }

                        });

                    }
                } catch (Exception ex) {
                    LOG.log(Level.SEVERE, ex.getMessage(), ex);
                }

            }
        });
    }

    private void remove(Iterable<? extends Structure> structures) {
        synchronized (entryMutex) {
            for (Structure structure : structures) {
                entries.remove(structure.getId());
            }
        }
    }

    private void remove(long id) {
        synchronized (entryMutex) {
            PhysicsWatch watch = StructureAPI.getInstance().getPhysicsWatcher();
            StructureEntry entry = entries.get(id);
            watch.unregister(entry.getStructure());
            entries.remove(id);
        }
    }

    @Override
    public void remove(StructureEntry entry) {
        remove(entry.getStructure().getId());
    }

    @Override
    public void purge(final Structure structure) {
        structurePool.execute(structure.getId(), new Runnable() {

            @Override
            public void run() {
                StructureEntry entry = getEntry(structure);
                if(entry != null) {
                    entry.stop();
                }
            }
        });

    }

}
