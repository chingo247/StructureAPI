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
package com.chingo247.structureapi.worldguard.protection;

import com.chingo247.settlercraft.core.SettlerCraft;
import com.chingo247.settlercraft.core.model.settler.BaseSettlerNode;
import com.chingo247.settlercraft.core.persistence.neo4j.Neo4jHelper;
import com.chingo247.structureapi.worldguard.restriction.WorldGuardRestriction;
import com.chingo247.structureapi.StructureAPI;
import com.chingo247.structureapi.model.RelTypes;
import com.chingo247.structureapi.model.owner.OwnerDomainNode;
import com.chingo247.structureapi.model.owner.OwnerType;
import com.chingo247.structureapi.model.owner.Ownership;
import com.chingo247.structureapi.model.plot.IPlot;
import com.chingo247.structureapi.model.structure.ConstructionStatus;
import com.chingo247.structureapi.model.structure.IStructure;
import com.chingo247.structureapi.model.structure.Structure;
import com.chingo247.structureapi.model.structure.StructureNode;
import com.chingo247.structureapi.model.zone.ConstructionZone;
import com.chingo247.structureapi.model.zone.ConstructionZoneNode;
import com.chingo247.structureapi.model.zone.IConstructionZone;
import com.chingo247.structureapi.platform.ConfigProvider;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.bukkit.WorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.permission.RegionPermissionModel;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sk89q.worldguard.LocalPlayer;
import java.util.Iterator;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

/**
 *
 * @author Chingo
 */
public class WorldGuardProtection {

    private static final Label LABEL = DynamicLabel.label("WORLDGUARD_REGION");
    private static final String PREFIX = "SC_";
    private static final String STRUCTURE_PREFIX = PREFIX + "REG_";
    private static final String CONSTRUCTION_ZONE_PREFIX = PREFIX + "CZ_REG_";
    private static final String REGION_PROPERTY = "region";
    private static final String MSG_PREFIX = "[SettlerCraft-WorldGuard]: ";

    private static WorldGuardProtection instance;

    private WorldGuardProtection() {
        GraphDatabaseService graph = SettlerCraft.getInstance().getNeo4j();
        try (Transaction tx = graph.beginTx()) {
            Neo4jHelper.createUniqueIndexIfNotExist(graph, LABEL, REGION_PROPERTY);
            tx.success();
        }
    }

    public static WorldGuardProtection getInstance() {
        if (instance == null) {
            instance = new WorldGuardProtection();
        }
        return instance;
    }

    /**
     * Protects a Structure with WorldGuard. Note this will have no effect if
     *
     * @param plot The structure to protect
     */
    public synchronized void protect(IPlot plot) {

        World world = Bukkit.getWorld(plot.getWorldName());
        CuboidRegion dimension = plot.getCuboidRegion();
        RegionManager mgr = getRegionManager(world);

        Vector p1 = dimension.getMinimumPoint();
        Vector p2 = dimension.getMaximumPoint();
        String id = getRegionId(plot);

        if (regionExists(world, id)) {
            mgr.removeRegion(id);
        }

        ProtectedCuboidRegion region = new ProtectedCuboidRegion(
                id,
                new BlockVector(p1.getBlockX(), p1.getBlockY(), p1.getBlockZ()),
                new BlockVector(p2.getBlockX(), p2.getBlockY(), p2.getBlockZ())
        );
        GraphDatabaseService graph = SettlerCraft.getInstance().getNeo4j();

        try (Transaction tx = graph.beginTx()) {
            OwnerDomainNode ownerDomain = new OwnerDomainNode(plot.getUnderlyingNode());

            for (Ownership owner : ownerDomain.getOwnerships()) {

                // Set privilages by type
                BaseSettlerNode ownerNode = owner.getOwner();
                OwnerType type = owner.getOwnerType();
                if (type == OwnerType.MEMBER) {
                    region.getMembers().addPlayer(ownerNode.getUniqueId());
                } else {
                    region.getOwners().addPlayer(ownerNode.getUniqueId());
                }

            }

            mgr.addRegion(region);
            try {
                mgr.save();
            } catch (StorageException ex) {
                Logger.getLogger(WorldGuardPlotListener.class.getName()).log(Level.SEVERE, null, ex);
                tx.failure();
            }
            Node worldGuardNode = graph.createNode(LABEL);
            worldGuardNode.setProperty(REGION_PROPERTY, id);
            plot.getUnderlyingNode().createRelationshipTo(worldGuardNode, RelTypes.PROTECTED_BY);
            tx.success();

        }
    }

