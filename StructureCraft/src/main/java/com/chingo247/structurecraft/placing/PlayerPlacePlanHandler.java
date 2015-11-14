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
package com.chingo247.structurecraft.placing;

import com.chingo247.settlercraft.core.Direction;
import com.chingo247.settlercraft.core.SettlerCraft;
import com.chingo247.settlercraft.core.concurrent.KeyPool;
import com.chingo247.settlercraft.core.platforms.services.IEconomyProvider;
import com.chingo247.structurecraft.model.world.StructureWorldRepository;
import com.chingo247.structurecraft.model.world.IStructureWorldRepository;
import com.chingo247.structurecraft.plan.interfaces.IStructurePlan;
import com.chingo247.structurecraft.plan.StructurePlanManager;
import com.chingo247.structurecraft.construction.options.BuildOptions;
import com.chingo247.structurecraft.model.structure.IStructureRepository;
import com.chingo247.structurecraft.model.structure.StructureRepository;
import com.chingo247.structurecraft.IStructureAPI;
import com.chingo247.structurecraft.StructureAPI;
import static com.chingo247.structurecraft.menu.StructurePlanItem.getPlanID;
import static com.chingo247.structurecraft.menu.StructurePlanItem.getValue;
import static com.chingo247.structurecraft.menu.StructurePlanItem.isStructurePlan;
import com.chingo247.structurecraft.model.structure.IStructure;
import com.chingo247.structurecraft.selection.CUISelectionManager;
import com.chingo247.structurecraft.selection.ISelectionManager;
import com.chingo247.structurecraft.selection.NoneSelectionManager;
import com.chingo247.structurecraft.platform.permission.PermissionManager;
import com.chingo247.structurecraft.placing.structure.IStructurePlaceResult;
import com.chingo247.structurecraft.placing.structure.IStructurePlacerFactory;
import com.chingo247.structurecraft.util.PlacementUtil;
import com.chingo247.structurecraft.util.WorldUtil;
import com.chingo247.xplatform.core.AInventory;
import com.chingo247.xplatform.core.AItemStack;
import com.chingo247.xplatform.core.APlatform;
import com.chingo247.xplatform.core.IColors;
import com.chingo247.xplatform.core.IPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.entity.Player;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 *
 * @author Chingo
 */
public class PlayerPlacePlanHandler {

    private final KeyPool<UUID> playerPool;
    private final IEconomyProvider economyProvider;
    private final IStructureAPI structureAPI;
    private final IColors color;
    private final IStructureWorldRepository structureWorldRepository;
    private final IStructureRepository structureRepository;
    private final APlatform platform;

    public PlayerPlacePlanHandler(IEconomyProvider economyProvider) {
        this.playerPool = new KeyPool<>(SettlerCraft.getInstance().getExecutor());
        this.economyProvider = economyProvider;
        this.structureAPI = StructureAPI.getInstance();
        this.platform = structureAPI.getPlatform();
        this.color = platform.getChatColors();

        // Setup repositories
        GraphDatabaseService graph = SettlerCraft.getInstance().getNeo4j();
        this.structureWorldRepository = new StructureWorldRepository(graph);
        this.structureRepository = new StructureRepository(graph);
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
        if (!PermissionManager.getInstance().isAllowed(player, PermissionManager.Perms.SETTLER_STRUCTURE_PLACE)) {
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
                        if (structureAPI.isLoading()) {
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

                        if (toLeft) {
                            pos2 = PlacementUtil.getPoint2Left(pos1, direction, plan.getPlacement().getCuboidRegion().getMaximumPoint());
                        } else {
                            pos2 = PlacementUtil.getPoint2Right(pos1, direction, plan.getPlacement().getCuboidRegion().getMaximumPoint());
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
                                pos1 = WorldUtil.translateLocation(pos1, direction, (-(plan.getPlacement().getCuboidRegion().getMaximumPoint().getBlockZ() - 1)), 0, 0);
                            }

                            IStructurePlacerFactory placerFactory = structureAPI.getStructurePlacerFactory();
                            IStructurePlaceResult placeResult = placerFactory.createPlacer(player.getWorld().getName())
                                    .setPlacer(playerUUID)
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
                                IStructure structure = placeResult.getPlacedStructure();
                                if (!structureAPI.isQueueLocked(player.getUniqueId())) {
                                    structureAPI.getConstructionManager().build(player.getUniqueId(), structure, new BuildOptions());
                                } else {
                                    player.sendMessage(color.red() + "Your AWE queue is locked at the moment, try '/stt:build " + structure.getId() + "' when your queue is unlocked");
                                }
                            } else {
                                player.sendMessage(placeResult.error());
                            }

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
