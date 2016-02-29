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
package com.chingo247.structureapi.util;

import com.chingo247.settlercraft.core.SettlerCraft;
import com.chingo247.xplatform.core.IPlayer;
import com.chingo247.xplatform.core.IWorld;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.world.World;
import java.util.UUID;

/**
 *
 * @author Chingo
 */
public class WorldEditHelper {
    
    private WorldEditHelper() {}
    
    public static Player getPlayer(IPlayer player) {
        return SettlerCraft.getInstance().getPlayer(player.getUniqueId());
    }
    
    public static Player getPlayer(UUID player) {
        return SettlerCraft.getInstance().getPlayer(player);
    }
    
    public static World getWorld(IWorld world) {
        return SettlerCraft.getInstance().getWorld(world.getUUID());
    }
    
    public static World getWorld(String world) {
        return SettlerCraft.getInstance().getWorld(world);
    }
    
    public static World getWorld(UUID world) {
        return SettlerCraft.getInstance().getWorld(world);
    }
    
}
