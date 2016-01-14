/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.construction;

import com.chingo247.structurecraft.model.structure.IStructure;
import com.chingo247.structurecraft.util.IProgressable;
import com.chingo247.structurecraft.construction.actions.IConstruction;

/**
 *
 * @author Chingo
 */
public interface IStructureEntry extends IProgressable {
    
    /**
     * Adds a listener for this entry. The listener will be removed once all the tasks are finished
     * or if a single tasks fails...
     * @param listener The listener to add
     */
    void addListener(IConstructionListener listener);
    
    IConstruction getConstructionDescription();
    
    /**
     * Gets the construction executor.
     * @return The construction executor
     */
    IContractor getConstructionExecutor();
    /**
     * Gets the structure.
     * @return The structure
     */
    IStructure getStructure();
    /**
     * Checks if there is a task next.
     * @return True if there is a task in the queue of this construction entry
     */
    boolean hasNextTask();
    /**
     * Returns the number of tasks.
     * @return The number of tasks
     */
    int numTasks();
    /**
     * Adds a task to the queue of this entry.
     * @param structureTask The task
     */
    void addTask(StructureTask structureTask);
    /**
     * Will trigger the next task to start.
     */
    void proceed();
    /**
     * Will stop/clean all tasks from this construction entry.
     */
    void purge();
    
    /**
     * Updates the entry with the given structure. Structure id must match current structure
     * @param structure The structure
     */
    void update(IStructure structure);
    
    
}
