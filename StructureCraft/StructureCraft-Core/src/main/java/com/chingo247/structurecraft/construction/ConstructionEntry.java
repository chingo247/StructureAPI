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

import com.chingo247.structurecraft.model.structure.IStructure;
import com.google.common.base.Preconditions;
import java.util.LinkedList;
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
    private StructureBlockPlacingTask currentTask;
    private Queue<StructureBlockPlacingTask> tasks;
    private IPlacementSource source;

    protected ConstructionEntry(IConstructionExecutor executor, IStructure structure) {
        Preconditions.checkNotNull(structure, "Structure may not be null!");
        this.tasks = new LinkedList<>();
        this.structure = structure;
        this.constructionExecutor = executor;
    }
    
    public void setPlacementSource(IPlacementSource source) {
        this.source = source;
    }

   
    
    @Override
    public IConstructionExecutor getConstructionExecutor() {
        return constructionExecutor;
    }

    @Override
    public void addTask(StructureBlockPlacingTask task) {
        System.out.println("[ConstructionEntry]: adding " + task.getClass().getSimpleName() + " to structure #" + structure.getId());
        this.tasks.add(task);
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

    @Override
    public void proceed() {
        System.out.println("[ConstructionEntry]: Proceed!");
        if (currentTask != null && (currentTask.hasFailed() || currentTask.isCancelled())) {
            purge();
            return;
        }

        currentTask = tasks.peek();
        if (currentTask == null) {
            System.out.println("[ConstructionEntry]: Current task is null!");
            System.out.println("[ConstructionEntry]: Removing current task!");
            constructionExecutor.remove(this);

            if (nextEntry != null) {
                System.out.println("[ConstructionEntry]: Moving to next entry");
                nextEntry.proceed();

                // Clean up
                nextEntry = null;
                prevEntry = null;
                currentTask = null;
            }
        } else {
            System.out.println("[ConstructionEntry]: Starting new task!");
            tasks.poll();
            currentTask.start();
        }

    }

    /**
     * Stops running tasks, clears existing ones.
     */
    @Override
    public void purge() {
        System.out.println("[ConstructionEntry]: PURGE TASK HERE");
        if (currentTask != null && !currentTask.isCancelled()) {
            System.out.println("[ConstructionEntry]: Not yet cancelled");
            currentTask.cancel();

        }

        if (nextEntry != null) {
            nextEntry.purge();
            nextEntry = null;
        }

        prevEntry = null;
        tasks.clear();
    }

    @Override
    public IPlacementSource getPlacementSource() {
        return source;
    }

}
