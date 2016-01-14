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
package com.chingo247.structurecraft.construction.actions;

import com.chingo247.structurecraft.util.ProgressChecker;
import com.chingo247.structurecraft.StructureAPI;
import com.chingo247.structurecraft.construction.IConstructionListener;
import com.chingo247.structurecraft.construction.ITaskAssigner;
import com.chingo247.structurecraft.model.structure.ConstructionStatus;
import com.chingo247.structurecraft.model.structure.IStructure;
import com.chingo247.structurecraft.placement.block.DemolishingPlacement;
import com.chingo247.structurecraft.placement.IPlacement;
import com.chingo247.structurecraft.util.RegionUtil;
import com.chingo247.xplatform.core.APlatform;
import com.chingo247.xplatform.core.IColors;
import com.chingo247.structurecraft.construction.IContractor;
import com.chingo247.structurecraft.construction.IStructureEntry;

/**
 *
 * @author Chingo
 */
public class Demolish extends Construction {

    public Demolish(IContractor executor, IStructure structure, ITaskAssigner assigner) {
        super(executor, structure, assigner);
    }

    @Override
    public IPlacement getPlacement(IStructure structure) throws Exception {
        DemolishingPlacement placement = new DemolishingPlacement(RegionUtil.getSize(structure.getCuboidRegion()));
        return placement;
    }

    @Override
    public void register(IStructureEntry entry) throws Exception {
        final ProgressChecker checker = new ProgressChecker();
        final double reportableProgress = getReportableProgress();
        
        entry.addListener(new IConstructionListener() {

            @Override
            public void onComplete(IStructureEntry newEntry) {
                APlatform platform = StructureAPI.getInstance().getPlatform();
                IColors colors = platform.getChatColors();
                String[] message = new String[] {
                        colors.green() + "COMPLETED " + colors.reset() + getStructureString(structure),
                        colors.red()+ "REMOVED " + colors.reset() + getStructureString(structure)
                };
                handleEntry(newEntry, ConstructionStatus.REMOVED, false, message);
            }

            @Override
            public void onCancelled(IStructureEntry newEntry) {
                APlatform platform = StructureAPI.getInstance().getPlatform();
                IColors colors = platform.getChatColors();
                String message = colors.red()+ "CANCELLED " + colors.reset() + getStructureString(structure);
                handleEntry(newEntry, ConstructionStatus.STOPPED, false, message);
            }

            @Override
            public void onStarted(IStructureEntry newEntry) {
                APlatform platform = StructureAPI.getInstance().getPlatform();
                IColors colors = platform.getChatColors();
                String message = colors.yellow()+ "DEMOLISHING " + colors.reset() + getStructureString(structure);
                handleEntry(newEntry, ConstructionStatus.DEMOLISHING, false, message);
            }

            @Override
            public void onQueued(IStructureEntry newEntry) {
                APlatform platform = StructureAPI.getInstance().getPlatform();
                IColors colors = platform.getChatColors();
                String message = "QUEUED " + colors.reset() + getStructureString(structure);
                handleEntry(newEntry, ConstructionStatus.QUEUED, false, message);
            }

            @Override
            public void onProgress(IStructureEntry newEntry) {
                if (checker.checkProgress(newEntry.getProgress(), reportableProgress) && newEntry.getProgress() < 100.0) {                
                    APlatform platform = StructureAPI.getInstance().getPlatform();
                    IColors colors = platform.getChatColors();
                    String message = colors.yellow()+ "DEMOLISHING " + colors.reset() + newEntry.getProgress() + "% " + getStructureString(structure);
                    handleEntry(newEntry, ConstructionStatus.DEMOLISHING, true, message);
                }
            }

            @Override
            public void onFailed(IStructureEntry newEntry) {
                APlatform platform = StructureAPI.getInstance().getPlatform();
                IColors colors = platform.getChatColors();
                String message = colors.red()+ "FAILED " + colors.reset() + getStructureString(structure);
                handleEntry(newEntry, ConstructionStatus.STOPPED, false, message);
            }
        });
    }

}
