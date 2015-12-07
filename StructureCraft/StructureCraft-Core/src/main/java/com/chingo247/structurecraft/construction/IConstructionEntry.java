/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.construction;

import com.chingo247.structurecraft.model.structure.IStructure;

/**
 *
 * @author Chingo
 */
public interface IConstructionEntry {
    /**
     * Gets the construction executor.
     * @return The construction executor
     */
    IConstructionExecutor getConstructionExecutor();
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
    
}
