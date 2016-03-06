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
package com.chingo247.structureapi.placing;

import com.chingo247.settlercraft.core.Direction;
import com.chingo247.settlercraft.core.SettlerCraft;
import com.chingo247.settlercraft.core.concurrent.KeyPool;
import com.chingo247.settlercraft.core.platforms.services.IEconomyProvider;
import com.chingo247.structureapi.plan.IStructurePlan;
import com.chingo247.structureapi.plan.StructurePlanManager;
import com.chingo247.structureapi.IStructureAPI;
import com.chingo247.structureapi.StructurePlacerFactory;
import com.chingo247.structureapi.construction.contract.BuildContract;
import com.chingo247.structureapi.construction.contract.SafeContract;
import com.chingo247.structureapi.construction.manual.ManualContract;
import static com.chingo247.structureapi.menus.plans.StructurePlanItem.getPlanID;
import static com.chingo247.structureapi.menus.plans.StructurePlanItem.getValue;
import static com.chingo247.structureapi.menus.plans.StructurePlanItem.isStructurePlan;
import com.chingo247.structureapi.model.structure.Structure;
import com.chingo247.structureapi.model.structure.StructureNode;
import com.chingo247.structureapi.model.structure.StructureRepository;
import com.chingo247.structureapi.selection.CUISelectionManager;
import com.chingo247.structureapi.selection.ISelectionManager;
import com.chingo247.structureapi.selection.NoneSelectionManager;
import com.chingo247.structureapi.platform.permission.PermissionManager;
import com.chingo247.structureapi.util.PlacementUtil;
import com.chingo247.structureapi.util.WorldUtil;
import com.chingo247.xplatform.core.AInventory;
import com.chingo247.xplatform.core.AItemStack;
import com.chingo247.xplatform.core.APlatform;
import com.chingo247.xplatform.core.IColors;
import com.chingo247.xplatform.core.IPlayer;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.regions.CuboidRegion;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

/**
 *
 * @author Chingo
 */
public class PlayerPlanPlacer {

    private final KeyPool<UUID> playerPool;
    private final IEconomyProvider economyProvider;
    private final IStructureAPI structureAPI;
    private final IColors color;
    private final APlatform platform;

    public PlayerPlanPlacer(IStructureAPI structureAPI, IEconomyProvider economyProvider) {
        Preconditions.checkNotNull(structureAPI, "structureapi was null");
        
        this.structureAPI = structureAPI;
        this.playerPool = new KeyPool<>(structureAPI.getExecutor());
        this.economyProvider = economyProvider;
        this.platform = structureAPI.getPlatform();
        this.color = platform.getChatColors();
    }

    public void handlePlace(PlayerPlacePlanAction planAction) {
        if (planAction.isClickedAir()) {
            deselect(planAction.getPlayer());
        } else {
            place(planAction.getPlanItem(), planAction.getPlayer(), planAction.getPosition());
        }
    }

    private void deselect(UUID player) {
        if (CUISelectionManager.getInstance().hasSelection(player)) {
            CUISelectionManager.getInstance().deselect(player);
        }
//        if (HologramSelectionManager.getInstance().hasHologramsProvider() && HologramSelectionManager.getInstance().hasSelection(player)) {
//            HologramSelectionManager.getInstance().deselect(player);
//        }
        if (NoneSelectionManager.getInstance().hasSelection(player)) {
            NoneSelectionManager.getInstance().deselect(player);
        }
    }

