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
package com.chingo247.structureapi.construction;

import com.chingo247.settlercraft.core.SettlerCraft;
import com.chingo247.structureapi.StructureAPI;
import com.chingo247.structureapi.model.structure.Structure;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.world.World;
import java.util.UUID;
import org.primesoft.asyncworldedit.worldedit.AsyncEditSession;

/**
 *
 * @author Chingo
 */
public class DefaultStructureEditSessionFactory implements StructureEditSessionFactory {

    @Override
    public AsyncEditSession createEditSession(Structure structure, UUID submitter) {
        World world = SettlerCraft.getInstance().getWorld(structure.getWorldName());
        Player player = SettlerCraft.getInstance().getPlayer(submitter);
        
        AsyncEditSession editSession = null;
        if(submitter != null) {
            editSession = (AsyncEditSession)StructureAPI.getInstance().getSessionFactory().getEditSession(world, -1, player);
        } else {
            editSession = (AsyncEditSession) StructureAPI.getInstance().getSessionFactory().getEditSession(world, -1);
        }
        return editSession;
    }
    
}
