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

import com.chingo247.structurecraft.StructureAPI;
import com.chingo247.structurecraft.construction.options.PlaceOptions;
import com.chingo247.structurecraft.event.task.StructureTaskCancelledEvent;
import com.chingo247.structurecraft.event.task.StructureTaskCompleteEvent;
import com.google.common.base.Preconditions;
import java.util.UUID;

/**
 *
 * @author Chingo
 */
public abstract class StructureTask {

    private IConstructionEntry constructionEntry;
    private final UUID submitter, uuid;
    private boolean cancelled = false, failed = false, finished = false, started = false;
    protected final ITaskCallback callback;
   
    
    public StructureTask(IConstructionEntry constructionEntry, UUID submitter) {
        this(null, constructionEntry, submitter);
    }
    
    public StructureTask(ITaskCallback callback, IConstructionEntry constructionEntry, UUID submitter) {
        Preconditions.checkNotNull(constructionEntry, "ConstructionEntry may not be null");
        Preconditions.checkNotNull(submitter, "Submitter may not be null");
        this.constructionEntry = constructionEntry;
        this.uuid = UUID.randomUUID();
        this.callback = callback;
        this.submitter = submitter;
    }

   
    public UUID getSubmitter() {
        return submitter;
    }
    
    public IConstructionEntry getConstructionEntry() {
        return constructionEntry;
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
                callback.onCancelled();
                StructureAPI.getInstance().getEventDispatcher().dispatchEvent(new StructureTaskCancelledEvent(this));
            } else {
                callback.onComplete();
                StructureAPI.getInstance().getEventDispatcher().dispatchEvent(new StructureTaskCompleteEvent(this));
            }
            
            if(!isCancelled() && !failed) {
                constructionEntry.proceed();
            } else {
                constructionEntry.purge();
            }
        }
    }

}
