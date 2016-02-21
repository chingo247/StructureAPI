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
package com.chingo247.structureapi;

import com.chingo247.structureapi.exeption.StructureAPIException;
import com.chingo247.structureapi.model.structure.ConstructionStatus;
import com.chingo247.structureapi.model.structure.StructureNode;
import com.chingo247.menuapi.menu.CategoryMenu;
import com.chingo247.menuapi.menu.MenuAPI;
import com.chingo247.xplatform.core.APlatform;
import com.chingo247.settlercraft.core.SettlerCraft;
import com.chingo247.settlercraft.core.concurrent.ThreadPoolFactory;
import com.chingo247.settlercraft.core.event.DefaultSubscriberExceptionHandler;
import com.chingo247.settlercraft.core.event.EventDispatcher;
import com.chingo247.settlercraft.core.event.IEventDispatcher;
import com.chingo247.settlercraft.core.exception.SettlerCraftException;
import com.chingo247.structureapi.menu.StructurePlanMenuFactory;
import com.chingo247.structureapi.menu.StructurePlanMenuReader;
import com.chingo247.settlercraft.core.model.world.SCWorldNode;
import com.chingo247.settlercraft.core.persistence.neo4j.Neo4jHelper;
import com.chingo247.structureapi.construction.Contractor;
import com.chingo247.structureapi.plan.IStructurePlan;
import com.chingo247.structureapi.plan.StructurePlanManager;
import com.chingo247.structureapi.event.structure.plan.StructurePlansLoadedEvent;
import com.chingo247.structureapi.event.structure.plan.StructurePlansReloadEvent;
import com.chingo247.structureapi.model.structure.Structure;
import com.chingo247.structureapi.platform.services.AsyncEditSessionFactoryProvider;
import com.chingo247.structureapi.model.zone.ConstructionZoneNode;
import com.chingo247.structureapi.placement.block.SchematicPlacement;
import com.chingo247.structureapi.placing.constructionzone.IConstructionZonePlacerFactory;
import com.chingo247.structureapi.schematic.Schematic;
import com.chingo247.structureapi.schematic.SchematicManager;
import com.chingo247.structureapi.platform.ConfigProvider;
import com.chingo247.structureapi.placing.structure.IStructurePlacerFactory;
import com.chingo247.structureapi.platform.IStructureAPIPlugin;
import com.chingo247.structureapi.exeption.StructureRestrictionException;
import com.chingo247.xplatform.core.IColors;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.Monitor;
import com.sk89q.worldedit.regions.CuboidRegion;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import org.dom4j.DocumentException;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.primesoft.asyncworldedit.worldedit.AsyncEditSessionFactory;
import com.chingo247.structureapi.construction.IContractor;
import com.chingo247.structureapi.updates.StructureAPIModelUpdater;
import com.chingo247.structureapi.watchers.PhysicsWatch;

/**
 *
 * @author Chingo
 */
public class StructureAPI implements IStructureAPI {

    public static final String PLUGIN_NAME = "SettlerCraft";
    public static final String PLANS_DIRECTORY = "plans";
    private static final Logger LOG = Logger.getLogger(StructureAPI.class.getName());

    private final ExecutorService executor;
    private final GraphDatabaseService graph;
    private final Lock loadLock = new ReentrantLock();
    
    private final APlatform platform;
    private final IColors COLORS;
    private final Map<String, Monitor> monitors;
    
    private final Set<StructureRestriction> restrictions;
    private IStructurePlacerFactory structurePlacerFactory;
    private IConstructionZonePlacerFactory constructionZonePlacerFactory;
    private IContractor constructionExecutor;
   
    private IStructureAPIPlugin plugin;
    private ConfigProvider config;
    private StructurePlanMenuFactory planMenuFactory;
    
    private CategoryMenu menuTemplate;
    private boolean isLoadingPlans = false, initialized = false;
    
    private IEventDispatcher eventDispatcher;
    private EventBus eventBus, asyncEventBus;
    
    private AsyncEditSessionFactoryProvider sessionFactoryProvider;
    private IAsyncWorldEditIntegration asyncWorldEditIntegration;

    private static StructureAPI instance;
    
    private final PhysicsWatch physicsWatch;

    private StructureAPI() {
        this.platform = SettlerCraft.getInstance().getPlatform();
        this.graph = SettlerCraft.getInstance().getNeo4j();
        this.executor = new ThreadPoolFactory().newCachedThreadPool(Runtime.getRuntime().availableProcessors(), Runtime.getRuntime().availableProcessors());
        this.monitors = Maps.newHashMap();
        this.COLORS = platform.getChatColors();
        this.restrictions = Sets.newHashSet();
        this.eventDispatcher = new EventDispatcher();
        this.asyncEventBus = new AsyncEventBus(executor, new DefaultSubscriberExceptionHandler());
        this.eventBus = new EventBus(new DefaultSubscriberExceptionHandler());
        this.eventDispatcher.register(eventBus);
        this.eventDispatcher.register(asyncEventBus);
        this.asyncEventBus.register(new StructurePlanManagerHandler());
        this.physicsWatch = new PhysicsWatch();
        setupSchema();
        applyUpdates();

    }


