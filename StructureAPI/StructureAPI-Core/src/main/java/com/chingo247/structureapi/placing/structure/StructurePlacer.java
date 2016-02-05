/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.placing.structure;

import com.chingo247.settlercraft.core.Direction;
import com.chingo247.settlercraft.core.SettlerCraft;
import com.chingo247.structureapi.StructureAPI;
import com.chingo247.structureapi.event.structure.StructureCreateEvent;
import com.chingo247.structureapi.exeption.StructureException;
import com.chingo247.structureapi.model.owner.OwnerDomainNode;
import com.chingo247.structureapi.model.owner.OwnerType;
import com.chingo247.structureapi.model.owner.Ownership;
import com.chingo247.structureapi.model.settler.ISettlerRepository;
import com.chingo247.structureapi.model.settler.SettlerNode;
import com.chingo247.structureapi.model.settler.SettlerRepositiory;
import com.chingo247.structureapi.model.structure.IStructure;
import com.chingo247.structureapi.model.structure.IStructureRepository;
import com.chingo247.structureapi.model.structure.Structure;
import com.chingo247.structureapi.model.structure.StructureNode;
import com.chingo247.structureapi.model.structure.StructureRepository;
import com.chingo247.structureapi.model.world.IStructureWorldRepository;
import com.chingo247.structureapi.model.world.StructureWorldNode;
import com.chingo247.structureapi.model.world.StructureWorldRepository;
import com.chingo247.structureapi.placement.FilePlacement;
import com.chingo247.structureapi.placement.IPlacement;
import com.chingo247.structureapi.placing.AbstractPlacer;
import com.chingo247.structureapi.plan.IStructurePlan;
import com.chingo247.structureapi.plan.io.export.PlacementExporter;
import com.chingo247.structureapi.plan.io.export.UnsupportedPlacementException;
import com.chingo247.structureapi.StructureRestriction;
import com.chingo247.structureapi.exeption.StructureRestrictionException;
import com.chingo247.structureapi.util.PlacementUtil;
import com.chingo247.structureapi.util.RegionUtil;
import com.chingo247.xplatform.core.ILocation;
import com.chingo247.xplatform.core.IWorld;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.common.util.concurrent.Monitor;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.World;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

/**
 *
 * @author Chingo
 */
public class StructurePlacer extends AbstractPlacer<IStructurePlacer> implements IStructurePlacer {

    private final Monitor monitor;
    private String name;
    private double price;
    private boolean inheritOwnership;
    private boolean checkOwnerRestriction;
    private boolean checkStructureRestrictions;
    private IStructure parent;
    private StructureAPI structureAPI;
    private final IWorld world;
    private UUID placer;

    public StructurePlacer(IWorld world) {
        this.world = world;
        this.price = 0.0d;
        this.inheritOwnership = true;
        this.checkOwnerRestriction = true;
        this.structureAPI = (StructureAPI)StructureAPI.getInstance();
        this.monitor = structureAPI.getMonitor(world.getName());
        this.checkStructureRestrictions = true;
    }

    @Override
    public IStructurePlacer setPlacer(UUID placer) {
        this.placer = placer;
        return this;
    }

    
    @Override
    public IStructurePlacer setCheckStructureRestrictions(boolean checkStructureRestrictions) {
        this.checkStructureRestrictions = checkStructureRestrictions;
        return this;
    }
    
    

    @Override
    public IStructurePlacer setCheckOwnerRestriction(boolean enable) {
        this.checkOwnerRestriction = enable;
        return this;
    }
    
    

    @Override
    public StructurePlacer setInheritOwnership(boolean inheritOwnership) {
        this.inheritOwnership = inheritOwnership;
        return this;
    }

    @Override
    public StructurePlacer setPrice(double price) {
        this.price = price;
        return this;
    }

    @Override
    public StructurePlacer setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public IStructurePlacer setParent(IStructure parent) {
        this.parent = parent;
        return this;
    }

    @Override
    public IStructurePlaceResult place(CuboidRegion structureRegion) throws IOException, UnsupportedPlacementException {
        return place(structureRegion, structureRegion.getMinimumPoint(), Direction.EAST, null);
    }

