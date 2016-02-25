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
package com.chingo247.structureapi.construction.task;

import com.chingo247.structureapi.construction.StructureEntry;
import com.chingo247.structureapi.StructureAPI;
import com.chingo247.structureapi.event.task.StructureTaskCancelledEvent;
import com.chingo247.structureapi.event.task.StructureTaskCompleteEvent;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author Chingo
 */
public abstract class StructureTask {

    private StructureEntry structureEntry;
    private final UUID submitter, uuid;
    private boolean cancelled = false, failed = false, finished = false, started = false;
    private List<ITaskStartedListener> listeners;

    public StructureTask(StructureEntry constructionEntry, UUID submitter) {
        Preconditions.checkNotNull(constructionEntry, "ConstructionEntry may not be null");
        Preconditions.checkNotNull(submitter, "Submitter may not be null");
        this.structureEntry = constructionEntry;
        this.uuid = UUID.randomUUID();
//        this.callback = callback;
        this.submitter = submitter;
        this.listeners = Lists.newArrayList();
    }
    
    public void addListener(ITaskStartedListener listener) {
        this.listeners.add(listener);
    }
    
    protected Iterable<ITaskStartedListener> getListeners() {
        return new ArrayList<>(listeners);
    }
    
    public final UUID getSubmitter() {
        return submitter;
    }

    public final StructureEntry getConstructionEntry() {
        return structureEntry;
    }

    public final UUID getUUID() {
        return uuid;
    }

    public final boolean isFinished() {
        return finished;
    }

    public final boolean isCancelled() {
        return cancelled;
    }

    public final void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public final void setFailed(boolean failed) {
        this.failed = failed;
    }

    public final boolean hasFailed() {
        return failed;
    }

    public synchronized final void start() {
        if (started == false) {
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
            structureEntry.getConstructionExecutor().remove(structureEntry);
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
                StructureAPI.getInstance().getEventDispatcher().dispatchEvent(new StructureTaskCancelledEvent(this));
            } else {
                StructureAPI.getInstance().getEventDispatcher().dispatchEvent(new StructureTaskCompleteEvent(this));
            }
            listeners.clear();
            structureEntry.proceed();
        }
    }

}
