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
package com.chingo247.structurecraft.construction;

import com.chingo247.settlercraft.core.event.async.AsyncEventManager;
import com.chingo247.structurecraft.construction.event.StructureTaskCancelledEvent;
import com.chingo247.structurecraft.construction.event.StructureTaskCompleteEvent;
import com.google.common.base.Preconditions;
import java.util.UUID;

/**
 *
 * @author Chingo
 */
public abstract class StructureTask {

    private IConstructionEntry constructionEntry;
    private UUID uuid;
    private boolean cancelled = false;
    private boolean failed = false;
    private boolean finished = false;
    private boolean started = false;
    private String action;
    private final UUID submitter;

    public StructureTask(String action, IConstructionEntry constructionEntry, UUID submitter) {
        Preconditions.checkNotNull(constructionEntry, "ConstructionEntry may not be null");
        Preconditions.checkNotNull(action, "Action may not null");
        Preconditions.checkArgument(!"".equals(action), "Action may not be empty");
        this.constructionEntry = constructionEntry;
        this.uuid = UUID.randomUUID();
        this.action = action;
        this.submitter = submitter;
    }

    public UUID getSubmitter() {
        return submitter;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    public IConstructionEntry getConstructionEntry() {
        return constructionEntry;
    }

    public String getAction() {
        return action;
    }

    public UUID getUUID() {
        return uuid;
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public void setFailed(boolean failed) {
        this.failed = false;
    }

    public boolean hasFailed() {
        return failed;
    }

    public synchronized final void start() {
        if(started == false) {
            started = true;
            execute();
        }
    }
    
    protected abstract void execute();
    
    protected abstract void onCancel();

    public synchronized final void cancel() {
        if (!cancelled) {
            setCancelled(true);
            onCancel();
            constructionEntry.getConstructionExecutor().remove(constructionEntry);
            finish();
        } 
    }

    /**
     * *
     * Indicate that this task has finished
     */
    public synchronized final void finish() {
        if (!finished) {
            started = false;
            finished = true;
            if (isCancelled()) {
                AsyncEventManager.getInstance().post(new StructureTaskCancelledEvent(this));
            } else {
                AsyncEventManager.getInstance().post(new StructureTaskCompleteEvent(this));
            }
            
            if(!isCancelled() && !failed) {
                constructionEntry.proceed();
            } else {
                constructionEntry.purge();
            }
        }
    }

}