    @Override
    public IStructurePlaceResult place(final IPlacement placement, final Vector position, final Direction direction) throws IOException, UnsupportedPlacementException {
        Vector min = position;
        Vector max = PlacementUtil.getPoint2Right(min, direction, placement.getCuboidRegion().getMaximumPoint());
        IStructurePlaceResult result = place(new CuboidRegion(min, max), position, direction, new ICallback() {

            @Override
            public void onCreate(IStructure structure) throws IOException, UnsupportedPlacementException {
                copyResources(structure, placement);
            }
        });
        return result;
    }

    @Override
    public IStructurePlaceResult place(final IStructurePlan plan, final Vector position, final Direction direction) throws IOException, UnsupportedPlacementException  {
        CuboidRegion affectedArea = getAffectedRegion(plan.getPlacement(), position, direction);
        IStructurePlaceResult placeResult = place(affectedArea, position, direction, new ICallback() {

            @Override
            public void onCreate(IStructure structure) throws IOException {
                copyResources(structure, plan);
            }
        });
        return placeResult;
    }

    private IStructurePlaceResult place(CuboidRegion region, Vector position, Direction direction, ICallback callback) throws IOException, UnsupportedPlacementException {
        StructurePlaceResult placeResult = new StructurePlaceResult();

        ILocation spawn = world.getSpawn();
        

        try {
            

            // Check too low
            if (region.getMinimumY() <= 0) {
                throw new StructureException("Minimum place height for structures is 1");
            }

            // Check too high
            if (region.getMaximumY() > region.getMaximumY()) {
                throw new StructureException("Structure exceeds world height");
            }

            // Check overlap world's spawn
            if (region.contains(new BlockVector(spawn.getBlockX(), spawn.getBlockY(), spawn.getBlockZ()))) {
                throw new StructureException("Structure overlaps the world's spawn");
            }
            
            if(checkStructureRestrictions) {
                for(StructureRestriction restriction : structureAPI.getRestrictions()) {
                    Player player = SettlerCraft.getInstance().getPlayer(placer);
                    World w = SettlerCraft.getInstance().getWorld(world.getUUID());
                    restriction.check(player, w, region);
                }
            }

            GraphDatabaseService graph = structureAPI.getGraphDatabase();
            Transaction tx = null;
            try {
                tx = graph.beginTx();
                monitor.enter();
                
                

                // Set parent if applicable
                IStructureRepository structureRepository = new StructureRepository(graph);
                StructureNode parentNode = null;
                if (parent == null) {
                    parentNode = structureRepository.findStructureOnPosition(world.getUUID(), position);
                } else {
                    parentNode = structureRepository.findById(parent.getId());
                }

                // No parent, check overlap globally
                if (parent == null) {
                    if (structureRepository.hasStructuresWithin(world.getUUID(), region)) {
                        throw new StructureException("Structure overlaps another structure");
                    }

                    // Check parent - In parent only if applicable
                } else {
                    
                    if(placer != null && checkOwnerRestriction) {
                        Ownership ownership = parentNode.getOwnerDomain().getOwnership(placer);
                        if(ownership == null) {
                            throw new StructureException("Can't place structures within structures you don't own");
                        } else if (ownership.getOwnerType() != OwnerType.MASTER) {
                            throw new StructureException("Can't place structure within structure you don't have MASTER privileges of");
                        }
                    }

                    // Check parent in progress
                    if (parentNode.getStatus().isInProgress()) {
                        throw new StructureException("Can't place structures in structures that are in progress");
                    }

                    if (parentNode.hasSubstructuresWithin(region)) {
                        throw new StructureException("Structure overlaps another structure");
                    }
                }
                
                StructureNode structureNode = new StructureNode(
                        structureRepository.addStructure(name, position, region, direction, price).getNode()
                );
                
                // Add structure to parent if applicable
                if(parentNode != null) {
                    parentNode.addSubstructure(structureNode);
                }
                
                // Add structure to the world
                IStructureWorldRepository worldRepository = new StructureWorldRepository(graph);
                StructureWorldNode sructureWorld = worldRepository.addOrGet(world.getName(), world.getUUID());
                sructureWorld.addStructure(structureNode);
                
                
                // Set ownerships...
                ISettlerRepository settlerRepository = new SettlerRepositiory(graph);
                OwnerDomainNode ownerDomain = structureNode.getOwnerDomain();
                
                // Set placer as MASTER owner
                Set<UUID> added = Sets.newHashSet();
                if(placer != null) {
                    SettlerNode settler = settlerRepository.findByUUID(placer);
                    ownerDomain.setOwnership(settler, OwnerType.MASTER);
                    added.add(placer);
                }
                
                // If inherit ownership is enabled
                if(inheritOwnership) {
                    if(parent != null) {
                        for(Ownership ownership : ownerDomain.getOwnerships()) {
                            SettlerNode parentOwner = ownership.getOwner();
                            OwnerType type = ownership.getOwnerType();
                            ownerDomain.setOwnership(parentOwner, type);
                            added.add(parentOwner.getUniqueId());
                        }
                    }
                }
                
                // Add Masters
                for(UUID playerUUID : getMasters()) {
                    if(!added.contains(playerUUID)) {
                        SettlerNode settler = settlerRepository.findByUUID(playerUUID);
                        ownerDomain.setOwnership(settler, OwnerType.MASTER);
                    }
                }
                
                // Add Owners
                for(UUID playerUUID : getOwners()) {
                    if(!added.contains(playerUUID)) {
                        SettlerNode settler = settlerRepository.findByUUID(playerUUID);
                        ownerDomain.setOwnership(settler, OwnerType.OWNER);
                    }
                }
                
                // Add Members
                for(UUID playerUUID : getMembers()) {
                    if(!added.contains(playerUUID)) {
                        SettlerNode settler = settlerRepository.findByUUID(playerUUID);
                        ownerDomain.setOwnership(settler, OwnerType.MEMBER);
                    }
                }
                
                
                Structure structure = new Structure(structureNode);
                if(callback != null) {
                    callback.onCreate(structure);
                }
                
                placeResult.setStructure(structure);
                structureAPI.getEventDispatcher().dispatchEvent(new StructureCreateEvent(structure));
                tx.success();
            } catch (StructureException | UnsupportedPlacementException ex) {
                if (tx != null) {
                    tx.failure();
                }
                throw ex;
            } finally {
                if (tx != null) {
                    tx.close();
                }
                monitor.leave();
            }

        } catch (StructureRestrictionException | StructureException ex) {
            placeResult.setError(ex.getMessage());
        } 
        return placeResult;
    }

