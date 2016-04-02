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

import com.chingo247.structureapi.construction.awe.AWEJobEntry;
import com.chingo247.structureapi.construction.awe.IAWECallback;
import com.chingo247.structureapi.construction.engine.BlockPlaceSession;
import com.chingo247.xplatform.core.IScheduler;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.blocks.BaseBlock;
import org.primesoft.asyncworldedit.AsyncWorldEditMain;
import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacer;
import org.primesoft.asyncworldedit.blockPlacer.entries.JobEntry;
import org.primesoft.asyncworldedit.playerManager.PlayerEntry;
import org.primesoft.asyncworldedit.utils.WaitFor;
import org.primesoft.asyncworldedit.worldedit.AsyncEditSession;
import org.primesoft.asyncworldedit.worldedit.CancelabeEditSession;

/**
 *
 * @author Chingo
 */
public class AWEBlockPlaceSession extends BlockPlaceSession{
    
    private PlayerEntry playerEntry;
    private AWEJobEntry jobEntry;
    private IBlockPlacer placer;
    private JobEntry job;
    private EditSession jobSession;
    
    
    AWEBlockPlaceSession(PlayerEntry entry, EditSession editSession, IAWECallback callback) {
        super(editSession.getWorld());
        this.playerEntry = entry;
        this.placer = AsyncWorldEditMain.getInstance().getBlockPlacer();
        
        final int jobId = placer.getJobId(entry);
        if (editSession instanceof AsyncEditSession) {
            AsyncEditSession aSession = (AsyncEditSession) editSession;
            jobSession = new CancelabeEditSession(aSession, aSession.getMask(), jobId);
            job = new AWEJobEntry(playerEntry, (CancelabeEditSession) jobSession, jobId, "place", getSessionId(), callback);
        } else {
            jobSession = editSession;
            job = new AWEJobEntry(playerEntry, jobId, "place", getSessionId(), callback);
        }
        placer.addJob(entry, job);
    }

    public JobEntry getJob() {
        return job;
    }

    public PlayerEntry getPlayerEntry() {
        return playerEntry;
    }
    
    @Override
    public void setBlock(int x, int y, int z, int blockId, int data) {
        jobSession.rawSetBlock(new BlockVector(x, y, z), new BaseBlock(blockId, data));
    }

    @Override
    public void submit() {
        if (jobSession instanceof AsyncEditSession) {
            AsyncEditSession aSession = (AsyncEditSession) jobSession;
            aSession.addAsync(job);
        }
        placer.addTasks(playerEntry, job); // TODO Handle if returns false
    }
    
    
    
    
}
