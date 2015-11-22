/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft;

import com.chingo247.menuapi.menu.util.ShopUtil;
import com.chingo247.settlercraft.core.concurrent.KeyPool;
import com.chingo247.settlercraft.core.platforms.services.IEconomyProvider;
import com.chingo247.structurecraft.event.StructureStateChangeEvent;
import com.chingo247.structurecraft.event.structure.StructureBuildCompleteEvent;
import com.chingo247.structurecraft.event.structure.StructureBuildingEvent;
import com.chingo247.structurecraft.event.structure.StructureConstructionQueued;
import com.chingo247.structurecraft.event.structure.StructureDemolishingEvent;
import com.chingo247.structurecraft.event.structure.StructureDemolitionCompleteEvent;
import com.chingo247.structurecraft.model.owner.OwnerDomainNode;
import com.chingo247.structurecraft.model.owner.OwnerType;
import com.chingo247.structurecraft.model.owner.Ownership;
import com.chingo247.structurecraft.model.settler.SettlerNode;
import com.chingo247.structurecraft.model.structure.ConstructionStatus;
import static com.chingo247.structurecraft.model.structure.ConstructionStatus.BUILDING;
import static com.chingo247.structurecraft.model.structure.ConstructionStatus.DEMOLISHING;
import static com.chingo247.structurecraft.model.structure.ConstructionStatus.QUEUED;
import static com.chingo247.structurecraft.model.structure.ConstructionStatus.REMOVED;
import static com.chingo247.structurecraft.model.structure.ConstructionStatus.STOPPED;
import com.chingo247.structurecraft.model.structure.IStructure;
import com.chingo247.structurecraft.model.structure.StructureNode;
import com.chingo247.xplatform.core.APlatform;
import com.chingo247.xplatform.core.IColors;
import com.chingo247.xplatform.core.IPlayer;
import com.google.common.collect.Lists;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

/**
 *
 * @author Chingo
 */
class StructureEventListener {

    private static final Logger LOG = Logger.getLogger(StructureEventListener.class.getName());
    private final IEconomyProvider economyProvider;
    private final IStructureAPI structureAPI;
    private final KeyPool<Long> structureEventPool;

    public StructureEventListener(IEconomyProvider economyProvider, IStructureAPI structureAPI) {
        this.economyProvider = economyProvider;
        this.structureAPI = structureAPI;
        this.structureEventPool = new KeyPool<>(structureAPI.getExecutor());
    }

    private Iterable<IPlayer> getPlayers(IStructure structure) {
        List<IPlayer> players = Lists.newArrayList();
        OwnerDomainNode ownerDomainNode = new StructureNode(structure.getUnderlyingNode()).getOwnerDomain();
        APlatform platform = structureAPI.getPlatform();
        for (Ownership ownership : ownerDomainNode.getOwnerships()) {
            SettlerNode settlerNode = ownership.getOwner();
            IPlayer player = platform.getPlayer(settlerNode.getUniqueId());
            if (player != null) {
                players.add(player);
            }
        }
        return players;
    }

    private void refund(IStructure structure) {
        StructureNode structureNode = new StructureNode(structure.getUnderlyingNode());
        double price = structureNode.getPrice();
        if (economyProvider != null && price > 0) {
            List<SettlerNode> settlers = structureNode.getOwnerDomain().getOwners(OwnerType.MASTER);
            double refundValue = price / settlers.size();
            if (!settlers.isEmpty()) {
                APlatform platform = structureAPI.getPlatform();
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
    }

    private String getStructureString(IStructure structure) {
        IColors colors = structureAPI.getPlatform().getChatColors();
        return "#" + colors.gold() + structure.getId() + colors.blue() + " " + structure.getName();
    }

    private void handleStructure(final IStructure structure, final String message, final ConstructionStatus newStatus) {
        structureEventPool.execute(structure.getId(), new Runnable() {

            @Override
            public void run() {
                try {
                    Transaction tx = null;
                    GraphDatabaseService graph = structureAPI.getGraphDatabase();
                    Iterable<IPlayer> owners = null;
                    ConstructionStatus oldStatus = structure.getStatus();
                    try {
                        tx = graph.beginTx();
                        owners = getPlayers(structure);
                        StructureNode structureNode = new StructureNode(structure.getUnderlyingNode());
                        structureNode.setStatus(newStatus);
                        if (newStatus == REMOVED) {
                            refund(structure);
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
                    
                    if(oldStatus != newStatus) {
                        structureAPI.getEventDispatcher().post(new StructureStateChangeEvent(structure));
                    }
                    
                    boolean shouldTell = (oldStatus != newStatus);
                    if(newStatus == STOPPED && (oldStatus == ConstructionStatus.ON_HOLD || oldStatus == STOPPED)) {
                        shouldTell = false;
                    }
                    
                    if(shouldTell) {
                        // Tell the starter
                        for (IPlayer p : owners) {
                            p.sendMessage(message);
                        }
                    }
                } catch (Exception ex) {
                    LOG.log(Level.SEVERE, ex.getMessage(), ex);
                }
            }
        });

    }

    @AllowConcurrentEvents
    @Subscribe
    public void onStructureBuildComplete(StructureBuildCompleteEvent bce) {
        IColors colors = structureAPI.getPlatform().getChatColors();
        IStructure structure = bce.getStructure();
        String message = colors.green() + "COMPLETE " + colors.reset() + getStructureString(structure);
        handleStructure(bce.getStructure(), message, ConstructionStatus.COMPLETED);
    }

    @AllowConcurrentEvents
    @Subscribe
    public void onStructureBuilding(StructureBuildingEvent sbe) {
        IColors colors = structureAPI.getPlatform().getChatColors();
        IStructure structure = sbe.getStructure();
        String message = colors.yellow() + "BUILDING " + colors.reset() + getStructureString(structure);
        handleStructure(structure, message, BUILDING);
    }

    @AllowConcurrentEvents
    @Subscribe
    public void onStructureDemolitionComplete(StructureDemolitionCompleteEvent dce) {
        IColors colors = structureAPI.getPlatform().getChatColors();
        IStructure structure = dce.getStructure();
        String message = colors.red() + "REMOVED " + colors.reset() + getStructureString(structure);
        handleStructure(structure, message, REMOVED);
    }

    @AllowConcurrentEvents
    @Subscribe
    public void onStructureDemolition(StructureDemolishingEvent sde) {
        IColors colors = structureAPI.getPlatform().getChatColors();
        IStructure structure = sde.getStructure();
        String message = colors.yellow() + "DEMOLISHING " + colors.reset() + getStructureString(structure);
        handleStructure(structure, message, DEMOLISHING);
    }

    @AllowConcurrentEvents
    @Subscribe
    public void onStructureConstructionQueued(StructureConstructionQueued scq) {
        IColors colors = structureAPI.getPlatform().getChatColors();
        IStructure structure = scq.getStructure();
        String message = colors.yellow() + "QUEUED " + colors.reset() + getStructureString(structure);
        handleStructure(structure, message, QUEUED);
    }

    @AllowConcurrentEvents
    @Subscribe
    public void onStructureConstructionCancelled(StructureConstructionQueued scq) {
        IColors colors = structureAPI.getPlatform().getChatColors();
        IStructure structure = scq.getStructure();
        String message = colors.red() + "STOPPED " + colors.reset() + getStructureString(structure);
        handleStructure(structure, message, STOPPED);
    }

}
