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

import com.chingo247.structureapi.StructureAPI;
import com.chingo247.structureapi.construction.StructureEntry;
import com.chingo247.structureapi.model.structure.IStructure;
import com.chingo247.structureapi.model.structure.Structure;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import org.primesoft.asyncworldedit.AsyncWorldEditMain;
import org.primesoft.asyncworldedit.api.IAsyncWorldEdit;
import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacerListener;
import org.primesoft.asyncworldedit.blockPlacer.entries.JobEntry;
import com.chingo247.structureapi.construction.IStructureEntry;

/**
 *
 * @author Chingo
 */
public class AWEJobManager {

    private static final Logger LOG = Logger.getLogger(AWEJobManager.class.getSimpleName());
    private static AWEJobManager instance;
    private Map<UUID, AWEPlacementTask> tasks;
    private Lock jobLock;
    
    /**
     * Private Constructor as this is a singleton
     */
    private AWEJobManager() {
        this.tasks = Maps.newHashMap();
        this.jobLock = new ReentrantLock();

        StructureAPI.getInstance().getAsyncWorldEditIntegration().getAsyncWorldEdit().getBlockPlacer().addListener(new IBlockPlacerListener() {

            @Override
            public void jobAdded(JobEntry je) { /* DO NOTHING HERE! */ }

            @Override
            public void jobRemoved(JobEntry je) {

                if (je instanceof AWEJobEntry) {
                    AWEJobEntry jobEntry = (AWEJobEntry) je;
                    jobLock.lock();
                    try {
                        AWEPlacementTask task = tasks.get(jobEntry.getTaskUUID());
                        if (task != null) {
                            if (task.isChecked()) {
                                return;
                            }
                            task.setChecked(true);
                            task.finish();
                            // Remove this AWE Task
                            tasks.remove(task.getUUID());
                        }
                    } finally {
                        jobLock.unlock();
                    }

                }

            }
        });

    }

    public IStructure getStructure(UUID taskUUID) {
        jobLock.lock();
        try {
            AWEPlacementTask task = tasks.get(taskUUID);
            if (task != null) {
                IStructureEntry entry = task.getConstructionEntry();
                if(entry != null) {
                    IStructure structure = entry.getStructure();
                    return structure;
                }
            }
        } finally {
            jobLock.unlock();
        }
        return null;
    }

    public static AWEJobManager getInstance() {
        if (instance == null) {
            instance = new AWEJobManager();
        }
        return instance;
    }

    /**
     * Register a new AWE Placement task to track
     *
     * @param task The task
     */
    void register(AWEPlacementTask task) {
        jobLock.lock();
        try {
            this.tasks.put(task.getUUID(), task);
        } finally {
            jobLock.unlock();
        }
    }

    /**
     * Unregisters an AWE Placement task
     *
     * @param task
     */
    void unregister(AWEPlacementTask task) {
        jobLock.lock();
        try {
            tasks.remove(task.getUUID());
        } finally {
            jobLock.unlock();
        }

    }

}
