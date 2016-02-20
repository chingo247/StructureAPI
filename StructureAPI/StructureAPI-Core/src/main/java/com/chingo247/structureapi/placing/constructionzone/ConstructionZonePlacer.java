/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.placing.constructionzone;

import com.chingo247.structureapi.StructureAPI;
import com.chingo247.structureapi.event.zone.ConstructionZoneCreateEvent;
import com.chingo247.structureapi.exeption.ConstructionZoneException;
import com.chingo247.structureapi.model.AccessType;
import com.chingo247.structureapi.model.owner.OwnerType;
import com.chingo247.structureapi.model.settler.ISettlerRepository;
import com.chingo247.structureapi.model.settler.SettlerNode;
import com.chingo247.structureapi.model.settler.SettlerRepositiory;
import com.chingo247.structureapi.model.structure.IStructureRepository;
import com.chingo247.structureapi.model.structure.StructureRepository;
import com.chingo247.structureapi.model.world.IStructureWorldRepository;
import com.chingo247.structureapi.model.world.StructureWorldNode;
import com.chingo247.structureapi.model.world.StructureWorldRepository;
import com.chingo247.structureapi.model.zone.ConstructionZone;
import com.chingo247.structureapi.model.zone.ConstructionZoneNode;
import com.chingo247.structureapi.model.zone.ConstructionZoneRepository;
import com.chingo247.structureapi.model.zone.IConstructionZoneRepository;
import com.chingo247.structureapi.placing.AbstractPlacer;
import com.chingo247.structureapi.exeption.ConstructionZoneRestrictionException;
import com.chingo247.xplatform.core.IWorld;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Monitor;
import com.sk89q.worldedit.regions.CuboidRegion;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

/**
 *
 * @author Chingo
 */
public class ConstructionZonePlacer extends AbstractPlacer<IConstructionZonePlacer> implements IConstructionZonePlacer {
    
    private static final Logger LOG = Logger.getLogger(ConstructionZonePlacer.class.getName());

    private AccessType accessType;
    private IWorld world;
    private StructureAPI structureAPI;
    private Monitor monitor;

    public ConstructionZonePlacer(IWorld world) {
        this.accessType = AccessType.PRIVATE;
        this.structureAPI = (StructureAPI) StructureAPI.getInstance();
        this.monitor = structureAPI.getMonitor(world.getName());
        this.world = world;
    }

    @Override
    public IConstructionZonePlacer setAccess(AccessType accessType) {
        Preconditions.checkNotNull(accessType, "AccessType may not be null");
        this.accessType = accessType;
        return this;
    }

    @Override
    public IConstructionZonePlaceResult place(CuboidRegion region) {
        ConstructionZonePlaceResult placeResult = new ConstructionZonePlaceResult();

        try {
            // Check too low
            if (region.getMinimumY() <= 0) {
                throw new ConstructionZoneException("Minimum place height for construction-zones is 1");
            }

            // Check too high
            if (region.getMaximumY() > region.getMaximumY()) {
                throw new ConstructionZoneException("Construction-Zone exceeds world height");
            }

            Transaction tx = null;
            GraphDatabaseService graph = structureAPI.getGraphDatabase();
            try {
                monitor.enter();
                tx = graph.beginTx();
                
                
                IStructureRepository structureRepository = new StructureRepository(graph);
                //TODO offer auto grow feature
                if(structureRepository.hasStructuresWithin(world.getUUID(), region)) {
                    throw new ConstructionZoneException("ConstructionZone will overlap existing structures");
                }
                
                
                IConstructionZoneRepository constructionZoneRepository = new ConstructionZoneRepository(graph);
                // Check overlap
                if (constructionZoneRepository.hasWithin(world.getUUID(), region)) {
                    throw new ConstructionZoneRestrictionException("ConstructionZone will overlap another construction zone");
                }
                
                // Create the zone
                ConstructionZoneNode zone = new ConstructionZoneNode(constructionZoneRepository.add(region).getNode());
                zone.setAccessType(accessType);
                
                // Add zone to world
                IStructureWorldRepository structureWorldRepository = new StructureWorldRepository(graph);
                StructureWorldNode structureWorld = structureWorldRepository.addOrGet(world.getName(), world.getUUID());
                structureWorld.addZone(zone);
                
                
                // Add owners
                ISettlerRepository settlerRepository = new SettlerRepositiory(graph);
                Set<UUID> added = Sets.newHashSet();
                // Add masters
                for(UUID player : getMasters()) {
                    SettlerNode settler = settlerRepository.findByUUID(player);
                    zone.getOwnerDomain().setOwnership(settler, OwnerType.MASTER);
                    added.add(player);
                }
                
                // Add owners
                for(UUID player : getOwners()) {
                    if(!added.contains(player)) {
                        SettlerNode settler = settlerRepository.findByUUID(player);
                        zone.getOwnerDomain().setOwnership(settler, OwnerType.OWNER);
                        added.add(player);
                    }
                }
                
                // Add members
                for(UUID player : getMembers()) {
                    if(!added.contains(player)) {
                        SettlerNode settler = settlerRepository.findByUUID(player);
                        zone.getOwnerDomain().setOwnership(settler, OwnerType.MEMBER);
                        added.add(player);
                    }
                }
                
                placeResult.setZone(new ConstructionZone(zone));

                tx.success();
            } catch (Exception ex) {
                if (tx != null) {
                    tx.failure();
                }
                LOG.log(Level.SEVERE, ex.getMessage(), ex);
            } finally {
                if (tx != null) {
                    tx.close();
                }
                monitor.leave();
            }


        } catch (ConstructionZoneException ex) {
            placeResult.setError(ex.getMessage());
            placeResult.setZone(null); // ensure null
        }
        
        if(placeResult.succes()) {
            StructureAPI.getInstance().getEventDispatcher().dispatchEvent(new ConstructionZoneCreateEvent(placeResult.getPlacedConstructionZone()));
        }
        
        
        return placeResult;
    }

}
