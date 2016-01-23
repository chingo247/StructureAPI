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

import com.chingo247.structurecraft.construction.listener.IConstructionListener;
import com.chingo247.structurecraft.construction.task.ITaskStartedListener;
import com.chingo247.structurecraft.construction.task.StructureTask;
import com.chingo247.structurecraft.model.structure.IStructure;
import com.chingo247.structurecraft.util.ProgressChecker;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 *
 * @author Chingo
 */
public class StructureEntry implements IStructureEntry {
    
    private IStructure structure;
    private IContractor constractor;
    private StructureEntry nextEntry;
    private StructureEntry prevEntry;
    private StructureTask currentTask;
    private Queue<StructureTask> tasks;
    private IContract contract;
    private int total = 0, done = 0;
    private List<IConstructionListener> listeners;
    private double reportableProgress;
    private boolean firstQueue = true, firstStarted = true;
    private ProgressChecker checker;

    protected StructureEntry(IContractor constractor, IStructure structure, IContract contract) {
        Preconditions.checkNotNull(structure, "Structure may not be null!");
        this.tasks = new LinkedList<>();
        this.structure = structure;
        this.constractor = constractor;
        this.contract = contract;
        this.listeners = Lists.newArrayList();
        this.reportableProgress = 5.0;
    }

    public void setReportableProgress(double reportableProgress) {
        Preconditions.checkArgument(reportableProgress >= 0, "reportableProgress must be greater or equal to 0");
        Preconditions.checkArgument(reportableProgress <= 100, "reportableProgress must be smaller or equal to 100");
        this.reportableProgress = reportableProgress;
    }

    public double getReportableProgress() {
        return reportableProgress;
    }
    
    @Override
    public void update(IStructure structure) {
        if(!structure.getId().equals(this.structure.getId())) {
            throw new IllegalArgumentException("Structure id does not match with current structure!");
        }
        this.structure = structure;
    }
    
    @Override
    public IContract getContract() {
        return contract;
    }

    @Override
    public IContractor getConstructionExecutor() {
        return constractor;
    }

    @Override
    public void addTask(StructureTask task) {
        this.tasks.add(task);
        total++;
    }

    void setPrevEntry(StructureEntry entry) {
        this.prevEntry = entry;
    }

    void setNextEntry(StructureEntry nextEntry) {
        Preconditions.checkArgument(!nextEntry.equals(this), "Next entry may not be equal the current entry");
        Preconditions.checkArgument(!matchesAncestor(nextEntry), "Entry may not be equal to any previous entries");
        this.nextEntry = nextEntry;
        nextEntry.setPrevEntry(this);
    }

    @Override
    public double getProgress() {
        if (done == 0) {
            return 0.0; // Never divide by zero...
        }

        return (double) ((done / total) * 100);
    }

    private boolean matchesAncestor(StructureEntry entry) {
        if (prevEntry == null) {
            return false;
        } else if (prevEntry.equals(entry)) {
            return true;
        } else {
            return prevEntry.matchesAncestor(entry);
        }
    }

    @Override
    public IStructure getStructure() {
        return structure;
    }

    private void clear() {
        // Clean up
        nextEntry = null;
        prevEntry = null;
        currentTask = null;
        listeners.clear();
        tasks.clear();
        firstQueue = true;
        firstStarted = true;
    }

    @Override
    public void addListener(IConstructionListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void proceed() {

        // If it was cancelled or has failed... abort all
        if (currentTask != null && (currentTask.hasFailed() || currentTask.isCancelled())) {
            purge();
            clear();
            for (Iterator<IConstructionListener> iterator = listeners.iterator(); iterator.hasNext();) {
                IConstructionListener next = iterator.next();
                if (currentTask.hasFailed()) {
                    next.onFailed(this);
                } else {
                    next.onCancelled(this);
                }
            }
        } else {

            // If there already was a task... and proceed is called.
            // Then previous task is finished
            // The statement is assured below
            if (currentTask != null) {
                if (!currentTask.isFinished()) {
                    throw new RuntimeException("Proceed was called while current task hasn't finished yet!");
                }

                done++;
                for (Iterator<IConstructionListener> iterator = listeners.iterator(); iterator.hasNext();) {
                    IConstructionListener next = iterator.next();
                    next.onProgress(this);
                }
            }

            currentTask = tasks.peek();
            if (currentTask == null) {
                // This task has completed
                for (Iterator<IConstructionListener> iterator = listeners.iterator(); iterator.hasNext();) {
                    IConstructionListener next = iterator.next();
                    next.onComplete(this);
                }
                clear();
                constractor.remove(this);
                if (nextEntry != null) {
                    nextEntry.proceed();
                }
            } else {
                tasks.poll();

                currentTask.addListener(new ITaskStartedListener() {

                    @Override
                    public void onStarted(StructureTask task) {
                        if (firstStarted) {
                            firstStarted = false;
                            for (Iterator<IConstructionListener> iterator = listeners.iterator(); iterator.hasNext();) {
                                IConstructionListener next = iterator.next();
                                next.onStarted(StructureEntry.this);
                            }
                        }
                    }
                });

                currentTask.start();
                if (firstQueue) {
                    firstQueue = false;
                    for (Iterator<IConstructionListener> iterator = listeners.iterator(); iterator.hasNext();) {
                        IConstructionListener next = iterator.next();
                        next.onQueued(this);
                    }
                }
            }
        }
    }

    /**
     * Stops running tasks, clears existing ones.
     */
    @Override
    public void purge() {
        if (currentTask != null && !currentTask.isCancelled()) {
            currentTask.cancel();

        }

        if (nextEntry != null) {
            nextEntry.purge();
            nextEntry = null;
        }

        prevEntry = null;
        
    }

    @Override
    public boolean hasNextTask() {
        return tasks.peek() != null;
    }

    @Override
    public int numTasks() {
        return tasks.size();
    }

    @Override
    public boolean hasProgress() {
        return (checker.checkProgress((done / total) * 100, reportableProgress));
    }

}
