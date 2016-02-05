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

import org.primesoft.asyncworldedit.api.blockPlacer.IJobEntryListener;
import org.primesoft.asyncworldedit.blockPlacer.entries.JobEntry;

/**
 *
 * @author Chingo
 */
public abstract class AWEJobListener implements IJobEntryListener {

    @Override
    public void jobStateChanged(JobEntry je) {
        if(je instanceof AWEJobEntry) {
            jobStateChanged((AWEJobEntry) je);
        }
    }
    
    public abstract void jobStateChanged(AWEJobEntry jobEntry);

    
    
    
}
