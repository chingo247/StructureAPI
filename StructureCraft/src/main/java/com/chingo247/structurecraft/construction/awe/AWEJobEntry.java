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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacer;
import org.primesoft.asyncworldedit.api.blockPlacer.IJobEntryListener;
import org.primesoft.asyncworldedit.blockPlacer.entries.JobEntry;
import org.primesoft.asyncworldedit.playerManager.PlayerEntry;
import org.primesoft.asyncworldedit.worldedit.CancelabeEditSession;

/**
 * JobEntry NON-TALKATIVE version
 * @author Chingo
 */
public class AWEJobEntry extends JobEntry {

    /**
     * Is the job status
     */
    private JobEntry.JobStatus status;
    
    
    /**
     * The player name
     */
    private final PlayerEntry player;

    private boolean m_canceled = false;
    
    private boolean m_demolishing = false;
    
    private IAWECallback callback;
    
    private UUID taskUUID;

    /**
     * All job state changed events
     */
    private final List<IJobEntryListener> listeners;
    


    
    @Override
    public boolean isDemanding() {
        return false;
    }
    
    @Override
    public JobEntry.JobStatus getStatus() {
        return status;
    }

        /**
     * Set the job state
     * @param newStatus 
     */
    @Override
    public void setStatus(JobEntry.JobStatus newStatus) {
        int newS = newStatus.getSeqNumber();
        int oldS = status.getSeqNumber();

        if (newS < oldS) {
            return;
        }
        status = newStatus;
        callStateChangedEvents();
    }

    @Override
    public void addStateChangedListener(IJobEntryListener listener) {
        if (listener == null) {
            return;
        }

        synchronized (listeners) {
            if (!listeners.contains(listener)) {
                listeners.add(listener);
            }
        }
    }
    
    public void addStateChangeListener(AWEJobListener listener) {
        addStateChangedListener(listener);
    }
    
    
        

    /**
     * Create new instance of the class
     * @param player player uuid
     * @param jobId job id
     * @param name operation name
     * @param taskID
     */
    public AWEJobEntry(PlayerEntry player, int jobId, String name, UUID taskUUID, IAWECallback callback) {
        super(player, jobId, name);
        this.player = player;
        this.status = JobEntry.JobStatus.Initializing;
        this.listeners = new ArrayList<>();
        this.callback = callback;
        this.taskUUID = taskUUID;
    }

    
    /**     
     * Create new instance of the class
     * @param player player uuid
     * @param jobId job id
     * @param name operation name
     * @param cEditSession the cancelable edit session
     * @param taskID
     */
    public AWEJobEntry(PlayerEntry player, 
            CancelabeEditSession cEditSession,
            int jobId, String name, UUID taskUUID, IAWECallback callback) {
        super(player, cEditSession, jobId, name);

        this.player = player;
        this.status = JobEntry.JobStatus.Initializing;
        this.listeners = new ArrayList<>();
        this.callback = callback;
        this.taskUUID = taskUUID;
        
    }

    public boolean isCanceled() {
        return m_canceled;
    }
    
    void setCanceled(boolean canceled) {
        m_canceled = canceled;
    }
    
    void setDemolishing(boolean demolishing) {
        m_demolishing = demolishing;
    }

    public boolean isDemolishing() {
        return m_demolishing;
    }

    public UUID getTaskUUID() {
        return taskUUID;
    }
    
    
    
    
    @Override
    public String toString() {
        return  "[" + getJobId() + "] " + getName();
    }

    @Override
    public boolean process(IBlockPlacer bp) {
        
        switch (status) {
            case Done:
                bp.removeJob(player, this);
                return true;
            case PlacingBlocks:
                setStatus(JobEntry.JobStatus.Done);
                bp.removeJob(player, this);
                break;
            case Initializing:
            case Preparing:
            case Waiting:
                setStatus(JobEntry.JobStatus.PlacingBlocks);
                callback.onStarted();
//                AsyncEventManager.getInstance().post(new StructureJobStartedEvent(m_TaskUUID, getJobId()));
                break;
        }

        
        return true;
    }

    
    /**
     * Inform the listener of state changed
     */
    private void callStateChangedEvents() {
        synchronized (listeners) {
            for (IJobEntryListener listener : listeners) {
                listener.jobStateChanged(this);
            }
        }
    }
}
