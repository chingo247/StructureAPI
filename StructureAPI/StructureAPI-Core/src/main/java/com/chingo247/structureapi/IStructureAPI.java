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

import com.chingo247.structureapi.exeption.StructureRestrictionException;
import com.chingo247.menuapi.menu.CategoryMenu;
import com.chingo247.settlercraft.core.event.EventDispatcher;
import com.chingo247.structureapi.placement.block.SchematicPlacement;
import com.chingo247.structureapi.plan.StructurePlanManager;
import com.chingo247.structureapi.platform.ConfigProvider;
import com.chingo247.xplatform.core.APlatform;
import com.google.common.eventbus.EventBus;
import com.sk89q.worldedit.regions.CuboidRegion;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import org.neo4j.graphdb.GraphDatabaseService;
import org.primesoft.asyncworldedit.worldedit.AsyncEditSessionFactory;
import com.chingo247.structureapi.construction.IContractor;
import com.chingo247.structureapi.watchers.PhysicsWatch;
import com.chingo247.xplatform.core.IPlugin;

/**
 *
 * @author Chingo
 */
public interface IStructureAPI {
    
    /**
     * Gets the physics watcher
     * @return The physics watcher
     */
    PhysicsWatch getPhysicsWatcher();
    
    /**
     * Gets the graph database
     * @return The graph database
     */
    GraphDatabaseService getGraphDatabase();
    
    /**
     * Gets the event dispatcher
     * @return The event dispatcher
     */
    EventDispatcher getEventDispatcher();
    
    /**
     * Gets the async eventbus
     * @return The async eventbus
     */
    EventBus getAsyncEventBus();
    
    /**
     * Gets the eventbus
     * @return The eventbus 
     */
    EventBus getEventBus();
    
    /**
     * Gets the ConstructionManager
     *
     * @return The ConstructionManager
     */
    IContractor getContractor();
    
    /**
     * Gets the StructurePlacerFactory
     * @return The StructurePlacerFactory
     */
    StructurePlacerFactory getStructurePlacerFactory();
    
//    IConstructionZonePlacerFactory getConstructionZonePlacerFactory();
    
    /**
     * Gets the AsyncWordEditIntegration
     * @return The asyncworldeidt integration
     */
    IAsyncWorldEditIntegration getAsyncWorldEditIntegration();
    
    /**
     * Gets the StructurePlanManager, alternatively {@link StructurePlanManager#getInstance()
     * } may be used
     *
     * @return The StructurePlanManager
     */
    StructurePlanManager getStructurePlanManager();

    /**
     * Reloads the StructureAPI
     */
    void reload();

    /**
     * Checks if the AWE queue is locked for a given UUID
     *
     * @param player The player UUID or PlayerEntry UUID
     * @return True if the queue was locked
     * @deprecated Will be removed in the future, instead use {@link #getAsyncWorldEditIntegration()}
     */
    @Deprecated
    boolean isQueueLocked(UUID player);

    /**
     * Checks if StructureAPI is loading (plans, schematics, etc)
     *
     * @return True if StructureAPI is loadings
     */
    boolean isLoadingPlans();

    /**
     * Gets the ConfigProvider
     *
     * @return The ConfigProvider
     */
    ConfigProvider getConfig();

    /**
     * Gets the platform
     *
     * @return The platform
     */
    APlatform getPlatform();

    /**
     * Gets the plan directory
     *
     * @return The plan directory
     */
    File getPlanDirectory();

    /**
     * The directory where plans are generated from schematics
     *
     * @return The directory
     */
    File getGenerationDirectory();

    /**
     * The directory of the plugin
     *
     * @return The directory
     */
    File getWorkingDirectory();

    /**
     * Gets the directory where world data is stored (within StructureAPI
     * directory)
     *
     * @param world The world
     * @return The directory for given world
     */
    File getWorldDirectory(String world);

    /**
     * Gets the Structures directory for a world
     *
     * @param world The world
     * @return The structures directory
     */
    File getStructuresDirectory(String world);

    /**
     * Creates a new PlanMenu, this PlanMenu is loaded with all the plans
     * available. The Plan Menu can be used by ONE player. Each player requires
     * it's own plan menu, therefore new instances need to be created
     *
     * @return The PlanMenu
     */
    CategoryMenu createPlanMenu();


    /**
     * Checks all StructureRestrictions. Each restriction determines if
     * something is allowed to be placed in a certain area by a certain player.
     *
     * @param player The player, may be null
     * @param world The world
     * @param region The region
     * @throws
     * com.chingo247.structureapi.exception.StructureRestrictionException Thrown
     * when a restriction was violated
     */
    void checkRestrictions(UUID player, String world, CuboidRegion region) throws StructureRestrictionException;

    /**
     * Adds a restriction, restrictions are checked before placing a structure
     * @param structureRestriction The structure restriction to add
     */
    void addRestriction(StructureRestriction structureRestriction);
    
    /**
     * Removed a StructureRestriction
     * @param structureRestriction The restriction to remove
     */
    void removeRestriction(StructureRestriction structureRestriction);
    
    /**
     * Gets current restrictions that are applied before structure placement
     * @return The restrictions
     */
    Iterable<StructureRestriction> getRestrictions();
    
    /**
     * Loads a schematic file
     * @param schematicFile The schematic file to load
     * @return The schematicPlacement
     * @throws IOException
     */
    SchematicPlacement loadSchematic(File schematicFile) throws IOException;

//    IWorldConfig loadOrGetConfig(String world);

    /**
     * Gets an AsyncEditSessionFactory
     * @return The AsyncEditSessionFactor
     */
    AsyncEditSessionFactory getSessionFactory();
    
    /**
     * Gets the ExecutorSerice (ThreadPool) which StructureAPI uses
     * @return The ExecutorService
     */
    ExecutorService getExecutor();

    /**
     * Gets the StructureAPI plugin
     * @return The plugin
     */
    IPlugin getPlugin();
    
}
