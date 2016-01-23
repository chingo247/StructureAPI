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
package com.chingo247.structurecraft.construction.listener;

import com.chingo247.structurecraft.StructureAPI;
import com.chingo247.structurecraft.construction.IStructureEntry;
import com.chingo247.structurecraft.model.structure.ConstructionStatus;
import com.chingo247.xplatform.core.APlatform;
import com.chingo247.xplatform.core.IColors;

/**
 *
 * @author Chingo
 */
public class RollbackListener extends ConstructionListener {

    @Override
    public void onComplete(IStructureEntry newEntry) {
        APlatform platform = StructureAPI.getInstance().getPlatform();
        IColors colors = platform.getChatColors();
        String[] message = new String[]{
            colors.green() + "COMPLETED " + colors.reset() + getStructureString(newEntry.getStructure()),
            colors.red() + "REMOVED " + colors.reset() + getStructureString(newEntry.getStructure())
        };
        handleEntry(newEntry, ConstructionStatus.REMOVED, false, message);
    }

    @Override
    public void onCancelled(IStructureEntry newEntry) {
        APlatform platform = StructureAPI.getInstance().getPlatform();
        IColors colors = platform.getChatColors();
        String message = colors.red() + "CANCELLED " + colors.reset() + getStructureString(newEntry.getStructure());
        handleEntry(newEntry, ConstructionStatus.STOPPED, false, message);
    }

    @Override
    public void onStarted(IStructureEntry newEntry) {
        APlatform platform = StructureAPI.getInstance().getPlatform();
        IColors colors = platform.getChatColors();
        String message = colors.yellow() + "ROLLING BACK " + colors.reset() + getStructureString(newEntry.getStructure());
        handleEntry(newEntry, ConstructionStatus.ROLLING_BACK, false, message);
    }

    @Override
    public void onQueued(IStructureEntry newEntry) {
        APlatform platform = StructureAPI.getInstance().getPlatform();
        IColors colors = platform.getChatColors();
        String message = "QUEUED " + colors.reset() + getStructureString(newEntry.getStructure());
        handleEntry(newEntry, ConstructionStatus.QUEUED, false, message);
    }

    @Override
    public void onProgress(IStructureEntry newEntry) {
        if (newEntry.hasProgress() && newEntry.getProgress() < 100.0) {
            APlatform platform = StructureAPI.getInstance().getPlatform();
            IColors colors = platform.getChatColors();
            String message = colors.yellow() + "ROLLING BACK " + colors.reset() + newEntry.getProgress() + "% " + getStructureString(newEntry.getStructure());
            handleEntry(newEntry, ConstructionStatus.ROLLING_BACK, true, message);
        }
    }

    @Override
    public void onFailed(IStructureEntry newEntry) {
        APlatform platform = StructureAPI.getInstance().getPlatform();
        IColors colors = platform.getChatColors();
        String message = colors.red() + "FAILED " + colors.reset() + getStructureString(newEntry.getStructure());
        handleEntry(newEntry, ConstructionStatus.STOPPED, false, message);
    }

}
