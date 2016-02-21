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

import com.chingo247.structureapi.StructureAPI;
import com.chingo247.structureapi.event.structure.StructureCreateEvent;
import com.chingo247.structureapi.event.structure.StructureRemoveEvent;
import com.chingo247.structureapi.event.structure.owner.StructureAddOwnerEvent;
import com.chingo247.structureapi.event.structure.owner.StructureRemoveOwnerEvent;
import com.chingo247.structureapi.event.zone.ConstructionZoneRemoveOwnerEvent;
import com.chingo247.structureapi.event.zone.ConstructionZoneUpdateOwnerEvent;
import com.chingo247.structureapi.event.zone.ConstructionZoneCreateEvent;
import com.chingo247.structureapi.event.zone.ConstructionZoneEventDelete;
import com.chingo247.structureapi.model.owner.OwnerType;
import com.chingo247.structureapi.model.structure.IStructure;
import com.chingo247.structureapi.model.zone.IConstructionZone;
import com.chingo247.structureapi.platform.ConfigProvider;
import com.google.common.eventbus.Subscribe;
import java.util.UUID;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 *
 * @author Chingo
 */
public class WorldGuardPlotListener {

    private final WorldGuardHelper worldGuardHelper;

    public WorldGuardPlotListener(WorldGuardHelper worldGuardHelper, GraphDatabaseService graph) {
        this.worldGuardHelper = worldGuardHelper;
    }

    @Subscribe
    public void onStructureCreate(StructureCreateEvent structureCreateEvent) {
        IStructure structure = structureCreateEvent.getStructure();
        ConfigProvider configProvider = StructureAPI.getInstance().getConfig();
        if(configProvider.isProtectStructures()) {
             worldGuardHelper.protect(structure);
        }
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
        if (type == OwnerType.MEMBER) {
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
        if (type == OwnerType.MEMBER) {
            worldGuardHelper.removeMember(player, structure);
        } else {
            worldGuardHelper.removeOwner(player, structure);
        }
    }

    @Subscribe
    public void onConstructionZoneCreate(ConstructionZoneCreateEvent constructionZoneEvent) {
        IConstructionZone zone = constructionZoneEvent.getZone();
        worldGuardHelper.protect(zone);
    }

    @Subscribe
    public void onConstructionZoneRemove(ConstructionZoneEventDelete deleteConstructionZoneEvent) {
        IConstructionZone zone = deleteConstructionZoneEvent.getZone();
        worldGuardHelper.removeProtection(zone);
    }

    @Subscribe
    public void onConstructionZoneAddOwner(ConstructionZoneUpdateOwnerEvent constructionZoneUpdateOwnerEvent) {
        final UUID player = constructionZoneUpdateOwnerEvent.getPlayer();
        final OwnerType type = constructionZoneUpdateOwnerEvent.getOwnerType();
        final IConstructionZone zone = constructionZoneUpdateOwnerEvent.getZone();
        if (type == OwnerType.MEMBER) {
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
        if (type == OwnerType.MEMBER) {
            worldGuardHelper.removeMember(player, zone);
        } else {
            worldGuardHelper.removeOwner(player, zone);
        }
    }

}
