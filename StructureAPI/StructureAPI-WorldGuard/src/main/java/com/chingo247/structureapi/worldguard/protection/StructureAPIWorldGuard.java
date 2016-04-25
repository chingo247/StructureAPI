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
import com.chingo247.structureapi.model.structure.Structure;
import com.chingo247.structureapi.model.structure.StructureNode;
import com.chingo247.xplatform.core.ICommandSender;
import com.chingo247.xplatform.core.IWorld;
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

    private static final long BULK_SIZE = 5000;
    private static final Label LABEL = DynamicLabel.label("WORLDGUARD_REGION");
    private static final String PREFIX = "SC_";
    private static final String STRUCTURE_PREFIX = PREFIX + "REG_";
    private static final String CONSTRUCTION_ZONE_PREFIX = PREFIX + "CZ_REG_";
    private static final String REGION_PROPERTY = "region";
    private static final String MSG_PREFIX = "[StructureAPI-WorldGuard]: ";

    private boolean installing = false, uninstalling = false;
    private Lock globalLock;

    private Map<String, WorldGuardProtectedWorld> worlds;
    private static StructureAPIWorldGuard instance;
    private ExecutorService activePool;

    private StructureAPIWorldGuard() {
        this.worlds = Maps.newHashMap();
        this.globalLock = new ReentrantLock();
    }

    public static StructureAPIWorldGuard getInstance() {
        if (instance == null) {
            instance = new StructureAPIWorldGuard();
        }
        return instance;
    }

    private synchronized WorldGuardProtectedWorld getWorldGuardProtectedWorld(World world) {
        WorldGuardProtectedWorld w = worlds.get(world.getName());
        if (w == null) {
            w = new WorldGuardProtectedWorld(world.getName());
            worlds.put(world.getName(), w);
        }
        return w;
    }

    public void runExpirationUpdate(final long expirationTime) {
        globalLock.lock();
        ExecutorService pool = null;
        try {
            System.out.println("expiration lock acquired!");
            pool = Executors.newFixedThreadPool(Math.max(Runtime.getRuntime().availableProcessors() - 1, 1));
            List<Future> tasks = Lists.newArrayList();
            for (final World w : Bukkit.getWorlds()) {
                tasks.add(pool.submit(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            System.out.println("Running expiration for world '" + w.getName() + "'");
                            getWorldGuardProtectedWorld(w).runExpiration(expirationTime);
                            System.out.println("Running finished check for world '" + w.getName() + "'");
                        } catch (Exception ex) {
                            Logger.getLogger(StructureAPIWorldGuard.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                        }
                    }
                }));
            }
            for (Future t : tasks) {
                try {
                    t.get();
                } catch (InterruptedException | ExecutionException ex) {
                    // Ignore
                }
            }
        } finally {
            try {
                if (pool != null) {
                    pool.shutdown();
                }
            } catch (Exception ex) {
            } // Silent
            globalLock.unlock();
            System.out.println("Expiration lock released!");
        }
    }

    public void install(ICommandSender executor) {
        globalLock.lock();
        try {
            if (activePool != null && !activePool.isShutdown()) {
                activePool.shutdown();
            }
            installing = true;
        } finally {
            globalLock.unlock();
        }
        try {
            if (executor != null) {
                executor.sendMessage("Protecting structures of all worlds...");
            }
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

            for (Future t : tasks) {
                try {
                    t.get();
                } catch (InterruptedException | ExecutionException ex) {
                    // Ignore
                }
            }
            if (executor != null) {
                executor.sendMessage("Protection completed");
            }

        } finally {
            installing = false;
            activePool.shutdown();
        }
    }

    public void uninstall(ICommandSender executor) {
        globalLock.lock();
        try {
            if (activePool != null && !activePool.isShutdown()) {
                activePool.shutdown();
            }
            uninstalling = true;
        } finally {
            globalLock.unlock();
        }
        try {
            if (executor != null) {
                executor.sendMessage("Removing worldguard regions that belonged to structures...");
            }
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

            for (Future t : tasks) {
                try {
                    t.get();
                } catch (InterruptedException | ExecutionException ex) {
                    // Ignore
                }
            }
            if (executor != null) {
                executor.sendMessage("Uninstall completed");
            }

        } finally {
            uninstalling = false;
            activePool.shutdown();
        }
    }

    public void protectStructuresWorld(World world) throws StorageException, StructureAPIWorldGuardException {
        WorldGuardProtectedWorld w = getWorldGuardProtectedWorld(world);
        w.protectAll();
    }

    public void expireStructureProtection(World world) throws StorageException, StructureAPIWorldGuardException {
        WorldGuardProtectedWorld w = getWorldGuardProtectedWorld(world);
        w.expireAll();
    }

    private String getRegionId(Structure structure) {
        String regionId = STRUCTURE_PREFIX + String.valueOf(structure.getId());
        return regionId;
    }

    public void expire(Structure structure) throws StorageException, StructureAPIWorldGuardException {
        World world = Bukkit.getWorld(structure.getWorldName());
        RegionManager mgr = WGBukkit.getRegionManager(world);
        String region = getRegionId(structure);
        expire(mgr, structure, region);
        mgr.saveChanges();

    }

    private void expire(RegionManager regionManager, Structure structure, String id) {
        regionManager.removeRegion(id);
        Node n = structure.getUnderlyingNode();
        System.out.println("Expired region: " + id);
        for (Relationship r : n.getRelationships(Direction.OUTGOING, RelTypes.PROTECTED_BY)) {
            Node regionNode = r.getOtherNode(n);
            if (regionNode.hasLabel(LABEL)) {
                WorldGuardRegionNode wgRegionNode = new WorldGuardRegionNode(r.getOtherNode(n));
                wgRegionNode.setExpired(true);
                break;
            }
        }
    }

    public void protect(Structure structure) throws StorageException, StructureAPIWorldGuardException {
        World world = Bukkit.getWorld(structure.getWorldName());

        RegionManager mgr = WGBukkit.getRegionManager(world);
        String id = getRegionId(structure);

        protect(world, mgr, structure, id);

        GraphDatabaseService graph = SettlerCraft.getInstance().getNeo4j();
        mgr.save();

        StructureRegionRepository regionRepository = new StructureRegionRepository(graph);
        WorldGuardRegionNode worldguardRegion = regionRepository.findRegionById(id);

        if (worldguardRegion == null) {
            worldguardRegion = new WorldGuardRegionNode(graph.createNode(LABEL));
            worldguardRegion.setExpired(false);
            worldguardRegion.setRegion(id);
            worldguardRegion.setCreatedAt(System.currentTimeMillis());
        }
        structure.getUnderlyingNode().createRelationshipTo(worldguardRegion.getNode(), RelTypes.PROTECTED_BY);
    }

    private void protect(World world, RegionManager regionManager, Structure structure, String id) {
        CuboidRegion dimension = structure.getCuboidRegion();
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
        OwnerDomainNode ownerDomain = new OwnerDomainNode(structure.getUnderlyingNode());

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

    protected boolean addMember(UUID player, Structure structure) {
        World world = Bukkit.getWorld(structure.getWorldName());
        RegionManager mgr = WGBukkit.getRegionManager(world);
        String regionId = getRegionId(structure);
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

    protected boolean addOwner(UUID player, Structure structure) {
        World world = Bukkit.getWorld(structure.getWorldName());
        RegionManager mgr = WGBukkit.getRegionManager(world);
        String regionId = getRegionId(structure);
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

    protected boolean removeOwner(UUID player, Structure structure) {
        World world = Bukkit.getWorld(structure.getWorldName());
        RegionManager mgr = WGBukkit.getRegionManager(world);
        String regionId = getRegionId(structure);
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

    protected boolean removeMember(UUID player, Structure structure) {
        World world = Bukkit.getWorld(structure.getWorldName());
        RegionManager mgr = WGBukkit.getRegionManager(world);
        String regionId = getRegionId(structure);
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

        private Lock worldLock;
        private String world;

        public WorldGuardProtectedWorld(String world) {
            this.worldLock = new ReentrantLock();
            this.world = world;
        }

        public void runExpiration(long expirationTime) {
            worldLock.lock();
            try {
                runExpirationRecursive(expirationTime); // Because no recursive locking
            } finally {
                worldLock.unlock();
            }
        }

        private void runExpirationRecursive(long expirationTime) {
            IWorld w = SettlerCraft.getInstance().getPlatform().getServer().getWorld(world);
            if (w != null) {
                UUID worldUUID = w.getUUID();
                GraphDatabaseService graph = SettlerCraft.getInstance().getNeo4j();
                StructureRegionRepository regionRepository = new StructureRegionRepository(graph);

                RegionManager regionManager = WGBukkit.getRegionManager(Bukkit.getWorld(world));
                
                boolean another;
                try (Transaction tx = graph.beginTx()) {
                    List<Node> toBeExpired = regionRepository.findToBeExpired(worldUUID, expirationTime, BULK_SIZE);
                    
                    for(Node node : toBeExpired) {
                        Structure structure = new Structure(node);
                        expire(regionManager, structure, getRegionId(structure));
                    }
                    
                    another = toBeExpired.size() == BULK_SIZE;
                    tx.success();
                }
                
                try {
                    regionManager.save();
                } catch (StorageException ex) {
                    throw new RuntimeException(ex);
                }
                
                if (another) {
                    runExpirationRecursive(expirationTime);
                }
            }
        }

        public void protectAll() throws StorageException {
            worldLock.lock();
            try {
                protectAllRecursive();
            } finally {
                worldLock.unlock();
            }
        }

        private void protectAllRecursive() {
            GraphDatabaseService graph = SettlerCraft.getInstance().getNeo4j();
            StructureRegionRepository regionRepository = new StructureRegionRepository(graph);
            RegionManager regionManager = WGBukkit.getRegionManager(Bukkit.getWorld(world));
            World bkWorld = Bukkit.getWorld(world);

            boolean anotherRun;
            try (Transaction tx = graph.beginTx()) {
                List<Node> structures = regionRepository.findUnprotectedStructures(BULK_SIZE);

                anotherRun = structures.size() == BULK_SIZE;

                for (Node n : structures) {
                    Structure structure = new Structure(n);
                    protect(bkWorld, regionManager, structure, getRegionId(structure));
                }
                tx.success();
            }
            try {
                regionManager.save();
            } catch (StorageException ex) {
                throw new RuntimeException(ex);
            }
            if (anotherRun) {
                protectAllRecursive();
            }
        }

        public void expireAll() throws StorageException {
            worldLock.lock();
            try {
                expireAllRecursive();
            } finally {
                worldLock.unlock();
            }
        }

        private void expireAllRecursive() {
            GraphDatabaseService graph = SettlerCraft.getInstance().getNeo4j();
            StructureRegionRepository regionRepository = new StructureRegionRepository(graph);
            RegionManager regionManager = WGBukkit.getRegionManager(Bukkit.getWorld(world));

            boolean another;
            try (Transaction tx = graph.beginTx()) {
                List<Node> structures = regionRepository.findProtectedStructures(0, BULK_SIZE);

                another = structures.size() == BULK_SIZE;

                for (Node n : structures) {
                    Structure structure = new Structure(n);
                    expire(regionManager, structure, getRegionId(structure));
                }
                tx.success();
            }
            try {
                regionManager.save();
            } catch (StorageException ex) {
                throw new RuntimeException(ex);
            }

            if (another) {
                expireAllRecursive();
            }
        }
    }

}
