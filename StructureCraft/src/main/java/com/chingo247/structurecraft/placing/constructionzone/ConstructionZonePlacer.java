/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.placing.constructionzone;

import com.chingo247.settlercraft.core.event.async.AsyncEventManager;
import com.chingo247.structurecraft.IStructureAPI;
import com.chingo247.structurecraft.StructureAPI;
import com.chingo247.structurecraft.event.zone.CreateConstructionZoneEvent;
import com.chingo247.structurecraft.exeption.ConstructionZoneException;
import com.chingo247.structurecraft.exeption.StructureException;
import com.chingo247.structurecraft.model.AccessType;
import com.chingo247.structurecraft.model.owner.OwnerType;
import com.chingo247.structurecraft.model.settler.ISettler;
import com.chingo247.structurecraft.model.settler.ISettlerRepository;
import com.chingo247.structurecraft.model.settler.SettlerRepositiory;
import com.chingo247.structurecraft.model.structure.IStructureRepository;
import com.chingo247.structurecraft.model.structure.StructureRepository;
import com.chingo247.structurecraft.model.world.IStructureWorldRepository;
import com.chingo247.structurecraft.model.world.StructureWorld;
import com.chingo247.structurecraft.model.world.StructureWorldRepository;
import com.chingo247.structurecraft.model.zone.ConstructionZone;
import com.chingo247.structurecraft.model.zone.ConstructionZoneNode;
import com.chingo247.structurecraft.model.zone.ConstructionZoneRepository;
import com.chingo247.structurecraft.model.zone.IConstructionZone;
import com.chingo247.structurecraft.model.zone.IConstructionZoneRepository;
import com.chingo247.structurecraft.placing.AbstractPlacer;
import com.chingo247.structurecraft.restriction.exception.ConstructionZoneRestrictionException;
import com.chingo247.xplatform.core.IWorld;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Monitor;
import com.sk89q.worldedit.regions.CuboidRegion;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

/**
 *
 * @author Chingo
 */
public class ConstructionZonePlacer extends AbstractPlacer<IConstructionZonePlacer> implements IConstructionZonePlacer {

    private AccessType accessType;
    private IWorld world;
    private IStructureAPI structureAPI;
    private Monitor monitor;

    public ConstructionZonePlacer(IWorld world) {
        this.accessType = AccessType.PRIVATE;
        this.structureAPI = StructureAPI.getInstance();
        this.monitor = structureAPI.getMonitor(world.getName());
    }

    @Override
    public IConstructionZonePlacer setAccess(AccessType accessType) {
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
                
                // Add zone to world
                IStructureWorldRepository structureWorldRepository = new StructureWorldRepository(graph);
                StructureWorld structureWorld = structureWorldRepository.addOrGet(world.getName(), world.getUUID());
                structureWorld.addZone(zone);
                
                
                // Add owners
                ISettlerRepository settlerRepository = new SettlerRepositiory(graph);
                Set<UUID> added = Sets.newHashSet();
                // Add masters
                for(UUID player : getMasters()) {
                    ISettler settler = settlerRepository.findByUUID(player);
                    zone.getOwnerDomain().setOwnership(settler, OwnerType.MASTER);
                    added.add(player);
                }
                
                // Add owners
                for(UUID player : getOwners()) {
                    if(!added.contains(player)) {
                        ISettler settler = settlerRepository.findByUUID(player);
                        zone.getOwnerDomain().setOwnership(settler, OwnerType.OWNER);
                        added.add(player);
                    }
                }
                
                // Add members
                for(UUID player : getMembers()) {
                    if(!added.contains(player)) {
                        ISettler settler = settlerRepository.findByUUID(player);
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
        return placeResult;
    }

}
