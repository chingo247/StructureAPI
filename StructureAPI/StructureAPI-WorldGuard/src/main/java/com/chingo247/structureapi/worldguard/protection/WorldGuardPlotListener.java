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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

/**
 *
 * @author Chingo
 */
public class WorldGuardPlotListener {

    private StructureAPIWorldGuardScheduler scheduler;

    public WorldGuardPlotListener() {
        this.scheduler = StructureAPIWorldGuardScheduler.getInstance();
    }

    @Subscribe
    public void onStructureCreate(StructureCreateEvent structureCreateEvent) {
        final Structure structure = structureCreateEvent.getStructure();
        ConfigProvider configProvider = StructureAPI.getInstance().getConfig();

        if (configProvider.isProtectStructures()) {
            scheduler.submit(structure.getWorldName(), new Runnable() {

                @Override
                public void run() {
                    GraphDatabaseService graph = StructureAPI.getInstance().getGraphDatabase();
                    Transaction tx = null;
                    try {
                        tx = graph.beginTx();
                        StructureAPIWorldGuard.getInstance().protect(structure);

                        tx.success();
                    } catch (Exception ex) {
                        if (tx != null) {
                            tx.failure();
                        }
                        Logger.getLogger(WorldGuardPlotListener.class.getName()).log(Level.SEVERE, null, ex);
                    } finally {
                        if (tx != null) {
                            tx.close();
                        }
                    }
                }
            });
        }
    }

    @Subscribe
    public void onStructureRemove(StructureRemoveEvent structureRemoveEvent) {
        final Structure structure = structureRemoveEvent.getStructure();
        scheduler.submit(structure.getWorldName(), new Runnable() {

            @Override
            public void run() {
                GraphDatabaseService graph = StructureAPI.getInstance().getGraphDatabase();
                Transaction tx = null;
                try {
                    tx = graph.beginTx();
                    StructureAPIWorldGuard.getInstance().expire(structure);

                    tx.success();
                } catch (Exception ex) {
                    if (tx != null) {
                        tx.failure();
                    }
                    Logger.getLogger(WorldGuardPlotListener.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    if (tx != null) {
                        tx.close();
                    }
                }
            }
        });
    }

    @Subscribe
    public void onStructureAddOwner(final StructureAddOwnerEvent addOwnerEvent) {
        ConfigProvider configProvider = StructureAPI.getInstance().getConfig();
        final Structure structure = addOwnerEvent.getStructure();
        if (configProvider.isProtectStructures()) {
            scheduler.submit(structure.getWorldName(), new Runnable() {

                @Override
                public void run() {
                    GraphDatabaseService graph = StructureAPI.getInstance().getGraphDatabase();
                    Transaction tx = null;
                    try {
                        tx = graph.beginTx();
                        final UUID player = addOwnerEvent.getAddedOwner();
                        final OwnerType type = addOwnerEvent.getOwnerType();
                        final Structure structure = addOwnerEvent.getStructure();
                        if (type == OwnerType.MEMBER) {
                            StructureAPIWorldGuard.getInstance().addMember(player, structure);
                        } else {
                            StructureAPIWorldGuard.getInstance().removeMember(player, structure);
                            StructureAPIWorldGuard.getInstance().addOwner(player, structure);
                        }

                        tx.success();
                    } catch (Exception ex) {
                        if (tx != null) {
                            tx.failure();
                        }
                        Logger.getLogger(WorldGuardPlotListener.class.getName()).log(Level.SEVERE, null, ex);
                    } finally {
                        if (tx != null) {
                            tx.close();
                        }
                    }
                }
            });
        }
    }

    @Subscribe
    public void onStructureRemoveOwner(final StructureRemoveOwnerEvent removeOwnerEvent) {
        ConfigProvider configProvider = StructureAPI.getInstance().getConfig();
        final Structure structure = removeOwnerEvent.getStructure();
        if (configProvider.isProtectStructures()) {
            scheduler.submit(structure.getWorldName(), new Runnable() {

                @Override
                public void run() {
                    GraphDatabaseService graph = StructureAPI.getInstance().getGraphDatabase();
                    Transaction tx = null;
                    try {
                        tx = graph.beginTx();
                        final UUID player = removeOwnerEvent.getRemovedOwner();
                        final OwnerType type = removeOwnerEvent.getOwnerType();
                        final Structure structure = removeOwnerEvent.getStructure();
                        if (type == OwnerType.MEMBER) {
                            StructureAPIWorldGuard.getInstance().removeMember(player, structure);
                        } else {
                            StructureAPIWorldGuard.getInstance().removeOwner(player, structure);
                        }

                        tx.success();
                    } catch (Exception ex) {
                        if (tx != null) {
                            tx.failure();
                        }
                        Logger.getLogger(WorldGuardPlotListener.class.getName()).log(Level.SEVERE, null, ex);
                    } finally {
                        if (tx != null) {
                            tx.close();
                        }
                    }
                }
            });
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