    private CuboidRegion getAffectedRegion(IPlacement placement, Vector position, Direction direction) {
        Vector min = position;
        
        Vector max = PlacementUtil.getPoint2Right(min, direction, placement.getCuboidRegion().getMaximumPoint());
        return new CuboidRegion(min, max);
    }

    private void copyResources(IStructure structure, IStructurePlan plan) throws IOException {
        // Give this structure a directory!
        File structureDirectory = structure.getDirectory();
        if (structureDirectory.exists()) {
            FileUtils.deleteDirectory(structureDirectory);
        }
        
        structureDirectory = structure.getDirectory();
        structureDirectory.mkdirs();

        Files.copy(plan.getFile(), new File(structureDirectory, "structureplan.xml"));
        IPlacement placement = plan.getPlacement();

        // Move the resources if applicable!
        if (placement instanceof FilePlacement) {
            FilePlacement filePlacement = (FilePlacement) placement;
            File[] files = filePlacement.getFiles();
            for (File f : files) {
                Files.copy(f, new File(structureDirectory, f.getName()));
            }
        }
    }

    private void copyResources(IStructure structure, IPlacement placement) throws IOException, UnsupportedPlacementException {
        File structureDirectory = structure.getDirectory();
        if (structureDirectory.exists()) {
            FileUtils.deleteDirectory(structureDirectory);
        }
        structureDirectory.mkdirs();
        File structurePlanFile = new File(structureDirectory, "structureplan.xml");
        PlacementExporter exporter = new PlacementExporter();
        exporter.export(placement, structurePlanFile, "structureplan.xml", true);
    }

   

    private interface ICallback {

        void onCreate(IStructure structure) throws IOException, UnsupportedPlacementException;

    }

}
