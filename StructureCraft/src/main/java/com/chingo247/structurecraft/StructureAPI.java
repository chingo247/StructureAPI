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
package com.chingo247.structurecraft;

import com.chingo247.structurecraft.exeption.StructureAPIException;
import com.chingo247.structurecraft.restriction.StructureRestriction;
import com.chingo247.structurecraft.model.structure.ConstructionStatus;
import com.chingo247.structurecraft.model.structure.StructureNode;
import com.chingo247.menuapi.menu.CategoryMenu;
import com.chingo247.menuapi.menu.MenuAPI;
import com.chingo247.xplatform.core.APlatform;
import com.chingo247.xplatform.core.IPlugin;
import com.chingo247.settlercraft.core.SettlerCraft;
import com.chingo247.settlercraft.core.event.EventManager;
import com.chingo247.settlercraft.core.exception.SettlerCraftException;
import com.chingo247.structurecraft.menu.StructurePlanMenuFactory;
import com.chingo247.structurecraft.menu.StructurePlanMenuReader;
import com.chingo247.settlercraft.core.model.WorldNode;
import com.chingo247.settlercraft.core.persistence.neo4j.Neo4jHelper;
import com.chingo247.structurecraft.construction.ConstructionManager;
import com.chingo247.structurecraft.construction.IConstructionManager;
import com.chingo247.structurecraft.event.EventDispatcher;
import com.chingo247.structurecraft.plan.interfaces.IStructurePlan;
import com.chingo247.structurecraft.plan.StructurePlanManager;
import com.chingo247.structurecraft.event.StructurePlansLoadedEvent;
import com.chingo247.structurecraft.event.StructurePlansReloadEvent;
import com.chingo247.structurecraft.model.structure.Structure;
import com.chingo247.structurecraft.platform.services.AsyncEditSessionFactoryProvider;
import com.chingo247.structurecraft.model.zone.ConstructionZoneNode;
import com.chingo247.structurecraft.placement.SchematicPlacement;
import com.chingo247.structurecraft.placement.schematic.Schematic;
import com.chingo247.structurecraft.placement.schematic.SchematicManager;
import com.chingo247.structurecraft.platform.ConfigProvider;
import com.chingo247.structurecraft.placing.structure.StructurePlacer;
import com.chingo247.structurecraft.placing.structure.IStructurePlacerFactory;
import com.chingo247.structurecraft.restriction.exception.StructureRestrictionException;
import com.chingo247.xplatform.core.IColors;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.Monitor;
import com.sk89q.worldedit.regions.CuboidRegion;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import org.dom4j.DocumentException;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.primesoft.asyncworldedit.AsyncWorldEditMain;
import org.primesoft.asyncworldedit.blockPlacer.BlockPlacer;
import org.primesoft.asyncworldedit.playerManager.PlayerEntry;
import org.primesoft.asyncworldedit.worldedit.AsyncEditSessionFactory;

/**
 *
 * @author Chingo
 */
public class StructureAPI implements IStructureAPI {

    public static final String STRUCTURE_PLAN_FILE_NAME = "plan.xml";
    public static final String PLUGIN_NAME = "SettlerCraft";
    public static final String PLANS_DIRECTORY = "plans";
    private final Set<StructureRestriction> restrictions;
    private final APlatform platform;
    private IPlugin plugin;
    private ConfigProvider config;
    private IConstructionManager constructionManager;

    private final Lock loadLock = new ReentrantLock();
    private StructurePlanMenuFactory planMenuFactory;
    private AsyncEditSessionFactoryProvider sessionFactoryProvider;

    private CategoryMenu planMenu;
    private final GraphDatabaseService graph;

    private boolean isLoadingPlans = false, initialized = false;
    private static StructureAPI instance;

    private final Logger LOG = Logger.getLogger(getClass().getName());

    private final IColors COLORS;
    private Level logLevel;
    private final Map<String, Monitor> monitors;
    private final IStructurePlacerFactory structurePlacerFactory;

    private StructureAPI() {
        this.platform = SettlerCraft.getInstance().getPlatform();
        this.graph = SettlerCraft.getInstance().getNeo4j();
        this.constructionManager = ConstructionManager.getInstance();
        this.monitors = Maps.newHashMap();
        this.COLORS = platform.getChatColors();
        this.restrictions = Sets.newHashSet();
        this.logLevel = Level.SEVERE;
        

        EventManager.getInstance().getEventBus().register(new StructurePlanManagerHandler());
        setupSchema();
        applyUpdates();
        
        this.structurePlacerFactory = new StructurePlacerFactory(this);
    }
    
    @Override
    public GraphDatabaseService getGraphDatabase() {
        return SettlerCraft.getInstance().getNeo4j();
    }
    
    @Override
    public EventDispatcher getEventDispatcher() {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    public IStructurePlacerFactory getStructurePlacerFactory() {
       return structurePlacerFactory;
    }
    
    @Override
    public synchronized Monitor getMonitor(String world) {
        Monitor monitor = monitors.get(world);
        if(monitor == null) {
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
        System.out.println(" ");
        System.out.println(" ");
        setupIdGenerator("STRUCTURE_ID");
        setupIdGenerator("CONSTRUCTIONZONE_ID");
        System.out.println(" ");
        System.out.println(" ");
    }

    private void setupIdGenerator(String generatorName) {
        try (Transaction tx = graph.beginTx()) {
            Result r = graph.execute("MATCH (sid:ID_GENERATOR {name:'" + generatorName + "'}) "
                    + "RETURN sid "
                    + "LIMIT 1");
            System.out.println("ID GENERATOR: " + generatorName + " has next: " + r.hasNext());
            if (!r.hasNext()) {
                graph.execute("CREATE (sid:ID_GENERATOR {name:'" + generatorName + "', nextId: 0})");
            }
            tx.success();
        }
    }

    private void applyUpdates() {
//        StructureAPI_Update_2_2_0 update = new StructureAPI_Update_2_2_0(graph);
//        update.update();
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

            planMenu = reader.read(new File(getWorkingDirectory(), "menu.xml"));
            planMenuFactory = new StructurePlanMenuFactory(platform, planMenu);

            reload();
            initialized = true;
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
    public boolean isLoading() {
        return isLoadingPlans;
    }

    @Override
    public IConstructionManager getConstructionManager() {
        return constructionManager;
    }

    @Override
    public StructurePlanManager getStructurePlanManager() {
        return StructurePlanManager.getInstance();
    }

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

    public void registerStructureAPIPlugin(IPlugin plugin) throws StructureAPIException {
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

    public void setLogLevel(Level level) {
        this.logLevel = level;
    }

    public Level getLogLevel() {
        return logLevel;
    }

    @Override
    public boolean isQueueLocked(UUID player) {
        BlockPlacer bp = (BlockPlacer) AsyncWorldEditMain.getInstance().getBlockPlacer();

        Class bpClass = bp.getClass();

        Field f;
        try {
            f = bpClass.getDeclaredField("m_lockedQueues");
            f.setAccessible(true);

            HashSet s = (HashSet) f.get(bp);

            PlayerEntry playerEntry = new PlayerEntry(null, player);
            return s.contains(playerEntry);

        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(StructureAPI.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    final File getDirectoryForStructure(WorldNode worldNode, Structure structureNode) {
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
            planMenuFactory = new StructurePlanMenuFactory(platform, planMenu);
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
