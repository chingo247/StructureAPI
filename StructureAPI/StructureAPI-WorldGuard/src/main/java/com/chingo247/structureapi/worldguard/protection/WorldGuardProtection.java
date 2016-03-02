/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.worldguard.protection;

import com.chingo247.settlercraft.core.model.settler.SettlerNode;
import com.chingo247.structureapi.StructureAPI;
import com.chingo247.structureapi.model.RelTypes;
import com.chingo247.structureapi.model.Spatial;
import com.chingo247.structureapi.model.owner.OwnerDomainNode;
import com.chingo247.structureapi.model.owner.OwnerType;
import com.chingo247.structureapi.model.owner.Ownership;
import com.chingo247.structureapi.model.plot.Plot;
import com.chingo247.structureapi.model.structure.Structure;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.bukkit.WorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

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
    private static final String MSG_PREFIX = "[StructureAPI-WorldGuard]: ";

    private GraphDatabaseService graph;

    public WorldGuardProtection() {
        this.graph = StructureAPI.getInstance().getGraphDatabase();
    }
    
    

    private String getRegionId(Spatial plot) {
        String regionId;
        if (plot instanceof Structure) {
            regionId = STRUCTURE_PREFIX + String.valueOf(((Structure) plot).getId());
        } else {
            regionId = UUID.randomUUID().toString();
        }

        return regionId;
    }

    public void expire(Plot plot) {
        World world = Bukkit.getWorld(plot.getWorldName());
        RegionManager mgr = WGBukkit.getRegionManager(world);
        String region = getRegionId(plot);
        if (mgr.hasRegion(region)) {
            mgr.removeRegion(region);
            try {
                mgr.save();
            } catch (StorageException ex) {
                Logger.getLogger(WorldGuardProtection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        Node n = plot.getUnderlyingNode();
        for(Relationship r : n.getRelationships(Direction.OUTGOING, RelTypes.PROTECTED_BY)) {
            Node pluginNode = r.getOtherNode(n);
            if(pluginNode.hasLabel(LABEL)) {
                pluginNode.setProperty("expired", true);
                break;
            }
        }
    }
    
    public void deleteProtection(Plot plot) {
        World world = Bukkit.getWorld(plot.getWorldName());
        RegionManager mgr = WGBukkit.getRegionManager(world);
        String region = getRegionId(plot);
        if (mgr.hasRegion(region)) {
            mgr.removeRegion(region);
            try {
                mgr.save();
            } catch (StorageException ex) {
                Logger.getLogger(WorldGuardProtection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        Node n = plot.getUnderlyingNode();
        for(Relationship protecteByRel : n.getRelationships(Direction.OUTGOING, RelTypes.PROTECTED_BY)) {
            Node protection = protecteByRel.getOtherNode(n);
            if(protection.hasLabel(LABEL)) {
                protecteByRel.delete();
                for(Relationship pr : protection.getRelationships()) {
                    pr.delete();
                }
                protection.delete();
            }
        }
    }

    public void protect(Plot plot) throws Exception {
        World world = Bukkit.getWorld(plot.getWorldName());
        CuboidRegion dimension = plot.getCuboidRegion();
        RegionManager mgr = WGBukkit.getRegionManager(world);

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
        OwnerDomainNode ownerDomain = new OwnerDomainNode(plot.getUnderlyingNode());

        for (Ownership owner : ownerDomain.getOwnerships()) {

            // Set privilages by type
            SettlerNode ownerNode = owner.getOwner();
            OwnerType type = owner.getOwnerType();
            if (type == OwnerType.MEMBER) {
                region.getMembers().addPlayer(ownerNode.getUniqueId());
            } else {
                region.getOwners().addPlayer(ownerNode.getUniqueId());
            }

        }

        mgr.addRegion(region);
        mgr.save();
        Node worldGuardNode = graph.createNode(LABEL);
        worldGuardNode.setProperty(REGION_PROPERTY, id);
        plot.getUnderlyingNode().createRelationshipTo(worldGuardNode, RelTypes.PROTECTED_BY);
    }
    
    protected boolean regionExists(World world, String id) {
        return WGBukkit.getRegionManager(world).hasRegion(id);
    }

    protected boolean addMember(UUID player, Spatial plot) {
        World world = Bukkit.getWorld(plot.getWorldName());
        RegionManager mgr = WGBukkit.getRegionManager(world);
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
    
    public boolean hasReachedMaxRegionCount(World world, Player player) {
        int maxRegionCount = getWorldConfiguration(world).getMaxRegionCount(player);
        return maxRegionCount >= 0
                && WGBukkit.getRegionManager(world).getRegionCountOfPlayer(getLocalPlayer(player)) >= maxRegionCount;
    }


    public boolean overlaps(World world, BlockVector pos1, BlockVector pos2) {
        return getWorldGuard().getRegionManager(world)
                .getApplicableRegions(new ProtectedCuboidRegion(null, pos1, pos2)).size() > 0;
    }

    public boolean hasRegion(World world, String id) {
        return WGBukkit.getRegionManager(world).hasRegion(id);
    }

    
    public WorldGuardPlugin getWorldGuard() {
        return WorldGuardPlugin.inst();
    }
    
    public WorldConfiguration getWorldConfiguration(World world) {
        return getWorldGuard().getGlobalStateManager().get(world);
    }

    public LocalPlayer getLocalPlayer(Player player) {
        return getWorldGuard().wrapPlayer(player);
    }
    
    protected  boolean addOwner(UUID player, Spatial plot) {
        World world = Bukkit.getWorld(plot.getWorldName());
        RegionManager mgr = WGBukkit.getRegionManager(world);
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

    protected boolean removeOwner(UUID player, Spatial plot) {
        World world = Bukkit.getWorld(plot.getWorldName());
        RegionManager mgr = WGBukkit.getRegionManager(world);
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

    protected  boolean removeMember(UUID player, Spatial plot) {
        World world = Bukkit.getWorld(plot.getWorldName());
        RegionManager mgr = WGBukkit.getRegionManager(world);
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
    
}