    @Override
    public EventBus getAsyncEventBus() {
        return asyncEventBus;
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    @Override
    public Iterable<StructureRestriction> getRestrictions() {
        return new ArrayList<>(restrictions);
    }

//    @Override
//    public IConstructionZonePlacerFactory getConstructionZonePlacerFactory() {
//        return constructionZonePlacerFactory;
//    }

    @Override
    public GraphDatabaseService getGraphDatabase() {
        return SettlerCraft.getInstance().getNeo4j();
    }

    @Override
    public IEventDispatcher getEventDispatcher() {
        return eventDispatcher;
    }

    @Override
    public IStructurePlacerFactory getStructurePlacerFactory() {
        return structurePlacerFactory;
    }

    public synchronized Monitor getMonitor(String world) {
        Monitor monitor = monitors.get(world);
        if (monitor == null) {
            monitor = new Monitor();
            monitors.put(world, monitor);
        }
        return monitor;
    }

    private void setupSchema() {
        // Create indexes, each index creation needs to be executed in a seperate transaction!
        try (Transaction tx = graph.beginTx()) {
            Neo4jHelper.createIndexIfNotExist(graph, StructureNode.label(), StructureNode.DELETED_AT_PROPERTY);
            tx.success();
        }
        try (Transaction tx = graph.beginTx()) {
            Neo4jHelper.createUniqueIndexIfNotExist(graph, StructureNode.label(), StructureNode.ID_PROPERTY);
            tx.success();
        }
        try (Transaction tx = graph.beginTx()) {
            Neo4jHelper.createUniqueIndexIfNotExist(graph, ConstructionZoneNode.label(), ConstructionZoneNode.ID_PROPERTY);
            tx.success();
        }

        setupIdGenerator("STRUCTURE_ID");
        setupIdGenerator("CONSTRUCTIONZONE_ID");
    }

    private void setupIdGenerator(String generatorName) {
        try (Transaction tx = graph.beginTx()) {
            Result r = graph.execute("MATCH (sid:ID_GENERATOR {name:'" + generatorName + "'}) "
                    + "RETURN sid "
                    + "LIMIT 1");
            if (!r.hasNext()) {
                graph.execute("CREATE (sid:ID_GENERATOR {name:'" + generatorName + "', nextId: 0})");
            }
            tx.success();
        }
    }

    private void applyUpdates() {
        StructureAPIModelUpdater update = new StructureAPIModelUpdater(graph);
        update.update();
    }

    @Override
    public void addRestriction(StructureRestriction structureRestriction) {
        synchronized (restrictions) {
            this.restrictions.add(structureRestriction);
        }
    }

    @Override
    public void removeRestriction(StructureRestriction structureRestriction) {
        synchronized (restrictions) {
            this.restrictions.remove(structureRestriction);
        }
    }

    /**
     * Gets the StructureAPI instance
     *
     * @return
     */
    public static IStructureAPI getInstance() {
        if (instance == null) {
            instance = new StructureAPI();
        }
        return instance;
    }

    /**
     * Fires all process that need to start at the startup of the StructureAPI
     *
     * @throws DocumentException
     * @throws SettlerCraftException
     */
    public synchronized void initialize() throws DocumentException, SettlerCraftException {
        if (!initialized) {
            // Set states back to stopped... if not completed or removed
            resetStates();
            // Load StructurePlans
            StructurePlanMenuReader reader = new StructurePlanMenuReader();

            // Setup menu
            this.menuTemplate = reader.read(new File(getWorkingDirectory(), "menu.xml"));
            this.planMenuFactory = new StructurePlanMenuFactory(platform, menuTemplate);
            reload();
            
            this.constructionZonePlacerFactory = new ConstructionZonePlacerFactory(this);
            this.structurePlacerFactory = new StructurePlacerFactory(this);
            this.constructionExecutor = Contractor.getInstance();
            this.initialized = true;
        }
    }

    private void resetStates() {
        try (Transaction tx = graph.beginTx()) {

            Map<String, Object> params = Maps.newHashMap();
            // Enforce integers
            params.put("completed", (Integer) ConstructionStatus.COMPLETED.getStatusId());
            params.put("removed", (Integer) ConstructionStatus.REMOVED.getStatusId());
            params.put("stopped", (Integer) ConstructionStatus.COMPLETED.getStatusId());

            String query = "MATCH (s:" + StructureNode.LABEL + ") "
                    + "WHERE NOT s." + StructureNode.CONSTRUCTION_STATUS_PROPERTY + " =  {completed} "
                    + "AND NOT s." + StructureNode.CONSTRUCTION_STATUS_PROPERTY + " =  {removed}"
                    + "SET s." + StructureNode.CONSTRUCTION_STATUS_PROPERTY + " =  {stopped}";
            graph.execute(query, params);

            tx.success();
        }
    }

    /**
     * Reloads all plans from the StructurePlan directory
     */
    @Override
    public void reload() {
        if (loadLock.tryLock()) {
            try {
                StructurePlanManager.getInstance().reload();
            } finally {
                loadLock.unlock();
            }
        }

    }

    @Override
    public boolean isLoadingPlans() {
        return isLoadingPlans;
    }

    @Override
    public IContractor getConstructionExecutor() {
        return constructionExecutor;
    }

    @Override
    public StructurePlanManager getStructurePlanManager() {
        return StructurePlanManager.getInstance();
    }

    @Override
    public final File getWorldDirectory(String world) {
        File f = new File(getWorkingDirectory().getAbsolutePath() + "//worlds//" + world);
        f.mkdirs();
        return f;
    }

    @Override
    public final File getStructuresDirectory(String world) {
        File f = new File(getWorkingDirectory().getAbsolutePath() + "//worlds//" + world + "//structures");
        f.mkdirs(); // creates if not exists..
        return f;
    }

    @Override
    public final File getPlanDirectory() {
        return new File(getWorkingDirectory(), PLANS_DIRECTORY);
    }

    @Override
    public CategoryMenu createPlanMenu() {
        return planMenuFactory.createPlanMenu();
    }

    public void registerStructureAPIPlugin(IStructureAPIPlugin plugin) throws StructureAPIException {
        if (this.plugin != null) {
            throw new StructureAPIException("Already registered a Plugin for the StructureAPI, NOTE that this method should only be used by StructureAPI Plugin itself!");
        }
        this.plugin = plugin;
    }

    @Override
    public APlatform getPlatform() {
        return platform;
    }

    @Override
    public File getWorkingDirectory() {
        return plugin.getDataFolder();
    }

    public void registerConfigProvider(ConfigProvider configProvider) {
        this.config = configProvider;
    }

    public void registerAWE(IAsyncWorldEditIntegration asyncWorldEditIntegration) {
        if (this.asyncWorldEditIntegration != null) {
            throw new RuntimeException("Already registered AWE");
        }
        this.asyncWorldEditIntegration = asyncWorldEditIntegration;
    }

    public IAsyncWorldEditIntegration getAsyncWorldEditIntegration() {
        return asyncWorldEditIntegration;
    }

    @Override
    public boolean isQueueLocked(UUID player) {
        return asyncWorldEditIntegration.isQueueLocked(player);
    }

    final File getDirectoryForStructure(SCWorldNode worldNode, Structure structureNode) {
        File structuresDirectory = getStructuresDirectory(worldNode.getName());
        File structureDir = new File(structuresDirectory, String.valueOf(structureNode.getId()));
        return structureDir;
    }

    @Override
    public ConfigProvider getConfig() {
        return config;
    }

    @Override
    public File getGenerationDirectory() {
        return new File(getWorkingDirectory(), "generate");
    }

    @Override
    public void checkRestrictions(UUID player, String world, CuboidRegion region) throws StructureRestrictionException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    @Override
    public IWorldConfig loadOrGetConfig(String world) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public IStructureAPIPlugin getPlugin() {
        return plugin;
    }

    @Override
    public PhysicsWatch getPhysicsWatcher() {
        return physicsWatch;
    }

    private class StructurePlanManagerHandler {

        @Subscribe
        @AllowConcurrentEvents
        public void onLoadingStructurePlans(StructurePlansReloadEvent event) {
            isLoadingPlans = true;
            platform.getServer().broadcast(COLORS.yellow() + "[SettlerCraft]: " + COLORS.reset() + "Plans are being reloaded...");
//            platform.getConsole().printMessage(COLORS.yellow() + "[SettlerCraft]: " + COLORS.reset() + "Loading StructurePlans");
        }

        @Subscribe
        @AllowConcurrentEvents
        public void onStructurePlansLoaded(StructurePlansLoadedEvent event) {
            planMenuFactory = new StructurePlanMenuFactory(platform, menuTemplate);
            MenuAPI.getInstance().closeMenusWithTag(StructurePlanMenuFactory.PLAN_MENU_TAG);
            planMenuFactory.clearAll();
            for (IStructurePlan plan : StructurePlanManager.getInstance().getPlans()) {
                planMenuFactory.load(plan);
            }
            isLoadingPlans = false;
            platform.getServer().broadcast(COLORS.yellow() + "[SettlerCraft]: " + COLORS.reset() + "Reload plans complete!");
//            platform.getConsole().printMessage(COLORS.yellow() + "[SettlerCraft]: " + COLORS.reset() + "Plans are loaded!");
        }
    }

    public void registerAsyncEditSesionFactoryProvider(AsyncEditSessionFactoryProvider provider) {
        Preconditions.checkNotNull(provider, "Provider was null");
        Preconditions.checkArgument(sessionFactoryProvider == null, "Already registered a AsyncEditSessionFactoryProvider");
        this.sessionFactoryProvider = provider;
    }

    @Override
    public AsyncEditSessionFactory getSessionFactory() {
        return sessionFactoryProvider.getFactory();
    }

    @Override
    public SchematicPlacement loadSchematic(File schematicFile) throws IOException {
        Schematic schematic = SchematicManager.getInstance().getOrLoadSchematic(schematicFile);
        return new SchematicPlacement(schematic);
    }

}
