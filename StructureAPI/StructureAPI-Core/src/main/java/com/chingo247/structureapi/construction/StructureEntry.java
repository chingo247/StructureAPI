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
package com.chingo247.structureapi.construction;

import com.chingo247.structureapi.construction.contract.Contract;
import com.chingo247.structureapi.construction.listener.IConstructionListener;
import com.chingo247.structureapi.construction.task.ITaskStartedListener;
import com.chingo247.structureapi.construction.task.StructureTask;
import com.chingo247.structureapi.model.structure.Structure;
import com.chingo247.structureapi.util.ProgressChecker;
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
public class StructureEntry  {
    
    private Structure structure;
    private IContractor constractor;
    private StructureEntry nextEntry;
    private StructureEntry prevEntry;
    private StructureTask currentTask;
    private Queue<StructureTask> tasks;
    private Contract contract;
    private int total = 0, done = 0;
    private List<IConstructionListener> listeners;
    private double reportableProgress;
    private boolean firstQueue = true, firstStarted = true;
    private ProgressChecker checker;

    protected StructureEntry(IContractor constractor, Structure structure, Contract contract) {
        Preconditions.checkNotNull(structure, "Structure may not be null!");
        this.tasks = new LinkedList<>();
        this.structure = structure;
        this.constractor = constractor;
        this.contract = contract;
        this.listeners = Lists.newArrayList();
        this.reportableProgress = 10.0;
        this.checker = new ProgressChecker();
    }

    public void setReportableProgress(double reportableProgress) {
        Preconditions.checkArgument(reportableProgress >= 0, "reportableProgress must be greater or equal to 0");
        Preconditions.checkArgument(reportableProgress <= 100, "reportableProgress must be smaller or equal to 100");
        this.reportableProgress = reportableProgress;
    }

    public double getReportableProgress() {
        return reportableProgress;
    }
    
    public void update(Structure structure) {
        if(!structure.getId().equals(this.structure.getId())) {
            throw new IllegalArgumentException("Structure id does not match with current structure!");
        }
        this.structure = structure;
    }
    
    public Contract getContract() {
        return contract;
    }

    public IContractor getConstructionExecutor() {
        return constractor;
    }

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

    public double getProgress() {
        if (done == 0) {
            return 0.0; // Never divide by zero...
        }
        return ( (double) ((double) done / (double) total) * 100);
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

    public Structure getStructure() {
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

    public void addListener(IConstructionListener listener) {
        this.listeners.add(listener);
    }

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
                
                constractor.remove(this);
                if (nextEntry != null) {
                    nextEntry.proceed();
                }
                clear();
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
    
    /**
     * Stops all tasks and will tell all listeners
     */
    public void stop() {
        purge();
        for (Iterator<IConstructionListener> iterator = listeners.iterator(); iterator.hasNext();) {
            IConstructionListener next = iterator.next();
            next.onCancelled(this);
        }
    }

    public boolean hasNextTask() {
        return tasks.peek() != null;
    }

    public int numTasks() {
        return tasks.size();
    }

    public boolean hasProgress() {
        return (checker.checkProgress(getProgress(), reportableProgress));
    }

}
