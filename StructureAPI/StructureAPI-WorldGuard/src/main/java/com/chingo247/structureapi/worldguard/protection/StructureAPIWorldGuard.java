/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.worldguard.protection;

import com.chingo247.settlercraft.core.SettlerCraft;
import com.chingo247.settlercraft.core.model.settler.SettlerNode;
import com.chingo247.structureapi.model.RelTypes;
import com.chingo247.structureapi.model.owner.OwnerDomainNode;
import com.chingo247.structureapi.model.owner.OwnerType;
import com.chingo247.structureapi.model.owner.Ownership;
import com.chingo247.structureapi.model.plot.Plot;
import com.chingo247.structureapi.model.structure.Structure;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
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
import org.neo4j.graphdb.Transaction;

/**
 *
 * @author Chingo
 */
public class StructureAPIWorldGuard {

    private static final long BULK_SIZE = 5000L;
    private static final Label LABEL = DynamicLabel.label("WORLDGUARD_REGION");
    private static final String PREFIX = "SC_";
    private static final String STRUCTURE_PREFIX = PREFIX + "REG_";
    private static final String CONSTRUCTION_ZONE_PREFIX = PREFIX + "CZ_REG_";
    private static final String REGION_PROPERTY = "region";
    private static final String MSG_PREFIX = "[StructureAPI-WorldGuard]: ";

    
    private boolean installing = false, uninstalling = false;
    private Lock guard;
    
    private Map<String, WorldGuardProtectedWorld> worlds;
    private static StructureAPIWorldGuard instance;
    private ExecutorService activePool;

    private StructureAPIWorldGuard() {
        this.worlds = Maps.newHashMap();
        this.guard = new ReentrantLock();
    }
    
    public static StructureAPIWorldGuard getInstance() {
        if (instance == null) {
            instance = new StructureAPIWorldGuard();
        }
        return instance;
    }
    
    private synchronized WorldGuardProtectedWorld getWorldGuardProtectedWorld(World world) {
        WorldGuardProtectedWorld w = worlds.get(world.getName());
        if(w == null) {
            w = new WorldGuardProtectedWorld(world.getName());
            worlds.put(world.getName(), w);
        }
        return w;
    }
    