    private String getRegionId(IPlot plot) {
        String regionId;
        if (plot instanceof IStructure) {
            regionId = STRUCTURE_PREFIX + String.valueOf(((IStructure) plot).getId());
        } else if (plot instanceof IConstructionZone) {
            regionId = CONSTRUCTION_ZONE_PREFIX + String.valueOf(((IConstructionZone) plot).getId());
        } else {
            regionId = UUID.randomUUID().toString();
        }
        
        
        return regionId;
    }

    private void processStructuresWithoutRegion() {
        final List<Structure> structures = Lists.newArrayList();
        GraphDatabaseService graph = SettlerCraft.getInstance().getNeo4j();
        try (Transaction tx = graph.beginTx()) {
            // (a)-[:PROTECTED_BY]->(w:WORLDGUARD_REGION {region: a.WGRegion})

            String query = "MATCH(s:" + StructureNode.LABEL + ")-[r:" + RelTypes.PROTECTED_BY.name() + "]->(:WORLDGUARD_REGION) "
                    + "WHERE r IS NULL "
                    + "AND NOT s." + StructureNode.CONSTRUCTION_STATUS_PROPERTY + " = " + ConstructionStatus.REMOVED.getStatusId() + " "
                    + "RETURN s";

            Result r = graph.execute(query);
            while (r.hasNext()) {
                Map<String, Object> map = r.next();
                for (Object o : map.values()) { // all structures
                    Node n = (Node) o;
                    Structure structure = new Structure(n);
                    structures.add(structure);
                }
            }
            tx.success();
        }

        if (!structures.isEmpty()) {
            System.out.println(MSG_PREFIX + "Processing " + structures.size() + " structures without a worldguard region");
        }

        SettlerCraft.getInstance().getExecutor().submit(new Runnable() {

            @Override
            public void run() {
                for (Iterator<Structure> sit = structures.iterator(); sit.hasNext();) {
                    Structure s = sit.next();
                    protect(s);
                    System.out.println(MSG_PREFIX + "Protected structure #" + s.getId() + " with 'WorldGuard'");
                    sit.remove();
                }
            }
        });

    }

//    private void processConstructionZonesWithoutRegion() {
//        final List<ConstructionZone> zones = Lists.newArrayList();
//        GraphDatabaseService graph = SettlerCraft.getInstance().getNeo4j();
//        try (Transaction tx = graph.beginTx()) {
//            // (a)-[:PROTECTED_BY]->(w:WORLDGUARD_REGION {region: a.WGRegion})
//
//            String query = "MATCH(s:" + ConstructionZoneNode.LABEL + ")-[r:" + RelTypes.PROTECTED_BY.name() + "]->(:WORLDGUARD_REGION) "
//                    + "WHERE r IS NULL "
//                    + "RETURN s";
//
//            Result r = graph.execute(query);
//            while (r.hasNext()) {
//                Map<String, Object> map = r.next();
//                for (Object o : map.values()) { // all structures
//                    Node n = (Node) o;
//                    ConstructionZone constructionZone = new ConstructionZone(n);
//                    zones.add(constructionZone);
//                }
//            }
//            tx.success();
//        }
//
//        if (!zones.isEmpty()) {
//            System.out.println(MSG_PREFIX + "Processing " + zones.size() + " structures without a worldguard region");
//        }
//
//        SettlerCraft.getInstance().getExecutor().submit(new Runnable() {
//
//            @Override
//            public void run() {
//                for (Iterator<ConstructionZone> czit = zones.iterator(); czit.hasNext();) {
//                    ConstructionZone zone = czit.next();
//                    protect(zone);
//                    czit.remove();
//                }
//            }
//        });
//
//    }

 
    public void removeProtection(IPlot plot, boolean inTransaction) {
        World world = Bukkit.getWorld(plot.getWorldName());
        RegionManager mgr = getRegionManager(world);
        String region = getRegionId(plot);
        if (mgr.hasRegion(region)) {
            mgr.removeRegion(region);
            try {
                mgr.save();
            } catch (StorageException ex) {
                Logger.getLogger(WorldGuardProtection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        Map<String, Object> params = Maps.newHashMap();
        params.put("regionId", region);

        String query = "MATCH (wg:" + LABEL.name() + " { " + REGION_PROPERTY + ": {regionId} }) "
                + "RETURN wg";
        
        GraphDatabaseService graph = SettlerCraft.getInstance().getNeo4j();
        if(!inTransaction) {
            try(Transaction tx = graph.beginTx()) {
                Result r = graph.execute(query, params);
                while (r.hasNext()) {
                    Node regionNode = (Node) r.next().get("wg");
                    for (Relationship rel : regionNode.getRelationships()) {
                        rel.delete();
                    }
                    regionNode.delete();
                }
                tx.success();
            }
        } else {
                Result r = graph.execute(query, params);
                while (r.hasNext()) {
                    Node regionNode = (Node) r.next().get("wg");
                    for (Relationship rel : regionNode.getRelationships()) {
                        rel.delete();
                    }
                    regionNode.delete();
                }
        }
    }
    
    public void initialize() {
        GraphDatabaseService graph = SettlerCraft.getInstance().getNeo4j();
        StructureAPI.getInstance().getAsyncEventBus().register(new WorldGuardPlotListener(this, graph));
        StructureAPI.getInstance().addRestriction(new WorldGuardRestriction());
        processStructuresWithoutRegion();
        processInvalidStructures();
//        processConstructionZonesWithoutRegion();
    }

    boolean addMember(UUID player, IPlot plot) {
        World world = Bukkit.getWorld(plot.getWorldName());
        RegionManager mgr = getRegionManager(world);
        String regionId = getRegionId(plot);
        ProtectedRegion region = mgr.getRegion(regionId);
        if (region != null) {
            if (!region.getMembers().contains(player)) {
                region.getMembers().addPlayer(player);
                try {
                    mgr.save();
                    return true;
                } catch (StorageException ex) {
                    Logger.getLogger(WorldGuardProtection.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return true;
    }

    boolean addOwner(UUID player, IPlot plot) {
        World world = Bukkit.getWorld(plot.getWorldName());
        RegionManager mgr = getRegionManager(world);
        String regionId = getRegionId(plot);
        ProtectedRegion region = mgr.getRegion(regionId);
        if (region != null) {
            if (!region.getOwners().contains(player)) {
                region.getOwners().addPlayer(player);
                try {
                    mgr.save();
                    return true;
                } catch (StorageException ex) {
                    Logger.getLogger(WorldGuardProtection.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } 
        return true;
    }

    boolean removeOwner(UUID player, IPlot plot) {
        World world = Bukkit.getWorld(plot.getWorldName());
        RegionManager mgr = getRegionManager(world);
        String regionId = getRegionId(plot);
        ProtectedRegion region = mgr.getRegion(regionId);
        
        if (region != null) {
            region.getOwners().removePlayer(player);
            try {
                mgr.save();
                return true;
            } catch (StorageException ex) {
                Logger.getLogger(WorldGuardProtection.class.getName()).log(Level.SEVERE, null, ex);
            }
        } 
        return false;
    }

    boolean removeMember(UUID player, IPlot plot) {
        World world = Bukkit.getWorld(plot.getWorldName());
        RegionManager mgr = getRegionManager(world);
        String regionId = getRegionId(plot);
        ProtectedRegion region = mgr.getRegion(regionId);
        if (region != null) {
            region.getMembers().removePlayer(player);
            try {
                mgr.save();
                return true;
            } catch (StorageException ex) {
                Logger.getLogger(WorldGuardProtection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }

    public WorldGuardPlugin getWorldGuard() {
        return WorldGuardPlugin.inst();
    }

    public boolean overlaps(World world, BlockVector pos1, BlockVector pos2) {
        return getWorldGuard().getRegionManager(world)
                .getApplicableRegions(new ProtectedCuboidRegion(null, pos1, pos2)).size() > 0;
    }

    public boolean hasRegion(World world, String id) {
        return getRegionManager(world).hasRegion(id);
    }

    public WorldConfiguration getWorldConfiguration(World world) {
        return getWorldGuard().getGlobalStateManager().get(world);
    }

    public LocalPlayer getLocalPlayer(Player player) {
        return getWorldGuard().wrapPlayer(player);
    }

    public boolean hasReachedMaxRegionCount(World world, Player player) {
        int maxRegionCount = getWorldConfiguration(world).getMaxRegionCount(player);
        return maxRegionCount >= 0
                && getRegionManager(world).getRegionCountOfPlayer(getLocalPlayer(player)) >= maxRegionCount;
    }

    public RegionManager getRegionManager(World world) {
        return WGBukkit.getRegionManager(world);
    }

    public RegionPermissionModel getRegionPermissionModel(Player player) {
        return new RegionPermissionModel(getWorldGuard(), player);
    }

    /**
     * Checks wheter the region exists.
     *
     * @param world The world
     * @param id The region's id
     * @return True if regions exists, otherwise false.
     */
    public boolean regionExists(World world, String id) {
        return getRegionManager(world).hasRegion(id);
    }

    public boolean mayClaim(Player player) {
        RegionPermissionModel permissionModel = getRegionPermissionModel(player);
        return permissionModel.mayClaim();
    }

    /**
     * Checks whether the player can claim a region within a world
     *
     * @param player The player
     * @param world The world
     * @return True if player can claim, false if region count was exceeded or
     * when there is no region manager for the specified world
     */
    public boolean canClaim(Player player, World world) {
        WorldConfiguration wcfg = getWorldGuard().getGlobalStateManager().get(player.getWorld());
        RegionPermissionModel permissionModel = getRegionPermissionModel(player);
        RegionManager mgr = getWorldGuard().getRegionManager(world);
        if (mgr == null) {
            return false;
        }

        // Check whether the player has created too many regions
        if (!permissionModel.mayClaimRegionsUnbounded()) {
            int maxRegionCount = wcfg.getMaxRegionCount(player);
            if (maxRegionCount >= 0
                    && mgr.getRegionCountOfPlayer(getLocalPlayer(player)) >= maxRegionCount) {

                return false;
            }
        }
        return true;
    }

    private void processInvalidStructures() {

        String query = "MATCH(structure:" + StructureNode.LABEL + ")-[r:" + RelTypes.PROTECTED_BY.name() + "]-(:WORLDGUARD_REGION) "
                + "WHERE r IS NULL "
                + "AND structure." + StructureNode.CONSTRUCTION_STATUS_PROPERTY + " = " + ConstructionStatus.REMOVED.getStatusId() + " "
                + "RETURN structure";

        GraphDatabaseService graph = SettlerCraft.getInstance().getNeo4j();
        try (Transaction tx = graph.beginTx()) {
            Result r = graph.execute(query);

            while (r.hasNext()) {
                Node n = (Node) r.next().get("structure");
                Structure structure = new Structure(n);
                removeProtection(structure, true);
                System.out.println("[SettlerCraft-WorldGuard]: Removed protection from structure #" + structure.getId() + " because it was removed");
            }

            tx.success();
        }
    }

}