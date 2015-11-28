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
package com.chingo247.structurecraft.construction.awe;

import com.chingo247.structurecraft.construction.options.PlaceOptions;
import com.chingo247.structurecraft.placement.interfaces.IPlacement;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import java.util.UUID;
import org.primesoft.asyncworldedit.api.IAsyncWorldEdit;
import org.primesoft.asyncworldedit.playerManager.PlayerEntry;
import org.primesoft.asyncworldedit.utils.WaitFor;
import org.primesoft.asyncworldedit.worldedit.AsyncEditSession;
import org.primesoft.asyncworldedit.worldedit.CancelabeEditSession;

/**
 *
 * @author Chingo
 */
class AWEPlacement extends AbstractAWEPlacement {

    private final IAWECallback callback;
    private UUID taskUUID;
    
    /**
     * Constructor.
     *
     * @param playerEntry The PlayerEntry
     * @param placement The placement
     * @param callback
     */
    AWEPlacement(IAsyncWorldEdit asyncWorldEdit, PlayerEntry playerEntry, IPlacement placement, UUID taskUUID, IAWECallback callback) {
        super(asyncWorldEdit, playerEntry, placement);
        this.callback = callback;
        this.taskUUID = taskUUID;
    }
    
  
    

    @Override
    public void place(EditSession editSession, final Vector pos, final PlaceOptions options) {

        final int jobId = getJobId();
        final EditSession session;
        final AWEJobEntry job;
        final WaitFor wait;

        if (editSession instanceof AsyncEditSession) {
            AsyncEditSession aSession = (AsyncEditSession) editSession;
            wait = aSession.getWait();
            session = new CancelabeEditSession(aSession, aSession.getMask(), jobId);
            job = new AWEJobEntry(playerEntry, (CancelabeEditSession) session, jobId, "place", taskUUID, callback);
        } else {
            session = editSession;
            wait = null;
            job = new AWEJobEntry(playerEntry, jobId, "place", taskUUID, callback);
        }

        if(callback != null) {
            callback.onJobAdded(job);
        }
        
        scheduler.runAsync(new AWESilentAsyncTask(placement, editSession, playerEntry, null, placer, job, callback){
            
            @Override
            public void task(IPlacement placement) throws MaxChangedBlocksException {
                if (wait != null) {
                    wait.checkAndWait(null);
                }
                placement.place(session, pos, options);
            }
        });
        
       

    }

    @Override
    public int getWidth() {
        return placement.getWidth();
    }

    @Override
    public int getHeight() {
        return placement.getHeight();
    }

    @Override
    public int getLength() {
        return placement.getLength();
    }

    @Override
    public Vector getSize() {
        return placement.getSize();
    }

    
}
