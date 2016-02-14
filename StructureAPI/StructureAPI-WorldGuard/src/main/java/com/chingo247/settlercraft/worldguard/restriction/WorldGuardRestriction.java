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
package com.chingo247.settlercraft.worldguard.restriction;

import com.chingo247.settlercraft.worldguard.protecttion.SettlerCraftWGService;
import com.chingo247.structureapi.StructureRestriction;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import org.bukkit.Bukkit;

/**
 *
 * @author Chingo
 */
public class WorldGuardRestriction extends StructureRestriction {

    public WorldGuardRestriction() {
        super("worldguard", "worldguard.region.overlap", "Structure overlaps a worldguard region you don't own");
    }

    @Override
    public boolean evaluate(Player whoPlaces, World world, CuboidRegion affectedArea) {
        LocalPlayer localPlayer = null;
        if (whoPlaces != null) {
            localPlayer = SettlerCraftWGService.getLocalPlayer(Bukkit.getPlayer(whoPlaces.getUniqueId()));
        }

        RegionManager mgr = SettlerCraftWGService.getRegionManager(Bukkit.getWorld(world.getName()));

        Vector p1 = affectedArea.getMinimumPoint();
        Vector p2 = affectedArea.getMaximumPoint();
        ProtectedCuboidRegion dummy = new ProtectedCuboidRegion("DUMMY", new BlockVector(p1.getBlockX(), p1.getBlockY(), p1.getBlockZ()), new BlockVector(p2.getBlockX(), p2.getBlockY(), p2.getBlockZ()));
        ApplicableRegionSet regions = mgr.getApplicableRegions(dummy);
        

        // Check if this region getOverlapping any other region
        if (regions.size() > 0) {
            if (localPlayer == null) {
                return false;
            }

            if (!regions.isOwnerOfAll(localPlayer)) {
                return false;
            }
        }
        return true;
    }
    
}
