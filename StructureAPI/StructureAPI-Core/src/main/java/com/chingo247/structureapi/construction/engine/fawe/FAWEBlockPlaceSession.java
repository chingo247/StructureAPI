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
package com.chingo247.structureapi.construction.engine.fawe;

import com.boydti.fawe.FaweAPI;
import com.boydti.fawe.object.FaweChunk;
import com.chingo247.structureapi.construction.engine.BlockPlaceSession;
import com.sk89q.worldedit.world.World;
import java.util.Map;

/**
 *
 * @author Chingo
 */
public class FAWEBlockPlaceSession extends BlockPlaceSession {
    
    private static final int CHUNK_SIZE = 16;

    private Map<String,FaweChunk> chunks;
    
    public FAWEBlockPlaceSession(World world) {
        super(world);
    }
    
    private FaweChunk getChunk(int x, int z) {
        int chunkX = x << 4;
        int chunkZ = z << 4;
        
        FaweChunk chunk = chunks.get(chunkX + "-" + chunkZ);
        if(chunk == null) {
            chunk = FaweAPI.createChunk();
            chunks.put(chunkX + "-" + chunkZ, chunk);
        }
        return chunk;
    }

    @Override
    public void setBlock(int x, int y, int z, int blockId, int data) {
//        int chunkX = x << 4;
//        int chunkZ = z << 4;
//        
//        synchronized(this) {
//            FaweChunk chunk = getChunk(x, z);
//            chunk.setBlock(x - (chunkX * CHUNK_SIZE), y, z - (chunkZ * CHUNK_SIZE),  blockId, (byte) data);
//        }
        FaweAPI.setBlockAsync(getWorld().getName(), x, y, z, (short)blockId, (byte) data);
        
    }

    @Override
    public void submit() {
        
        FaweAPI.addTask(new Runnable() {

            @Override
            public void run() {
                System.out.println("COMPLETED");
            }
        });
//        if(chunks != null) {
//            synchronized(this) {
//                for (Iterator<FaweChunk> iterator = chunks.values().iterator(); iterator.hasNext();) {
//                    FaweChunk next = iterator.next();
//                    FaweAPI.setChunkAsync(next, Bukkit.getWorld(getWorld().getName()).getChunkAt(next.ge, CHUNK_SIZE));
//                }
//            }
//            chunks.clear();
//        }
    }
    
    
    
    
}
