/*
 * Copyright (C) 2016 Chingo
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
package com.chingo247.structureapi.construction.listener;

import com.chingo247.structureapi.StructureAPI;
import com.chingo247.structureapi.construction.StructureEntry;
import com.chingo247.structureapi.model.structure.ConstructionStatus;
import com.chingo247.structureapi.util.Progress;
import com.chingo247.xplatform.core.APlatform;
import com.chingo247.xplatform.core.IColors;

/**
 *
 * @author Chingo
 */
public class RollbackListener extends ConstructionListener {

    @Override
    public void onComplete(StructureEntry newEntry) {
        APlatform platform = StructureAPI.getInstance().getPlatform();
        IColors colors = platform.getChatColors();
        String[] message = new String[]{
            getStructureString(newEntry.getStructure()) + colors.green() + " COMPLETED",
            getStructureString(newEntry.getStructure()) + colors.red() + " REMOVED" 
        };
        handleEntry(newEntry, ConstructionStatus.REMOVED, false, message);
    }

    @Override
    public void onCancelled(StructureEntry newEntry) {
        APlatform platform = StructureAPI.getInstance().getPlatform();
        IColors colors = platform.getChatColors();
        String message =  getStructureString(newEntry.getStructure()) + colors.red() + " ROLLBACK CANCELLED";
        handleEntry(newEntry, ConstructionStatus.STOPPED, false, message);
    }

    @Override
    public void onStarted(StructureEntry newEntry) {
        APlatform platform = StructureAPI.getInstance().getPlatform();
        IColors colors = platform.getChatColors();
        String message = getStructureString(newEntry.getStructure()) + colors.yellow() + " ROLLING BACK";
        handleEntry(newEntry, ConstructionStatus.ROLLING_BACK, false, message);
    }

    @Override
    public void onQueued(StructureEntry newEntry) {
        APlatform platform = StructureAPI.getInstance().getPlatform();
        IColors colors = platform.getChatColors();
        String message = getStructureString(newEntry.getStructure()) + colors.reset() + " QUEUED ";
        handleEntry(newEntry, ConstructionStatus.QUEUED, false, message);
    }

    @Override
    public void onProgress(StructureEntry newEntry) {
        if (newEntry.hasProgress() && newEntry.getProgress() < 100.0) {
            APlatform platform = StructureAPI.getInstance().getPlatform();
            IColors colors = platform.getChatColors();
            String message = getStructureString(newEntry.getStructure()) + colors.yellow() + " ROLLING BACK " + colors.reset() + Progress.ONE_DECIMAL.format(newEntry.getProgress()) + "%";
            handleEntry(newEntry, ConstructionStatus.ROLLING_BACK, true, message);
        }
    }

    @Override
    public void onFailed(StructureEntry newEntry) {
        APlatform platform = StructureAPI.getInstance().getPlatform();
        IColors colors = platform.getChatColors();
        String message = getStructureString(newEntry.getStructure()) + colors.red() + " ROLLBACK FAILED";
        handleEntry(newEntry, ConstructionStatus.STOPPED, false, message);
    }

}
