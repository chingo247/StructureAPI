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
import com.chingo247.settlercraft.core.event.EventManager;
import com.chingo247.settlercraft.core.event.async.AsyncEventManager;
import com.chingo247.settlercraft.core.concurrent.KeyPool;
import com.chingo247.structurecraft.StructureAPI;
import com.chingo247.structurecraft.construction.event.StructureTaskCancelledEvent;
import com.chingo247.structurecraft.construction.event.StructureTaskCompleteEvent;
import com.chingo247.structurecraft.construction.event.StructureTaskStartEvent;
import com.chingo247.structurecraft.construction.options.BuildOptions;
import com.chingo247.structurecraft.construction.options.DemolitionOptions;
import com.chingo247.structurecraft.event.StructureStateChangeEvent;
import com.chingo247.structurecraft.event.async.StructureJobAddedEvent;
import com.chingo247.structurecraft.model.structure.ConstructionStatus;
import com.chingo247.structurecraft.model.structure.IStructureRepository;
import com.chingo247.structurecraft.model.structure.Structure;
import com.chingo247.structurecraft.model.structure.StructureNode;
import com.chingo247.structurecraft.model.structure.StructureRepository;
import com.chingo247.structurecraft.construction.options.Options;
import com.chingo247.structurecraft.model.RelTypes;
import com.chingo247.structurecraft.model.owner.Ownership;
import com.chingo247.xplatform.core.APlatform;
import com.chingo247.xplatform.core.IColors;
import com.chingo247.xplatform.core.IPlayer;
import com.google.common.collect.Lists;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.sk89q.worldedit.entity.Player;
import java.util.ArrayList;
import java.util.HashMap;
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
public class ConstructionManager implements IConstructionManager {

    private Map<Long, ConstructionEntry> entries;
    private final IStructureTaskFactory taskFactory;
    private ITaskAssigner buildTaskAssigner;
    private ITaskAssigner demolitionTaskAssigner;
    private static ConstructionManager instance;

    private final GraphDatabaseService graph;
    private final KeyPool<Long> structurePool;
    private final KeyPool<Long> eventPool;
    private final ExecutorService executor;
    private final IStructureRepository structureRepository;
    private final APlatform platform;
    private final IColors colors;

    private final Object entriesMutex = new Object();

    private ConstructionManager() {
        this.demolitionTaskAssigner = new DefaultDemolitionTaskAssigner();
        this.buildTaskAssigner = new DefaultBuildTaskAssigner();
        this.entries = new HashMap<>();
        this.taskFactory = new DefaultStructureTaskFactory();
        this.graph = SettlerCraft.getInstance().getNeo4j();
        this.executor = SettlerCraft.getInstance().getExecutor();
        this.structurePool = new KeyPool<>(executor);
        this.structureRepository = new StructureRepository(graph);
        this.platform = SettlerCraft.getInstance().getPlatform();
        this.colors = platform.getChatColors();
        this.eventPool = new KeyPool<>(executor);
    }

    /**
     * Gets the {@link IConstructionManager} instance
     *
     * @return The instance
     */
    public static IConstructionManager getInstance() {
        if (instance == null) {
            instance = new ConstructionManager();
            AsyncEventManager.getInstance().register(instance);
        }
        return instance;
    }

    /**
     * Gets the taskfactory
     *
     * @return The taskfactory
     */
    @Override
    public IStructureTaskFactory getDefaultTaskFactory() {
        return taskFactory;
    }

    @Override
    public void remove(Structure structure) {
        synchronized (entriesMutex) {
            entries.remove(structure.getId());
        }
    }

    @Override
    public void remove(ConstructionEntry entry) {
        synchronized (entriesMutex) {
            entries.remove(entry.getStructure().getId());
        }
    }

    @Override
    public ConstructionEntry getEntry(Structure structure) {
        synchronized (entriesMutex) {
            long id = structure.getId();
            ConstructionEntry entry = entries.get(id);
            if (entry == null) {
                entry = new ConstructionEntry(structure);
                entries.put(id, entry);
            }
            return entry;
        }
    }

    @Override
    public void stop(Structure structure) throws ConstructionException {
        ConstructionEntry entry = getEntry(structure);
        stop(entry);
    }

    @Override
    public void stop(ConstructionEntry entry) throws ConstructionException {
        Structure structure = entry.getStructure();
        if (structure.getConstructionStatus() == ConstructionStatus.REMOVED) {
            throw new ConstructionException("Can't stop a removed structure...");
        }
        if (entry == null) {
            throw new ConstructionException("Unable to stop structure #" + structure.getId() + ": No active tasks,,,");
        }
        entry.purge();
        remove(entry);
    }

