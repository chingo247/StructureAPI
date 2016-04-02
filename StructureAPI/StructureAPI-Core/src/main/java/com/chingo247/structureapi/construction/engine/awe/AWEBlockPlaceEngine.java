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
package com.chingo247.structureapi.construction.engine.awe;

import com.chingo247.settlercraft.core.SettlerCraft;
import com.chingo247.settlercraft.core.model.settler.Settler;
import com.chingo247.structureapi.StructureAPI;
import com.chingo247.structureapi.construction.engine.BlockPlaceEngine;
import com.chingo247.structureapi.construction.engine.BlockPlaceSession;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.world.World;
import java.util.UUID;
import org.primesoft.asyncworldedit.AsyncWorldEditMain;
import org.primesoft.asyncworldedit.playerManager.PlayerEntry;
import org.primesoft.asyncworldedit.worldedit.AsyncEditSessionFactory;

/**
 *
 * @author Chingo
 */
public class AWEBlockPlaceEngine  extends BlockPlaceEngine  {

    private AsyncEditSessionFactory editSessionFactory;
    
    public AWEBlockPlaceEngine() {
    }
    
    
    
    public void setEditSessionFactory(AsyncEditSessionFactory factory) {
        this.editSessionFactory = factory;
    }
    

    @Override
    public BlockPlaceSession getSession(World world, UUID player) {
        Player ply = SettlerCraft.getInstance().getPlayer(player);
        PlayerEntry entry;
        EditSession session;
        if(ply != null) {
            entry = AsyncWorldEditMain.getInstance().getPlayerManager().getPlayer(player);
            session = StructureAPI.getInstance().getEditSessionFactory().getEditSession(world, -1, ply);
        } else {
            entry = PlayerEntry.CONSOLE;
            session = StructureAPI.getInstance().getEditSessionFactory().getEditSession(world, -1);
        }
        return new AWEBlockPlaceSession(entry, session, null);
    }

    
    

    
    
}
