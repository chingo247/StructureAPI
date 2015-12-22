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
package com.chingo247.structurecraft.construction.plan;

import com.chingo247.menuapi.menu.util.ShopUtil;
import com.chingo247.settlercraft.core.SettlerCraft;
import com.chingo247.settlercraft.core.concurrent.KeyPool;
import com.chingo247.settlercraft.core.platforms.services.IEconomyProvider;
import com.chingo247.structurecraft.StructureAPI;
import com.chingo247.structurecraft.construction.IConstructionEntry;
import com.chingo247.structurecraft.construction.IConstructionExecutor;
import com.chingo247.structurecraft.construction.ITaskAssigner;
import com.chingo247.structurecraft.StructureScheduler;
import com.chingo247.structurecraft.model.owner.OwnerDomainNode;
import com.chingo247.structurecraft.model.owner.OwnerType;
import com.chingo247.structurecraft.model.owner.Ownership;
import com.chingo247.structurecraft.model.settler.SettlerNode;
import com.chingo247.structurecraft.model.structure.ConstructionStatus;
import com.chingo247.structurecraft.placement.options.Traversal;
import com.chingo247.structurecraft.model.structure.IStructure;
import com.chingo247.structurecraft.model.structure.Structure;
import com.chingo247.structurecraft.model.structure.StructureNode;
import com.chingo247.structurecraft.placement.IPlacement;
import com.chingo247.xplatform.core.APlatform;
import com.chingo247.xplatform.core.IColors;
import com.chingo247.xplatform.core.IPlayer;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.primesoft.asyncworldedit.worldedit.AsyncEditSession;

/**
 *
 * @author Chingo
 */
public abstract class ConstructionPlan implements IConstructionPlan {
    
    private static final Logger LOG = Logger.getLogger(ConstructionPlan.class.getName());
    private double reportableProgress;
    private final IConstructionExecutor executor;
    private final ITaskAssigner assigner;
    private boolean recursive, restrictive, reversed;
    private UUID player;
    private AsyncEditSession editSession;
    private Traversal traveral;
    private boolean useForce;
    protected final IStructure structure;
    
    public ConstructionPlan(IConstructionExecutor executor, IStructure structure, ITaskAssigner assigner) {
        this.structure = structure;
        this.executor = executor;
        this.assigner = assigner;
        this.reportableProgress = 10.0;
    }

    /**
     * Set the progress that will be reported. Value must be greater than 0 and smaller or equal to 100
     * @param reportableProgress The min progress that has to be made before it gets reported to the player
     */
    public void setReportableProgress(double reportableProgress) {
        Preconditions.checkArgument(reportableProgress > 0, "Reportableprogress must be greater than zero");
        Preconditions.checkArgument(reportableProgress <= 100, "Reportableprogress must be smaller thatn 100");
        this.reportableProgress = reportableProgress;
    }

    public double getReportableProgress() {
        return reportableProgress;
    }
    
    @Override
    public abstract IPlacement getPlacement(IStructure structure) throws Exception;
    
    public abstract void register(IConstructionEntry entry) throws Exception;

    @Override
    public boolean isForced() {
        return useForce;
    }

    public ConstructionPlan setStructureTraversal(Traversal traversal) {
        this.traveral = traversal;
        return this;
    }

    public ConstructionPlan setForced(boolean useForce) {
        this.useForce = useForce;
        return this;
    }
    
    public ITaskAssigner getAssigner() {
        return assigner;
    }

    public AsyncEditSession getEditSession() {
        return editSession;
    }

    public UUID getPlayer() {
        return player;
    }

    public IStructure getStructure() {
        return structure;
    }
    
    public boolean isRecursive() {
        return recursive;
    }

    public boolean isRestrictive() {
        return restrictive;
    }

    
    public boolean isReversed() {
        return reversed;
    }
    
    
    public ConstructionPlan setRestrictive(boolean restrictive) {
        this.restrictive = restrictive;
        return this;
    }

    
    public ConstructionPlan setRecursive(boolean recursive) {
        this.recursive = recursive;
        return this;
    }

    
    public ConstructionPlan setReversedOrder(boolean reversed) {
        this.reversed = reversed;
        return this;
    }

    
    public ConstructionPlan setPlayer(UUID player) {
        this.player = player;
        return this;
    }
    
    

    
    public ConstructionPlan setEditsession(AsyncEditSession aes) {
        this.editSession = aes;
        return this;
    }
    
    
    public void execute() {
        executor.execute(this);
    }

    
    public Traversal getStructureTraversal() {
        return traveral;
    }
    
    
    protected void handleEntry(final IConstructionEntry entry, final ConstructionStatus newStatus, final boolean isProgressUpdate, final String... messages) {
        StructureScheduler.getInstance().submit(entry.getStructure().getId(), new Runnable() {
            @Override
            public void run() {
                try {
                    Transaction tx = null;
                    GraphDatabaseService graph = StructureAPI.getInstance().getGraphDatabase();
                    Iterable<IPlayer> owners = null;
                    ConstructionStatus oldStatus = entry.getStructure().getStatus();
                    try {
                        tx = graph.beginTx();
                        owners = getPlayers(entry.getStructure());
                        StructureNode structureNode = new StructureNode(entry.getStructure().getUnderlyingNode());
                        structureNode.setStatus(newStatus);
                        if (newStatus == ConstructionStatus.REMOVED) {
                            refund(entry.getStructure());
                        }
                        Structure structure = new Structure(structureNode);
                        entry.update(structure);
                        
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
                    boolean shouldTell = (oldStatus != newStatus) || isProgressUpdate;
                    
                    if (shouldTell) {
                        // Tell the starter
                        for (IPlayer p : owners) {
                            p.sendMessage(messages);
                        }
                    }
                } catch (Exception ex) {
                    LOG.log(Level.SEVERE, ex.getMessage(), ex);
                }
            }
        });
    }

    protected void refund(IStructure structure) {
        StructureNode structureNode = new StructureNode(structure.getUnderlyingNode());
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
    }

    protected Iterable<IPlayer> getPlayers(IStructure structure) {
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

    protected String getStructureString(IStructure structure) {
        IColors colors = StructureAPI.getInstance().getPlatform().getChatColors();
        return "#" + colors.gold() + structure.getId() + colors.blue() + " " + structure.getName();
    }

    
    
}