    @Override
    public void perform(AsyncEditSession session, UUID player, Structure structure, ITaskAssigner assigner, Options options) throws ConstructionException {
        ConstructionEntry entry = getEntry(structure);
        perform(session, player, entry, assigner, options);
    }

    @Override
    public void perform(final AsyncEditSession session, final UUID player, final ConstructionEntry entry, final ITaskAssigner assigner, final Options options) throws ConstructionException {
        perform(session, player, entry, assigner, options, true);
    }

    @Override
    public void perform(final AsyncEditSession session, final UUID player, final Structure structure, final ITaskAssigner assigner, final Options options, final boolean includeSubstructures) throws ConstructionException {
        ConstructionEntry entry = getEntry(structure);
        perform(session, player, entry, assigner, options, includeSubstructures);
    }

    @Override
    public void perform(final AsyncEditSession session, final UUID player, final ConstructionEntry entry, final ITaskAssigner assigner, final Options options, final boolean includeSubstructures) throws ConstructionException {
        if (entry.getStructure().getConstructionStatus() == ConstructionStatus.REMOVED) {
            throw new ConstructionException("Can't perform tasks on a removed structure");
        }

        // Perform async
        executor.execute(new Runnable() {

            @Override
            public void run() {
                try {
                    System.out.println("[ConstructionManager]: run build async 1");

                    // Get the id of the root structure, the structure that has no parent
                    // All actions will be queued on the root
                    Long rootId;
                    try (Transaction tx = graph.beginTx()) {
                        StructureNode sn = new StructureNode(entry.getStructure().getNode());
                        StructureNode root = sn.getRoot();
                        rootId = root.getId();
                        tx.success();
                    }

                    // Make sure actions are performed in a queue wise
                    // Therefore we set a lock on the root structure
                    structurePool.execute(rootId, new Runnable() {

                        @Override
                        public void run() {
                            Transaction tx = null;
                            try {
                                tx = graph.beginTx();
                                System.out.println("[ConstructionManager]: run build async 2");

                                // Traverse the structures from the database/graph
                                TraversalDescription traversal = graph.traversalDescription()
                                        .relationships(RelTypes.SUBSTRUCTURE_OF, Direction.INCOMING);

                                if (options != null && options.isTraversingReversed()) {
                                    traversal = traversal.reverse();
                                }

                                if (options == null || options.getTraveral() == StructureTraversal.BREADTH_FIRST) {
                                    traversal = traversal.breadthFirst();
                                } else {
                                    traversal = traversal.depthFirst();
                                }

                                Iterable<Node> nodes = traversal
                                        .traverse(entry.getStructure().getNode())
                                        .nodes();

                                List<Structure> structures = Lists.newArrayList();
                                for (Node structureNode : nodes) {
                                    structures.add(new Structure(structureNode));
                                }

                                if (structures != null) {

                                    System.out.println("[ConstructionManager]: Purging existing tasks");
                                    // Purge existing tasks from entries
                                    for (Structure s : structures) {
                                        System.out.println("[ConstructionManager]: Stopping " + s.getId());
                                        stop(s);
                                    }

                                    System.out.println("[ConstructionManager]: Has nodes ? " + nodes.iterator().hasNext());
                                    System.out.println("[ConstructionManager]: Asigning new tasks");

                                    // Build the links between the entries, similar to the linkedlist
                                    // Where each entry knows it's next and previous entry
                                    ConstructionEntry prevEntry = null;

                                    ConstructionEntry firstEntry = null;
                                    if (includeSubstructures) {
                                        for (Structure s : structures) {
                                            System.out.println("[ConstructionManager]: Asigning to " + s.getId() + " with " + assigner.getClass().getSimpleName());
                                            ConstructionEntry currentEntry = getEntry(s);
                                            if (firstEntry == null) {
                                                firstEntry = currentEntry;
                                            }
                                            assigner.assignTasks(session, player, currentEntry, options);

                                            if (prevEntry != null) {
                                                prevEntry.setNextEntry(currentEntry);
                                            }
                                        }
                                    } else {
                                        ConstructionEntry currentEntry = getEntry(entry.getStructure());
                                        assigner.assignTasks(session, player, currentEntry, options);
                                        firstEntry = currentEntry;
                                    }

                                    // Fire first structure entry
                                    System.out.println("[ConstructionManager]: Starting tasks!");
                                    if (firstEntry != null) {
                                        firstEntry.proceed();
                                    }

                                }
                                tx.success();
                            } catch (ConstructionException ex) {
                                if (tx != null) {
                                    tx.failure();
                                }
                                Player ply = SettlerCraft.getInstance().getPlayer(player);
                                if (ply != null) {
                                    ply.printError(ex.getMessage());
                                }
                            } catch (Exception ex) {
                                if (tx != null) {
                                    tx.failure();
                                }
                                // Catch everything else or it will disappear in the abyss
                                Logger.getLogger(StructureAPI.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                            } finally {
                                if (tx != null) {
                                    tx.close();
                                }
                            }
                        }
                    });

                } catch (Exception ex) {
                    // Catch everything else or it will disappear in the abyss
                    Logger.getLogger(StructureAPI.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                }
            }
        });
    }

    @Subscribe
    @AllowConcurrentEvents
    public void onJobAdded(StructureJobAddedEvent addedEvent) {
        try {
            final long structureId = addedEvent.getStructure().getId();
            final UUID uuid = addedEvent.getPlayerEntry().getUUID();

            eventPool.execute(structureId, new Runnable() {

                @Override
                public void run() {
                    try {
                        Structure structure = null;
                        try (Transaction tx = graph.beginTx()) {
                            StructureNode sn = structureRepository.findById(structureId);
                            if (sn != null) {
                                sn.setStatus(ConstructionStatus.QUEUED);
                                structure = new Structure(sn);
                            }
                            tx.success();
                        }

                        if (structure != null) {
                            EventManager.getInstance().getEventBus().post(new StructureStateChangeEvent(structure));
                            if (uuid != null) {
                                IPlayer player = platform.getServer().getPlayer(uuid);
                                if (player != null) {
                                    String status = getStatusString(ConstructionStatus.QUEUED, ConstructionStatus.QUEUED.name(), structure);
                                    player.sendMessage(status);
                                }
                            }
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(ConstructionManager.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    }
                }
            });
        } catch (Exception ex) {
            Logger.getLogger(ConstructionManager.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    @Subscribe
    @AllowConcurrentEvents
    public void onTaskStartEvent(StructureTaskStartEvent startEvent) {
        try {
            final StructureTask task = startEvent.getTask();
            final Structure structure = task.getConstructionEntry().getStructure();

            eventPool.execute(structure.getId(), new Runnable() {

                @Override
                public void run() {
                    try {
                        ConstructionStatus newStatus = ConstructionStatus.getStatus(task.getAction());
                        updateStatus(newStatus, task.getAction(), structure);
                    } catch (Exception ex) {
                        Logger.getLogger(ConstructionManager.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    }
                }
            });
        } catch (Exception ex) {
            Logger.getLogger(ConstructionManager.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    @Subscribe
    @AllowConcurrentEvents
    public void onTaskCancelled(StructureTaskCancelledEvent cancelledEvent) {
        try {
            final StructureTask task = cancelledEvent.getTask();
            final Structure structure = task.getConstructionEntry().getStructure();

            eventPool.execute(structure.getId(), new Runnable() {

                @Override
                public void run() {
                    try {
                        ConstructionStatus newStatus = ConstructionStatus.STOPPED;
                        updateStatus(newStatus, newStatus.name(), structure);
                    } catch (Exception ex) {
                        Logger.getLogger(ConstructionManager.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    }

                }
            });
        } catch (Exception ex) {
            Logger.getLogger(ConstructionManager.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    @Subscribe
    @AllowConcurrentEvents
    public void onTaskComplete(StructureTaskCompleteEvent taskCompleteEvent) {
        try {
            final StructureTask task = taskCompleteEvent.getTask();
            final ConstructionEntry entry = task.getConstructionEntry();
            final Structure structure = entry.getStructure();

            eventPool.execute(structure.getId(), new Runnable() {

                @Override
                public void run() {
                    try {

                        ConstructionStatus status = ConstructionStatus.getStatus(task.getAction());
                        ConstructionStatus newStatus;
                        switch (status) {
                            case BUILDING:
                            case ROLLING_BACK:
                                newStatus = ConstructionStatus.COMPLETED;
                                break;
                            case DEMOLISHING:
                            case RESTORING:
                                newStatus = ConstructionStatus.REMOVED;
                                break;
                            case CREATING_BACKUP:
                                newStatus = ConstructionStatus.BACKUP_COMPLETE;
                                break;
                            default:
                                if (task.isCancelled() || task.hasFailed()) {
                                    newStatus = ConstructionStatus.ON_HOLD;
                                    break;
                                }
                                return;
                        }

                        updateStatus(newStatus, newStatus.name(), structure);
                    } catch (Exception ex) {
                        Logger.getLogger(ConstructionManager.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    }
                }
            });
        } catch (Exception ex) {
            Logger.getLogger(ConstructionManager.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    private void updateStatus(ConstructionStatus newStatus, String statusToTell, Structure structure) {
        List<IPlayer> owners = new ArrayList<>();

        String status;
        Structure updatedStructure;
        try (Transaction tx = graph.beginTx()) {
            StructureNode sn = new StructureNode(structure.getNode());
            sn.setStatus(newStatus);

            List<Ownership> ownerships = sn.getOwnerDomain().getOwnerships();
            for (Ownership ownership : ownerships) {
                IPlayer ply = platform.getPlayer(ownership.getOwner().getUniqueId());
                if (ply != null) {
                    owners.add(ply);
                }
            }
            updatedStructure = new Structure(structure.getNode());
            status = getStatusString(newStatus, statusToTell.toUpperCase().replaceAll("_", " "), updatedStructure);
            tx.success();
        }

        EventManager.getInstance().getEventBus().post(new StructureStateChangeEvent(updatedStructure));

        // Tell the new status!
        for (IPlayer p : owners) {
            p.sendMessage(status);
        }
    }

    @Override
    public void build(final AsyncEditSession editSession, final UUID player, final Structure structure, final BuildOptions options) throws ConstructionException {
        perform(editSession, player, structure, buildTaskAssigner, options);
    }

    @Override
    public void build(UUID player, Structure structure, BuildOptions options) throws ConstructionException {
        Player ply = SettlerCraft.getInstance().getPlayer(player);
        com.sk89q.worldedit.world.World w = SettlerCraft.getInstance().getWorld(structure.getWorldUUID().getName());
        AsyncEditSession editSession;
        StructureAPI api = (StructureAPI) StructureAPI.getInstance();
        if (ply == null) {
            editSession = (AsyncEditSession) api.getSessionFactory().getEditSession(w, -1);
        } else {
            editSession = (AsyncEditSession) api.getSessionFactory().getEditSession(w, -1, ply);
        }
        build(editSession, player, structure, options);
    }

    @Override
    public void build(Structure structure, BuildOptions options) throws ConstructionException {
        build(UUID.randomUUID(), structure, options);
    }

    @Override
    public void build(Structure structure) throws ConstructionException {
        build(structure, new BuildOptions());
    }

    @Override
    public void demolish(AsyncEditSession editSession, UUID player, Structure structure, DemolitionOptions options) throws ConstructionException {
        perform(editSession, player, structure, demolitionTaskAssigner, options);
    }

    @Override
    public void demolish(UUID player, Structure structure, DemolitionOptions options) throws ConstructionException {
        Player ply = SettlerCraft.getInstance().getPlayer(player);
        com.sk89q.worldedit.world.World w = SettlerCraft.getInstance().getWorld(structure.getWorldUUID().getName());
        AsyncEditSession editSession;
        StructureAPI api = (StructureAPI) StructureAPI.getInstance();
        if (ply == null) {
            editSession = (AsyncEditSession) api.getSessionFactory().getEditSession(w, -1);
        } else {
            editSession = (AsyncEditSession) api.getSessionFactory().getEditSession(w, -1, ply);
        }
        demolish(editSession, player, structure, options);
    }

    @Override
    public void demolish(Structure structure, DemolitionOptions options) throws ConstructionException {
        demolish(UUID.randomUUID(), structure, options);
    }

    @Override
    public void demolish(Structure structure) throws ConstructionException {
        demolish(structure, new DemolitionOptions());
    }

    @Override
    public void stop(Structure structure, boolean force) throws ConstructionException {
        stop(structure);
    }

    private String getStatusString(ConstructionStatus newStatus, String status, Structure structure) {
        String structureInfo = colors.reset() + ": #" + colors.gold() + structure.getId() + colors.blue() + " " + structure.getName();
        String statusString = status.replaceAll("_", " ");
        switch (newStatus) {
            case COMPLETED:
                return colors.green() + statusString + structureInfo;
            case STOPPED:
            case ON_HOLD:
            case REMOVED:
                return colors.red() + statusString + structureInfo;
            default:
                return colors.yellow() + statusString + structureInfo;
        }
    }

}
