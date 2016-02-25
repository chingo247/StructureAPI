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
public class DemolitionListener extends ConstructionListener {

    @Override
    public void onComplete(StructureEntry structureEntry) {
        APlatform platform = StructureAPI.getInstance().getPlatform();
        IColors colors = platform.getChatColors();
        String[] message = new String[]{
            getStructureString(structureEntry.getStructure()) + colors.green() + " COMPLETED" + colors.reset(),
            getStructureString(structureEntry.getStructure()) + colors.red() + " REMOVED" + colors.reset() 
        };
        handleEntry(structureEntry, ConstructionStatus.REMOVED, false, message);
    }

    @Override
    public void onCancelled(StructureEntry structureEntry) {
        APlatform platform = StructureAPI.getInstance().getPlatform();
        IColors colors = platform.getChatColors();
        String message = getStructureString(structureEntry.getStructure()) + colors.red() + " DEMOLITION CANCELLED" ;
        handleEntry(structureEntry, ConstructionStatus.STOPPED, false, message);
    }

    @Override
    public void onStarted(StructureEntry structureEntry) {
        APlatform platform = StructureAPI.getInstance().getPlatform();
        IColors colors = platform.getChatColors();
        String message = getStructureString(structureEntry.getStructure()) + colors.yellow() + " DEMOLISHING";
        handleEntry(structureEntry, ConstructionStatus.DEMOLISHING, false, message);
    }

    @Override
    public void onQueued(StructureEntry structureEntry) {
        APlatform platform = StructureAPI.getInstance().getPlatform();
        IColors colors = platform.getChatColors();
        String message = getStructureString(structureEntry.getStructure())  + colors.reset() + " QUEUED";
        handleEntry(structureEntry, ConstructionStatus.QUEUED, false, message);
    }

    @Override
    public void onProgress(StructureEntry structureEntry) {
        if (structureEntry.hasProgress() && structureEntry.getProgress() < 100.0) {
            APlatform platform = StructureAPI.getInstance().getPlatform();
            IColors colors = platform.getChatColors();
            String message = getStructureString(structureEntry.getStructure()) +  colors.yellow() + " DEMOLISHING " + colors.reset() + Progress.ONE_DECIMAL.format(structureEntry.getProgress()) + "% ";
            handleEntry(structureEntry, ConstructionStatus.DEMOLISHING, true, message);
        }
    }

    @Override
    public void onFailed(StructureEntry structureEntry) {
        APlatform platform = StructureAPI.getInstance().getPlatform();
        IColors colors = platform.getChatColors();
        String message = getStructureString(structureEntry.getStructure()) + colors.red() + " FAILED";
        handleEntry(structureEntry, ConstructionStatus.STOPPED, false, message);
    }

}