    public void install() throws StructureAPIWorldGuardException {
        guard.lock();
        try {
            if(activePool != null || !activePool.isShutdown()) {
                activePool.shutdown();
            }
            installing = true;
        } finally {
            guard.unlock();
        }
        try {
            activePool = Executors.newFixedThreadPool(Math.max(Runtime.getRuntime().availableProcessors() - 1, 1));
            List<Future> tasks = Lists.newArrayList();
            for (final World w : Bukkit.getWorlds()) {
                tasks.add(activePool.submit(new Runnable() {

                    @Override
                    public void run() {
                        try {
                        protectStructuresWorld(w);
                        } catch (StorageException ex) {
                            Logger.getLogger(StructureAPIWorldGuard.class.getName()).log(Level.SEVERE, "Failed to protect structures in '" + w.getName() + "'", ex);
                        } catch (StructureAPIWorldGuardException ex) {
                            Logger.getLogger(StructureAPIWorldGuard.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }));
            }
            
            for(Future t : tasks) {
                try {
                    t.get();
                } catch (InterruptedException | ExecutionException ex) {
                    // Ignore
                }
            }
            
        } finally {
            installing = false;
            activePool.shutdown();
        }
    }
    
    public void uninstall() throws StructureAPIWorldGuardException {
        guard.lock();
        try {
            if(activePool != null || !activePool.isShutdown()) {
                activePool.shutdown();
            }
            uninstalling = true;
        } finally {
            guard.unlock();
        }
        try {
            activePool = Executors.newFixedThreadPool(Math.max(Runtime.getRuntime().availableProcessors() - 1, 1));
            List<Future> tasks = Lists.newArrayList();
            for (final World w : Bukkit.getWorlds()) {
                tasks.add(activePool.submit(new Runnable() {

                    @Override
                    public void run() {
                        try {
                        expireStructureProtection(w);
                        } catch (StorageException ex) {
                            Logger.getLogger(StructureAPIWorldGuard.class.getName()).log(Level.SEVERE, "Failed to protect structures in '" + w.getName() + "'", ex);
                        } catch (StructureAPIWorldGuardException ex) {
                            Logger.getLogger(StructureAPIWorldGuard.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }));
            }
            
            for(Future t : tasks) {
                try {
                    t.get();
                } catch (InterruptedException | ExecutionException ex) {
                    // Ignore
                }
            }
            
            
        } finally {
            uninstalling = false;
            activePool.shutdown();
        }
    }
    
    public void checkBusy() throws StructureAPIWorldGuardException {
        guard.lock();
        try {
            if (installing) {
                throw new StructureAPIWorldGuardException("Can't proceed, currently installing StructureAPIWorldGuard...");
            }
            
            if(uninstalling) {
                throw new StructureAPIWorldGuardException("Can't proceed, currently uninstalling StructureAPIWorldGuard...");
            }
        } finally {
            guard.unlock();
        }
    }
    
    public boolean protectStructuresWorld(World world) throws StorageException, StructureAPIWorldGuardException {
        WorldGuardProtectedWorld w = getWorldGuardProtectedWorld(world);
        return w.protectAll();
    }
    
    public boolean expireStructureProtection(World world) throws StorageException, StructureAPIWorldGuardException {
        WorldGuardProtectedWorld w = getWorldGuardProtectedWorld(world);
        return w.expireAll();
    }
    
    private String getRegionId(Plot plot) {
        String regionId;
        if (plot instanceof Structure) {
            regionId = STRUCTURE_PREFIX + String.valueOf(((Structure) plot).getId());
        } else {
            regionId = UUID.randomUUID().toString();
        }

        return regionId;
    }

    public void expire(Plot plot) throws StorageException, StructureAPIWorldGuardException {
        World world = Bukkit.getWorld(plot.getWorldName());
        RegionManager mgr = WGBukkit.getRegionManager(world);
        String region = getRegionId(plot);
        expire(mgr, plot, region);
        mgr.saveChanges();

    }

    private void expire(RegionManager regionManager, Plot plot, String id) {
        regionManager.removeRegion(id);
        Node n = plot.getUnderlyingNode();
        for (Relationship r : n.getRelationships(Direction.OUTGOING, RelTypes.PROTECTED_BY)) {
            Node pluginNode = r.getOtherNode(n);
            if (pluginNode.hasLabel(LABEL)) {
                pluginNode.setProperty("expired", true);
                break;
            }
        }
    }

    public void protect(Plot plot) throws StorageException, StructureAPIWorldGuardException {
        World world = Bukkit.getWorld(plot.getWorldName());

        RegionManager mgr = WGBukkit.getRegionManager(world);
        String id = getRegionId(plot);

        protect(world, mgr, plot, id);

        GraphDatabaseService graph = SettlerCraft.getInstance().getNeo4j();
        mgr.save();
        Node worldGuardNode = graph.createNode(LABEL);
        worldGuardNode.setProperty(REGION_PROPERTY, id);
        plot.getUnderlyingNode().createRelationshipTo(worldGuardNode, RelTypes.PROTECTED_BY);
    }

    private void protect(World world, RegionManager regionManager, Plot plot, String id) {
        CuboidRegion dimension = plot.getCuboidRegion();
        Vector p1 = dimension.getMinimumPoint();
        Vector p2 = dimension.getMaximumPoint();

        if (regionExists(world, id)) {
            regionManager.removeRegion(id);
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
        regionManager.addRegion(region);
    }

    protected boolean regionExists(World world, String id) {
        return WGBukkit.getRegionManager(world).hasRegion(id);
    }

    protected boolean addMember(UUID player, Plot plot) {
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
                    Logger.getLogger(StructureAPIWorldGuard.class.getName()).log(Level.SEVERE, null, ex);
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

    protected boolean addOwner(UUID player, Plot plot) {
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
                    Logger.getLogger(StructureAPIWorldGuard.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return true;
    }

    protected boolean removeOwner(UUID player, Plot plot) {
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
                Logger.getLogger(StructureAPIWorldGuard.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }

    protected boolean removeMember(UUID player, Plot plot) {
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
                Logger.getLogger(StructureAPIWorldGuard.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }

    private class WorldGuardProtectedWorld {

        private Lock guard;
        private String world;

        public WorldGuardProtectedWorld(String world) {
            this.guard = new ReentrantLock();
            this.world = world;
        }

        public boolean protectAll() throws StorageException {
            if (guard.tryLock()) {
                try {
                    GraphDatabaseService graph = SettlerCraft.getInstance().getNeo4j();
                    StructureRegionRepository regionRepository = new StructureRegionRepository(graph);
                    long total;
                    try (Transaction tx = graph.beginTx()) {
                        total = regionRepository.countActive();
                        tx.success();
                    }

                    RegionManager regionManager = WGBukkit.getRegionManager(Bukkit.getWorld(world));
                    World bkWorld = Bukkit.getWorld(world);

                    long done = 0;
                    while (done < total) {
                        List<Node> structures;
                        try (Transaction tx = graph.beginTx()) {
                            structures = regionRepository.findStructuresWithoutRegion(done, done + BULK_SIZE);

                            for (Node n : structures) {
                                Plot plot = new Plot(n);
                                protect(bkWorld, regionManager, plot, getRegionId(plot));
                            }
                            tx.success();
                        }
                        regionManager.save();
                        done += BULK_SIZE;
                    }
                    return true;
                } finally {
                    guard.unlock();
                }
            } else {
                return false;
            }
        }

        public boolean expireAll() throws StorageException {
            if (guard.tryLock()) {
                try {
                    GraphDatabaseService graph = SettlerCraft.getInstance().getNeo4j();
                    StructureRegionRepository regionRepository = new StructureRegionRepository(graph);
                    long total;
                    try (Transaction tx = graph.beginTx()) {
                        total = regionRepository.countActive();
                        tx.success();
                    }

                    RegionManager regionManager = WGBukkit.getRegionManager(Bukkit.getWorld(world));
                    long done = 0;
                    while (done < total) {
                        List<Node> structures;
                        try (Transaction tx = graph.beginTx()) {
                            structures = regionRepository.findStructuresWithoutRegion(done, done + BULK_SIZE);

                            for (Node n : structures) {
                                Plot plot = new Plot(n);
                                expire(regionManager, plot, getRegionId(plot));
                            }
                            tx.success();
                        }
                        regionManager.save();
                        done += BULK_SIZE;
                    }
                    return true;
                } finally {
                    guard.unlock();
                }
            } else {
                return false;
            }
        }
    }

}
