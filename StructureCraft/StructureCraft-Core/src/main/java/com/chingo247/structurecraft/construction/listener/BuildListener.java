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
import com.chingo247.structurecraft.util.Progress;
import com.chingo247.xplatform.core.APlatform;
import com.chingo247.xplatform.core.IColors;

/**
 *
 * @author Chingo
 */
public class BuildListener extends ConstructionListener {

    public BuildListener() {
    }

    @Override
    public void onComplete(IStructureEntry structureEntry) {
        APlatform platform = StructureAPI.getInstance().getPlatform();
        IColors colors = platform.getChatColors();
        String message = colors.green() + "COMPLETED " + colors.reset() + getStructureString(structureEntry.getStructure());
        handleEntry(structureEntry, ConstructionStatus.COMPLETED, false, message);
    }

    @Override
    public void onCancelled(IStructureEntry structureEntry) {
        APlatform platform = StructureAPI.getInstance().getPlatform();
        IColors colors = platform.getChatColors();
        String message = colors.red() + "BUILDING " + colors.reset() + getStructureString(structureEntry.getStructure());
        handleEntry(structureEntry, ConstructionStatus.STOPPED, false, message);
    }

    @Override
    public void onStarted(IStructureEntry structureEntry) {
        APlatform platform = StructureAPI.getInstance().getPlatform();
        IColors colors = platform.getChatColors();
        String message = colors.yellow() + "BUILDING " + colors.reset() + getStructureString(structureEntry.getStructure());
        handleEntry(structureEntry, ConstructionStatus.BUILDING, false, message);
    }

    @Override
    public void onQueued(IStructureEntry structureEntry) {
        APlatform platform = StructureAPI.getInstance().getPlatform();
        IColors colors = platform.getChatColors();
        String message = "QUEUED " + colors.reset() + getStructureString(structureEntry.getStructure());
        handleEntry(structureEntry, ConstructionStatus.QUEUED, false, message);
    }

    @Override
    public void onProgress(IStructureEntry structureEntry) {
        if (structureEntry.hasProgress() && structureEntry.getProgress() < 100.0) {
            APlatform platform = StructureAPI.getInstance().getPlatform();
            IColors colors = platform.getChatColors();
            String message = colors.yellow() + "BUILDING " + colors.reset() + Progress.ONE_DECIMAL.format(structureEntry.getProgress()) + "% " + getStructureString(structureEntry.getStructure());
            handleEntry(structureEntry, ConstructionStatus.BUILDING, true, message);
        }
    }

    @Override
    public void onFailed(IStructureEntry structureEntry) {
        APlatform platform = StructureAPI.getInstance().getPlatform();
        IColors colors = platform.getChatColors();
        String message = colors.red() + "FAILED " + colors.reset() + getStructureString(structureEntry.getStructure());
        handleEntry(structureEntry, ConstructionStatus.ON_HOLD, false, message);
    }

}
