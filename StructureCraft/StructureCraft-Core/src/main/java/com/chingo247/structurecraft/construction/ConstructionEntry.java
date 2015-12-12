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

import com.chingo247.structurecraft.construction.plan.ConstructionPlan;
import com.chingo247.structurecraft.construction.plan.IConstructionPlan;
import com.chingo247.structurecraft.model.structure.IStructure;
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
public class ConstructionEntry implements IConstructionEntry {

    private IStructure structure;
    private IConstructionExecutor constructionExecutor;
    private ConstructionEntry nextEntry;
    private ConstructionEntry prevEntry;
    private StructureTask currentTask;
    private Queue<StructureTask> tasks;
    private IConstructionPlan plan;
    private int total = 0, done = 0;
    private List<IConstructionListener> listeners;

    protected ConstructionEntry(IConstructionExecutor executor, IStructure structure, IConstructionPlan plan) {
        Preconditions.checkNotNull(structure, "Structure may not be null!");
        this.tasks = new LinkedList<>();
        this.structure = structure;
        this.constructionExecutor = executor;
        this.plan = plan;
        this.listeners = Lists.newArrayList();
    }

    @Override
    public IConstructionPlan getConstructionPlan() {
        return plan;
    }

    @Override
    public IConstructionExecutor getConstructionExecutor() {
        return constructionExecutor;
    }

    @Override
    public void addTask(StructureTask task) {
        this.tasks.add(task);
        total++;
    }

    void setPrevEntry(ConstructionEntry entry) {
        this.prevEntry = entry;
    }

    void setNextEntry(ConstructionEntry nextEntry) {
        Preconditions.checkArgument(!nextEntry.equals(this), "Next entry may not be equal the current entry");
        Preconditions.checkArgument(!matchesAncestor(nextEntry), "Entry may not be equal to any previous entries");
        this.nextEntry = nextEntry;
        nextEntry.setPrevEntry(this);
    }

    @Override
    public double getProgress() {
        if(done == 0 || total == 0) {
            return 0; // Never divide by zero...
        }
        
        double progress = ((double) (done / total)) * 100;
        return progress;
    }

    private boolean matchesAncestor(ConstructionEntry entry) {
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
    }
    
    @Override
    public void addListener(IConstructionListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void proceed() {
        
        if (currentTask != null && (currentTask.hasFailed() || currentTask.isCancelled())) {
            purge();
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

//            System.out.println("[ConstructionEntry]: Current task is null!");
//            System.out.println("[ConstructionEntry]: Removing current task!");
                constructionExecutor.remove(this);
                if (nextEntry != null) {
//                System.out.println("[ConstructionEntry]: Moving to next entry");
                    nextEntry.proceed();
                }
            } else {
//            System.out.println("[ConstructionEntry]: Starting new task!");
                tasks.poll();
                
                currentTask.addListener(new ITaskStartedListener() {

                    @Override
                    public void onStarted(StructureTask task) {
                        for (Iterator<IConstructionListener> iterator = listeners.iterator(); iterator.hasNext();) {
                            IConstructionListener next = iterator.next();
                            next.onStarted(ConstructionEntry.this);
                        }
                    }
                });
                
                currentTask.start();
                for (Iterator<IConstructionListener> iterator = listeners.iterator(); iterator.hasNext();) {
                    IConstructionListener next = iterator.next();
                    next.onQueued(this);
                }
            }
        }
    }
    
    

    /**
     * Stops running tasks, clears existing ones.
     */
    @Override
    public void purge() {
//        System.out.println("[ConstructionEntry]: PURGE TASK HERE");
        if (currentTask != null && !currentTask.isCancelled()) {
//            System.out.println("[ConstructionEntry]: Not yet cancelled");
            currentTask.cancel();

        }

        if (nextEntry != null) {
            nextEntry.purge();
            nextEntry = null;
        }

        prevEntry = null;
        clear();
    }

    @Override
    public boolean hasNextTask() {
        return tasks.peek() != null;
    }

    @Override
    public int numTasks() {
        return tasks.size();
    }

}
