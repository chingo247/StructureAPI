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
package com.chingo247.structurecraft.construction.plan;

import com.chingo247.structurecraft.IStructureAPI;
import com.chingo247.structurecraft.StructureAPI;
import com.chingo247.structurecraft.construction.IConstructionEntry;
import com.chingo247.structurecraft.construction.IConstructionExecutor;
import com.chingo247.structurecraft.construction.IConstructionListener;
import com.chingo247.structurecraft.construction.ITaskAssigner;
import com.chingo247.structurecraft.event.structure.construction.StructureConstructionCancelledEvent;
import com.chingo247.structurecraft.event.structure.construction.StructureConstructionFailedEvent;
import com.chingo247.structurecraft.event.structure.construction.StructureConstructionQueued;
import com.chingo247.structurecraft.event.structure.construction.StructureProgressUpdateEvent;
import com.chingo247.structurecraft.event.structure.StructureStateChangeEvent;
import com.chingo247.structurecraft.exeption.StructureException;
import com.chingo247.structurecraft.model.structure.ConstructionStatus;
import com.chingo247.structurecraft.model.structure.IStructure;
import com.chingo247.structurecraft.placement.IPlacement;
import com.chingo247.structurecraft.placement.RotationalPlacement;
import com.chingo247.xplatform.core.APlatform;
import com.chingo247.xplatform.core.IColors;

/**
 *
 * @author Chingo
 */
public class BuildPlan extends ConstructionPlan {

    public BuildPlan(IConstructionExecutor executor, IStructure structure, ITaskAssigner assigner) {
        super(executor, structure, assigner);
    }

    @Override
    public IPlacement getPlacement(IStructure structure) throws StructureException {
        IPlacement placement = structure.getStructurePlan().getPlacement();
        if (placement instanceof RotationalPlacement) {
            RotationalPlacement rt = (RotationalPlacement) placement;
            rt.rotate(structure.getDirection().getRotation());
        }
        return structure.getStructurePlan().getPlacement();
    }

    @Override
    public void register(IConstructionEntry entry) throws Exception {
        final ProgressChecker checker = new ProgressChecker();
        final double reportableProgress = getReportableProgress();
        
        entry.addListener(new IConstructionListener() {

            @Override
            public void onComplete(IConstructionEntry newEntry) {
                APlatform platform = StructureAPI.getInstance().getPlatform();
                IColors colors = platform.getChatColors();
                String message = colors.green() + "BUILDING COMPLETED " + colors.reset() + getStructureString(structure);
                handleEntry(newEntry, ConstructionStatus.COMPLETED, false, message);
            }

            @Override
            public void onCancelled(IConstructionEntry newEntry) {
                APlatform platform = StructureAPI.getInstance().getPlatform();
                IColors colors = platform.getChatColors();
                String message = colors.red()+ "BUILDING CANCELLED " + colors.reset() + getStructureString(structure);
                handleEntry(newEntry, ConstructionStatus.STOPPED, false, message);
            }

            @Override
            public void onStarted(IConstructionEntry newEntry) {
                APlatform platform = StructureAPI.getInstance().getPlatform();
                IColors colors = platform.getChatColors();
                String message = colors.red()+ "BUILDING CANCELLED " + colors.reset() + getStructureString(structure);
                handleEntry(newEntry, ConstructionStatus.STOPPED, false, message);
            }

            @Override
            public void onQueued(IConstructionEntry newEntry) {
                APlatform platform = StructureAPI.getInstance().getPlatform();
                IColors colors = platform.getChatColors();
                String message = colors.yellow() + "BUILDING QUEUED " + colors.reset() + getStructureString(structure);
                handleEntry(newEntry, ConstructionStatus.QUEUED, false, message);
            }

            @Override
            public void onProgress(IConstructionEntry newEntry) {
                if (checker.checkProgress(newEntry.getProgress(), reportableProgress)) {                
                    APlatform platform = StructureAPI.getInstance().getPlatform();
                    IColors colors = platform.getChatColors();
                    String message = colors.yellow()+ "BUILDING " + colors.reset() + newEntry.getProgress() + "% " + getStructureString(structure);
                    handleEntry(newEntry, ConstructionStatus.BUILDING, true, message);
                }
            }

            @Override
            public void onFailed(IConstructionEntry newEntry) {
                APlatform platform = StructureAPI.getInstance().getPlatform();
                IColors colors = platform.getChatColors();
                String message = colors.red()+ "BUILDING FAILED " + colors.reset() + getStructureString(structure);
                handleEntry(newEntry, ConstructionStatus.ON_HOLD, true, message);
            }
        });
    }
    
    

}