    private void place(final AItemStack item, final UUID playerUUID, final Vector pos) {
        // Check if this Itemstack is a StructurePlan
        if (!isStructurePlan(item)) {
            return;
        }
        final IPlayer player = SettlerCraft.getInstance().getPlatform().getPlayer(playerUUID);

        // Check if the player has permission to structures
        if (!PermissionManager.getInstance().isAllowed(player, PermissionManager.Perms.STRUCTURE_PLACE)) {
            player.sendMessage(color.red() + "You have no permission to place structures");
            return;
        }

        // Pick a selection type
        Player wePlayer = SettlerCraft.getInstance().getPlayer(player.getUniqueId());
        LocalSession session = WorldEdit.getInstance().getSession(wePlayer);
        final ISelectionManager selectionManager;
        // Set the SelectionManager if null...
        if (session.hasCUISupport()) {
            selectionManager = CUISelectionManager.getInstance();
        } else {
            selectionManager = NoneSelectionManager.getInstance();
        }

        // Dispatch a async task for structure creation at this players queue
        playerPool.execute(player.getUniqueId(), new Runnable() {

            @Override
            public void run() {
                try {
                    // Inventory check
                    AInventory inventory = player.getInventory();
                    if (!inventory.hasItem(item)) {
                        return;
                    }

                    String planId = getPlanID(item);
                    IStructurePlan plan = StructurePlanManager.getInstance().getPlan(planId);

                    // If the plan is null... and this is a plan... REFUND if possible
                    if (plan == null) {
                        if (structureAPI.isLoadingPlans()) {
                            player.sendMessage(color.red() + "Plans are not loaded yet... please wait...");
                            return;
                        }
                        player.sendMessage(color.red() + "The plan has become invalid, reason: data was not found");
                        int amount = item.getAmount();
                        double value = getValue(item);
                        if (economyProvider != null && value > 0.0d) {
                            economyProvider.give(player.getUniqueId(), value * amount);
                            player.sendMessage(color.red() + "Invalid StructurePlans have been removed and you've been refunded: " + (value * amount));
                        } else {
                            player.sendMessage(color.red() + "Removed invalid structure plans from your inventory");
                        }
                        player.getInventory().removeItem(item);

                        // Otherwise, plan is not null... soo place it!
                    } else {

                        Direction direction = WorldUtil.getDirection(player.getYaw());
                        Vector pos1 = pos;
                        Vector pos2;
                        boolean toLeft = player.isSneaking();

                        CuboidRegion selection = plan.getPlacement().getCuboidRegion();
                        
                        if (toLeft) {
                            pos2 = PlacementUtil.getPoint2Left(pos1, direction, selection.getMaximumPoint());
                        } else {
                            pos2 = PlacementUtil.getPoint2Right(pos1, direction, selection.getMaximumPoint());
                        }

                        if (!selectionManager.hasSelection(player.getUniqueId())) {
                            selectionManager.select(player.getUniqueId(), pos1, pos2);
                            if (!(selectionManager instanceof NoneSelectionManager)) {
                                player.sendMessage(color.yellow() + "Left-Click " + color.reset() + " in the " + color.green() + " green " + color.reset() + "square to " + color.yellow() + "confirm");
                                player.sendMessage(color.yellow() + "Right-Click " + color.reset() + "to" + color.yellow() + " deselect");
                            }
                        } else if (selectionManager.matchesCurrentSelection(player.getUniqueId(), pos1, pos2)) { // Means second time so place!

                            // To left means the player was sneaking
                            // This is a shortcut for placing the structure at the left of him
                            if (toLeft) {
                                // Fix WTF HOW?!!1?
                                pos1 = WorldUtil.translateLocation(pos1, direction, (-(selection.getMaximumPoint().getBlockZ() - 1)), 0, 0);
                            }
                            
                            Structure parent = null;
                            if(structureAPI.getConfig().allowsSubstructures()) {
                                GraphDatabaseService graph = structureAPI.getGraphDatabase();
                                try(Transaction tx = graph.beginTx()) {
                                    StructureRepository structureRepository = new StructureRepository(graph);
                                    StructureNode sn = structureRepository.findStructureOnPosition(player.getWorld().getUUID(), pos1);
                                    if(sn != null) {
                                        parent = new Structure(sn);
                                    }
                                    tx.success();
                                }
                            }
                            
                            
                            

                            StructurePlacerFactory placerFactory = structureAPI.getStructurePlacerFactory();
                            IPlaceResult<Structure> placeResult = placerFactory.createPlacer(player.getWorld().getName())
                                    .setPlacer(playerUUID)
                                    .setParent(parent)
                                    .setCheckOwnerRestriction(true)
                                    .setInheritOwnership(true)
                                    .setName(plan.getName())
                                    .setPrice(plan.getPrice())
                                    .place(plan, pos1, direction);
                            
                            if (placeResult.succes()) {
                                // remove item from inventory
                                AItemStack clone = item.clone();
                                clone.setAmount(1);
                                player.getInventory().removeItem(clone);
                                player.updateInventory();
                                // start construction
                                Structure structure = placeResult.getPlaced();
                                if (!structureAPI.isQueueLocked(player.getUniqueId())) {
                                    ManualContract buildContract = new ManualContract();
                                    SafeContract safeContract = new SafeContract(buildContract);
                                    safeContract.setRecursive(true)
                                            .setPlayer(playerUUID)
                                            .setRestrictive(true)
                                            .setRecursive(true);
                                    structureAPI.getContractor().submit(structure, safeContract);
                                    
                                } else {
                                    player.sendMessage(color.red() + "Your AWE queue is locked at the moment, try '/stt:build " + structure.getId() + "' when your queue is unlocked");
                                }
                            } else {
                                player.sendMessage(color.red() + placeResult.error());
                            }
                            deselect(playerUUID);
                        } else {
                            selectionManager.deselect(player.getUniqueId());
                            selectionManager.select(player.getUniqueId(), pos1, pos2);
                            if (!(selectionManager instanceof NoneSelectionManager)) {
                                player.sendMessage(color.yellow() + "Left-Click " + color.reset() + " in the " + color.green() + " green " + color.reset() + "square to " + color.yellow() + "confirm");
                                player.sendMessage(color.yellow() + "Right-Click " + color.reset() + "to" + color.yellow() + " deselect");
                            }
                        }

                    }
                } catch (Exception ex) { // Log the exception HERE, otherwise it will appear nowhere
                    player.sendMessage(color.red() + "[SettlerCraft-StructureAPI]: An error occured... see console");
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
                }
            }
        });

    }

}
