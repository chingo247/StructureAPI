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
package com.chingo247.structureapi.construction.engine;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.world.World;
import java.util.UUID;

/**
 *
 * @author Chingo
 */
public abstract class BlockPlaceSession {
    
    private final UUID sessionId;
    private World world;

    public BlockPlaceSession(World world) {
        this.world = world;
        this.sessionId = UUID.randomUUID();
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public World getWorld() {
        return world;
    }
    
    public abstract void setBlock(int x, int y, int z, int blockId, int data);
    
    public void setBlock(Vector pos, BaseBlock b) {
        setBlock(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ(), b.getId(), b.getData());
    }
    
    public abstract void submit();
    
    
}
