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
package com.chingo247.structurecraft.event.async;

import com.chingo247.settlercraft.core.event.async.AsyncEventManager;
import com.chingo247.structurecraft.model.structure.IStructure;
import com.chingo247.structurecraft.model.structure.Structure;
import org.primesoft.asyncworldedit.playerManager.PlayerEntry;

/**
 * Fired when a structure-construction operation is added to the AsyncWorldEdit blockplacer Queue.
 * Note that this event is fired Async! Therefore only available for subscribers registered at {@link AsyncEventManager}
 * @author Chingo
 */
public class StructureJobAddedEvent extends StructureJobEvent {
    
    private final PlayerEntry playerEntry;
    
    public StructureJobAddedEvent(IStructure structure, int jobid, PlayerEntry playerUUID) {
        super(structure, jobid);
        this.playerEntry = playerUUID;
    }

    public PlayerEntry getPlayerEntry() {
        return playerEntry;
    }

    
}
