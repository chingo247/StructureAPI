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
package com.chingo247.settlercraft.worldguard.protecttion;

import com.chingo247.structureapi.event.structure.StructureCreateEvent;
import com.chingo247.structureapi.event.structure.StructureRemoveEvent;
import com.chingo247.structureapi.event.structure.owner.StructureAddOwnerEvent;
import com.chingo247.structureapi.event.structure.owner.StructureRemoveOwnerEvent;
import com.chingo247.structureapi.event.zone.ConstructionZoneRemoveOwnerEvent;
import com.chingo247.structureapi.event.zone.ConstructionZoneUpdateOwnerEvent;
import com.chingo247.structureapi.event.zone.CreateConstructionZoneEvent;
import com.chingo247.structureapi.event.zone.DeleteConstructionZoneEvent;
import com.chingo247.structureapi.model.owner.OwnerType;
import com.chingo247.structureapi.model.structure.IStructure;
import com.chingo247.structureapi.model.structure.IStructureRepository;
import com.chingo247.structureapi.model.structure.StructureRepository;
import com.chingo247.structureapi.model.zone.IConstructionZone;
import com.google.common.eventbus.Subscribe;
import java.util.UUID;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 *
 * @author Chingo
 */
public class WorldGuardStructureListener {
    
    
    
    private final SettlerCraftWGService worldGuardHelper;
    private final GraphDatabaseService graph;
    private final IStructureRepository structureDAO;

    public WorldGuardStructureListener(SettlerCraftWGService worldGuardHelper, GraphDatabaseService graph) {
        this.worldGuardHelper = worldGuardHelper;
        this.graph = graph;
        this.structureDAO = new StructureRepository(graph);
    }
    
    @Subscribe
    public void onStructureCreate(StructureCreateEvent structureCreateEvent) {
        IStructure structure = structureCreateEvent.getStructure();
        worldGuardHelper.protect(structure);
    }
    
    @Subscribe
    public void onStructureRemove(StructureRemoveEvent structureRemoveEvent) {
        IStructure structure = structureRemoveEvent.getStructure();
        worldGuardHelper.removeProtection(structure);
    }
    
    @Subscribe
    public void onStructureAddOwner(StructureAddOwnerEvent addOwnerEvent) {
        final UUID player = addOwnerEvent.getAddedOwner();
        final OwnerType type = addOwnerEvent.getOwnerType();
        final IStructure structure = addOwnerEvent.getStructure();
        if(type == OwnerType.MEMBER) {
            worldGuardHelper.addMember(player, structure);
        } else {
            worldGuardHelper.removeMember(player, structure);
            worldGuardHelper.addOwner(player, structure);
        }
    }
    
    @Subscribe
    public void onStructureRemoveOwner(StructureRemoveOwnerEvent removeOwnerEvent) {
        final UUID player = removeOwnerEvent.getRemovedOwner();
        final OwnerType type = removeOwnerEvent.getOwnerType();
        final IStructure structure = removeOwnerEvent.getStructure();
        if(type == OwnerType.MEMBER)  {
            worldGuardHelper.removeMember(player, structure);
        } else {
            worldGuardHelper.removeOwner(player, structure);
        }
    }
    
    @Subscribe
    public void onConstructionZoneCreate(CreateConstructionZoneEvent constructionZoneEvent) {
        IConstructionZone zone = constructionZoneEvent.getZone();
        worldGuardHelper.protect(zone);
    }
    
    @Subscribe
    public void onConstructionZoneRemove(DeleteConstructionZoneEvent deleteConstructionZoneEvent) {
        IConstructionZone zone = deleteConstructionZoneEvent.getZone();
        worldGuardHelper.removeProtection(zone);
    }
    
    @Subscribe
    public void onConstructionZoneAddOwner(ConstructionZoneUpdateOwnerEvent constructionZoneUpdateOwnerEvent ){
        final UUID player = constructionZoneUpdateOwnerEvent.getPlayer();
        final OwnerType type = constructionZoneUpdateOwnerEvent.getOwnerType();
        final IConstructionZone zone = constructionZoneUpdateOwnerEvent.getZone();
        if(type == OwnerType.MEMBER) {
            worldGuardHelper.addMember(player, zone);
        } else {
            worldGuardHelper.removeMember(player, zone);
            worldGuardHelper.addOwner(player, zone);
        }
    }
    
    @Subscribe
    public void onConstructionZoneRemoveOwner(ConstructionZoneRemoveOwnerEvent removeOwnerEvent) {
        final UUID player = removeOwnerEvent.getPlayer();
        final OwnerType type = removeOwnerEvent.getType();
        final IConstructionZone zone = removeOwnerEvent.getZone();
        if(type == OwnerType.MEMBER)  {
            worldGuardHelper.removeMember(player, zone);
        } else {
            worldGuardHelper.removeOwner(player, zone);
        }
    }
    
    
    
    
    // TODO ADD CONSTRUCTION ZONES OWNER EVENT LISTENERS!
    
}
