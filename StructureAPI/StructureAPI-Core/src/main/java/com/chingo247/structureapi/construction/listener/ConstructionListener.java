/*
 * Copyright (C) 2016 Chingo
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
package com.chingo247.structureapi.construction.listener;

import com.chingo247.menuapi.menu.util.ShopUtil;
import com.chingo247.settlercraft.core.SettlerCraft;
import com.chingo247.settlercraft.core.model.settler.SettlerNode;
import com.chingo247.settlercraft.core.platforms.services.IEconomyProvider;
import com.chingo247.structureapi.StructureAPI;
import com.chingo247.structureapi.StructureScheduler;
import com.chingo247.structureapi.construction.StructureEntry;
import com.chingo247.structureapi.event.structure.StructureRemoveEvent;
import com.chingo247.structureapi.event.structure.StructureStateChangeEvent;
import com.chingo247.structureapi.model.owner.OwnerDomainNode;
import com.chingo247.structureapi.model.owner.OwnerType;
import com.chingo247.structureapi.model.owner.Ownership;
import com.chingo247.structureapi.model.structure.ConstructionStatus;
import com.chingo247.structureapi.model.structure.Structure;
import com.chingo247.structureapi.model.structure.StructureNode;
import com.chingo247.xplatform.core.APlatform;
import com.chingo247.xplatform.core.IColors;
import com.chingo247.xplatform.core.IPlayer;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

/**
 *
 * @author Chingo
 */
public abstract class ConstructionListener implements com.chingo247.structureapi.construction.listener.IConstructionListener {

    private static final Logger LOG = Logger.getLogger(ConstructionListener.class.getName());

    protected void handleEntry(final StructureEntry entry, final ConstructionStatus newStatus, final boolean isProgressUpdate, final String... messages) {
        StructureScheduler.getInstance().submit(entry.getStructure().getId(), new Runnable() {
            @Override
            public void run() {
                try {
                    boolean stateChanged = false;
                    Transaction tx = null;
                    GraphDatabaseService graph = StructureAPI.getInstance().getGraphDatabase();
                    Iterable<IPlayer> owners = null;
                    ConstructionStatus oldStatus = entry.getStructure().getStatus();
                    
                    
                    if (oldStatus != newStatus || isProgressUpdate) {
                        try {
                            tx = graph.beginTx();
                            owners = getPlayers(entry.getStructure());

                            
                            
                            if (oldStatus != newStatus) {
                                StructureNode structureNode = new StructureNode(entry.getStructure().getUnderlyingNode());
                                structureNode.setStatus(newStatus);
                                if (newStatus == ConstructionStatus.REMOVED) {
                                    refund(structureNode, true, true);
                                    
                                }
                                
                                stateChanged = true;
                                Structure structure = new Structure(structureNode);
                                entry.update(structure);
                            }
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
                        }
                        // Tell the starter
                        for (IPlayer p : owners) {
                            p.sendMessage(messages);
                        }

                    }
                    
                    
                    if(newStatus == ConstructionStatus.REMOVED) {
                        StructureAPI.getInstance().getEventDispatcher().dispatchEvent(new StructureRemoveEvent(entry.getStructure()));
                    }
                    
                    if(stateChanged) {
                        StructureAPI.getInstance().getEventDispatcher().dispatchEvent(new StructureStateChangeEvent(entry.getStructure(), newStatus));
                    }

                } catch (Exception ex) {
                    LOG.log(Level.SEVERE, ex.getMessage(), ex);
                }
            }
        });
    }

    protected void refund(StructureNode structureNode, boolean remove, boolean recursive) {
        double price = structureNode.getPrice();
        IEconomyProvider economyProvider = SettlerCraft.getInstance().getEconomyProvider();
        if (economyProvider != null && price > 0) {
            List<SettlerNode> settlers = structureNode.getOwnerDomain().getOwners(OwnerType.MASTER);
            double refundValue = price / settlers.size();
            if (!settlers.isEmpty()) {
                APlatform platform = StructureAPI.getInstance().getPlatform();
                IColors colors = platform.getChatColors();

                for (SettlerNode settler : settlers) {
                    IPlayer player = platform.getPlayer(settler.getUniqueId());
                    if (player != null) {
                        economyProvider.give(player.getUniqueId(), refundValue);
                        double newBalance = economyProvider.getBalance(player.getUniqueId());
                        player.sendMessage("You've been refunded " + colors.gold() + refundValue, "Your new balance is " + colors.gold() + ShopUtil.valueString(newBalance));
                    }
                }
            }
            structureNode.setPrice(0);
        }
        if(remove) {
            structureNode.setStatus(ConstructionStatus.REMOVED);
        }
        if(structureNode.hasSubstructures()) {
            for(StructureNode sn : structureNode.getSubstructures()) {
                refund(sn, remove, recursive);
            }
        }
        
        
    }

    protected Iterable<IPlayer> getPlayers(Structure structure) {
        List<IPlayer> players = Lists.newArrayList();
        OwnerDomainNode ownerDomainNode = new StructureNode(structure.getUnderlyingNode()).getOwnerDomain();
        APlatform platform = StructureAPI.getInstance().getPlatform();
        for (Ownership ownership : ownerDomainNode.getOwnerships()) {
            SettlerNode settlerNode = ownership.getOwner();
            IPlayer player = platform.getPlayer(settlerNode.getUniqueId());
            if (player != null) {
                players.add(player);
            }
        }
        return players;
    }

    protected String getStructureString(Structure structure) {
        IColors colors = StructureAPI.getInstance().getPlatform().getChatColors();
        return "#" + colors.gold() + structure.getId() + colors.blue() + " " + structure.getName();
    }

}
