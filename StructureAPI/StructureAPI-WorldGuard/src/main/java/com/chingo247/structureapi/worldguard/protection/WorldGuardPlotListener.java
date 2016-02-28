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
import com.chingo247.structureapi.model.owner.OwnerType;
import com.chingo247.structureapi.model.structure.Structure;
import com.chingo247.structureapi.platform.ConfigProvider;
import com.google.common.eventbus.Subscribe;
import java.util.UUID;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 *
 * @author Chingo
 */
public class WorldGuardPlotListener {

    private final WorldGuardProtection worldGuardHelper;

    public WorldGuardPlotListener(WorldGuardProtection worldGuardHelper, GraphDatabaseService graph) {
        this.worldGuardHelper = worldGuardHelper;
    }

    @Subscribe
    public void onStructureCreate(StructureCreateEvent structureCreateEvent) {
        Structure structure = structureCreateEvent.getStructure();
        ConfigProvider configProvider = StructureAPI.getInstance().getConfig();
        if (configProvider.isProtectStructures()) {
            worldGuardHelper.protect(structure);
        }
    }

    @Subscribe
    public void onStructureRemove(StructureRemoveEvent structureRemoveEvent) {
        Structure structure = structureRemoveEvent.getStructure();
        worldGuardHelper.removeProtection(structure, false, false);
    }

    @Subscribe
    public void onStructureAddOwner(StructureAddOwnerEvent addOwnerEvent) {
        ConfigProvider configProvider = StructureAPI.getInstance().getConfig();

        if (configProvider.isProtectStructures()) {
            final UUID player = addOwnerEvent.getAddedOwner();
            final OwnerType type = addOwnerEvent.getOwnerType();
            final Structure structure = addOwnerEvent.getStructure();
            if (type == OwnerType.MEMBER) {
                worldGuardHelper.addMember(player, structure);
            } else {
                worldGuardHelper.removeMember(player, structure);
                worldGuardHelper.addOwner(player, structure);
            }
        }
    }

    @Subscribe
    public void onStructureRemoveOwner(StructureRemoveOwnerEvent removeOwnerEvent) {
        ConfigProvider configProvider = StructureAPI.getInstance().getConfig();

        if (configProvider.isProtectStructures()) {
            final UUID player = removeOwnerEvent.getRemovedOwner();
            final OwnerType type = removeOwnerEvent.getOwnerType();
            final Structure structure = removeOwnerEvent.getStructure();
            if (type == OwnerType.MEMBER) {
                worldGuardHelper.removeMember(player, structure);
            } else {
                worldGuardHelper.removeOwner(player, structure);
            }
        }
    }

//    @Subscribe
//    public void onConstructionZoneCreate(ConstructionZoneCreateEvent constructionZoneEvent) {
//        IConstructionZone zone = constructionZoneEvent.getZone();
//        ConfigProvider configProvider = StructureAPI.getInstance().getConfig();
//        if (configProvider.isProtectConstructionZones()) {
//            worldGuardHelper.protect(zone);
//        }
//    }
//
//    @Subscribe
//    public void onConstructionZoneRemove(ConstructionZoneEventDelete deleteConstructionZoneEvent) {
//        IConstructionZone zone = deleteConstructionZoneEvent.getZone();
//        ConfigProvider configProvider = StructureAPI.getInstance().getConfig();
//        if (configProvider.isProtectConstructionZones()) {
//            worldGuardHelper.removeProtection(zone, false);
//        }
//    }
//
//    @Subscribe
//    public void onConstructionZoneAddOwner(ConstructionZoneUpdateOwnerEvent constructionZoneUpdateOwnerEvent) {
//        ConfigProvider configProvider = StructureAPI.getInstance().getConfig();
//
//        if (configProvider.isProtectConstructionZones()) {
//            final UUID player = constructionZoneUpdateOwnerEvent.getPlayer();
//            final OwnerType type = constructionZoneUpdateOwnerEvent.getOwnerType();
//            final IConstructionZone zone = constructionZoneUpdateOwnerEvent.getZone();
//            if (type == OwnerType.MEMBER) {
//                worldGuardHelper.addMember(player, zone);
//            } else {
//                worldGuardHelper.removeMember(player, zone);
//                worldGuardHelper.addOwner(player, zone);
//            }
//        }
//
//        
//    }
//
//    @Subscribe
//    public void onConstructionZoneRemoveOwner(ConstructionZoneRemoveOwnerEvent removeOwnerEvent) {
//        ConfigProvider configProvider = StructureAPI.getInstance().getConfig();
//        if (configProvider.isProtectConstructionZones()) {
//            final UUID player = removeOwnerEvent.getPlayer();
//            final OwnerType type = removeOwnerEvent.getType();
//            final IConstructionZone zone = removeOwnerEvent.getZone();
//            if (type == OwnerType.MEMBER) {
//                worldGuardHelper.removeMember(player, zone);
//            } else {
//                worldGuardHelper.removeOwner(player, zone);
//            }
//        }
//
//    }

}