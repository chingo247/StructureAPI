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
package com.chingo247.structureapi.construction.awe;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacer;
import org.primesoft.asyncworldedit.blockPlacer.entries.JobEntry;
import org.primesoft.asyncworldedit.configuration.PermissionGroup;
import org.primesoft.asyncworldedit.playerManager.PlayerEntry;
import org.primesoft.asyncworldedit.strings.MessageType;
import org.primesoft.asyncworldedit.utils.SessionCanceled;
import org.primesoft.asyncworldedit.worldedit.CancelabeEditSession;
import org.primesoft.asyncworldedit.worldedit.ThreadSafeEditSession;

/**
 *
 * @author Chingo
 */
abstract class AWESilentBaseTask implements Runnable {
    
    /**
     * Command name
     */
    protected final String m_command;

    /**
     * The player
     */
    protected final PlayerEntry m_player;

    /**
     * Cancelable edit session
     */
    protected final CancelabeEditSession m_cancelableEditSession;

    /**
     * Thread safe edit session
     */
    protected final ThreadSafeEditSession m_safeEditSession;

    /**
     * The edit session
     */
    protected final EditSession m_editSession;

    /**
     * The blocks placer
     */
    protected final IBlockPlacer m_blockPlacer;

    /**
     * Job instance
     */
    protected final JobEntry m_job;

    /**
     * The permission group
     */
    protected final PermissionGroup m_group;
    
    protected final IAWECallback callback;
    

    public AWESilentBaseTask(final EditSession editSession, final PlayerEntry player,
            final String commandName, IBlockPlacer blocksPlacer, AWEJobEntry job, IAWECallback callback) {

        m_editSession = editSession;
        m_cancelableEditSession = (editSession instanceof CancelabeEditSession) ? (CancelabeEditSession) editSession : null;

        m_player = player;
        m_group = m_player.getPermissionGroup();
        m_command = commandName;
        m_blockPlacer = blocksPlacer;
        m_job = job;

        if (m_cancelableEditSession != null) {
            m_safeEditSession = m_cancelableEditSession.getParent();
        } else {
            m_safeEditSession = (editSession instanceof ThreadSafeEditSession) ? (ThreadSafeEditSession) editSession : null;
        }

        if (m_safeEditSession != null) {
            m_safeEditSession.addAsync(job);
        }
        
        this.callback = callback;
        
    }

    @Override
    public void run() {
        Object result = null;

        if (m_job.getStatus() == JobEntry.JobStatus.Canceled) {
            return;
        }

        m_job.setStatus(JobEntry.JobStatus.Preparing);
        m_blockPlacer.addTasks(m_player, m_job);

        try {
            if ((m_cancelableEditSession == null || !m_cancelableEditSession.isCanceled())
                    && (m_job.getStatus() != JobEntry.JobStatus.Canceled)) {
                result = doRun();
            }

            if (m_editSession != null) {
                if (m_editSession.isQueueEnabled()) {
                    m_editSession.flushQueue();
                } else if (m_cancelableEditSession != null) {
                    m_cancelableEditSession.resetAsync();
                } else if (m_safeEditSession != null) {
                    m_safeEditSession.resetAsync();
                }
            }

            m_job.setStatus(JobEntry.JobStatus.Waiting);
            m_blockPlacer.addTasks(m_player, m_job);
            doPostRun(result);
        } catch (MaxChangedBlocksException ex) {
            m_player.say(MessageType.BLOCK_PLACER_MAX_CHANGED.format());
        } catch (IllegalArgumentException ex) {
            if (ex.getCause() instanceof SessionCanceled) {
                callback.onCancelled();
            }
        }
        postProcess();

        m_job.taskDone();
        
        if (m_cancelableEditSession != null) {
            ThreadSafeEditSession parent = m_cancelableEditSession.getParent();
            parent.removeAsync(m_job);
        } else if (m_safeEditSession != null) {
            m_safeEditSession.removeAsync(m_job);
        }
    }

    protected abstract Object doRun() throws MaxChangedBlocksException;

    protected abstract void doPostRun(Object result);

    protected void postProcess() {
    }
    
}
