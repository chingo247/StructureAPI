package com.chingo247.structureapi.bukkit.listener;

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
import com.chingo247.xplatform.core.AItemStack;
import com.chingo247.xplatform.platforms.bukkit.BukkitPlatform;
import com.chingo247.settlercraft.core.platforms.services.IEconomyProvider;
import com.chingo247.structureapi.IStructureAPI;
import static com.chingo247.structureapi.menu.StructurePlanItem.isStructurePlan;
import com.chingo247.structureapi.placing.PlayerPlacePlanAction;
import com.chingo247.structureapi.placing.PlayerPlanPlacer;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.BlockVector;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 *
 * @author Chingo
 */
public class PlanListener implements Listener {

    private final PlayerPlanPlacer placeHandler;

    public PlanListener(IStructureAPI structureAPI, IEconomyProvider provider) {
        Preconditions.checkNotNull(structureAPI, "StructureAPI may not be null");
        this.placeHandler = new PlayerPlanPlacer(structureAPI, provider);
    }

    @EventHandler
    public void onPlayerUsePlan(final PlayerInteractEvent pie) {
        if (pie.getItem() == null) {
            return;
        }
        AItemStack stack = BukkitPlatform.wrapItem(pie.getItem());
        if (isStructurePlan(stack)) {
            pie.setCancelled(true);
        }

        String world = pie.getPlayer().getWorld().getName();
        UUID player = pie.getPlayer().getUniqueId();
        Block block = pie.getClickedBlock();
        boolean clickedAir = (block == null || pie.getAction().equals(Action.RIGHT_CLICK_AIR) || pie.getAction().equals(Action.RIGHT_CLICK_BLOCK));
        Location location = pie.getClickedBlock() != null ? pie.getClickedBlock().getLocation() : null;
        
        if(location == null) {
            placeHandler.handlePlace(new PlayerPlacePlanAction(player, world, null, clickedAir, stack));
        } else {
            placeHandler.handlePlace(new PlayerPlacePlanAction(player, world, new BlockVector(location.getBlockX(), location.getBlockY(), location.getBlockZ()), clickedAir, stack));
        }

    }

}
