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

import com.chingo247.structurecraft.StructureAPI;
import com.chingo247.structurecraft.construction.IConstructionEntry;
import com.chingo247.structurecraft.construction.IConstructionExecutor;
import com.chingo247.structurecraft.construction.IConstructionListener;
import com.chingo247.structurecraft.construction.ITaskAssigner;
import com.chingo247.structurecraft.model.structure.ConstructionStatus;
import com.chingo247.structurecraft.model.structure.IStructure;
import com.chingo247.structurecraft.placement.block.DemolishingPlacement;
import com.chingo247.structurecraft.placement.IPlacement;
import com.chingo247.structurecraft.util.RegionUtil;
import com.chingo247.xplatform.core.APlatform;
import com.chingo247.xplatform.core.IColors;

/**
 *
 * @author Chingo
 */
public class DemolitionPlan extends ConstructionPlan {

    public DemolitionPlan(IConstructionExecutor executor, IStructure structure, ITaskAssigner assigner) {
        super(executor, structure, assigner);
    }

    @Override
    public IPlacement getPlacement(IStructure structure) throws Exception {
        DemolishingPlacement placement = new DemolishingPlacement(RegionUtil.getSize(structure.getCuboidRegion()));
        return placement;
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
                String[] message = new String[] {
                        colors.green() + "DEMOLITION COMPLETED " + colors.reset() + getStructureString(structure),
                        colors.red()+ "REMOVED " + colors.reset() + getStructureString(structure)
                };
                handleEntry(newEntry, ConstructionStatus.REMOVED, false, message);
            }

            @Override
            public void onCancelled(IConstructionEntry newEntry) {
                APlatform platform = StructureAPI.getInstance().getPlatform();
                IColors colors = platform.getChatColors();
                String message = colors.red()+ "DEMOLITION CANCELLED " + colors.reset() + getStructureString(structure);
                handleEntry(newEntry, ConstructionStatus.STOPPED, false, message);
            }

            @Override
            public void onStarted(IConstructionEntry newEntry) {
                APlatform platform = StructureAPI.getInstance().getPlatform();
                IColors colors = platform.getChatColors();
                String message = colors.yellow()+ "DEMOLISHING " + colors.reset() + getStructureString(structure);
                handleEntry(newEntry, ConstructionStatus.DEMOLISHING, false, message);
            }

            @Override
            public void onQueued(IConstructionEntry newEntry) {
                APlatform platform = StructureAPI.getInstance().getPlatform();
                IColors colors = platform.getChatColors();
                String message = colors.purple() + "DEMOLITION QUEUED " + colors.reset() + getStructureString(structure);
                handleEntry(newEntry, ConstructionStatus.QUEUED, false, message);
            }

            @Override
            public void onProgress(IConstructionEntry newEntry) {
                if (checker.checkProgress(newEntry.getProgress(), reportableProgress)) {                
                    APlatform platform = StructureAPI.getInstance().getPlatform();
                    IColors colors = platform.getChatColors();
                    String message = colors.yellow()+ "DEMOLISHING " + colors.reset() + newEntry.getProgress() + "% " + getStructureString(structure);
                    handleEntry(newEntry, ConstructionStatus.DEMOLISHING, true, message);
                }
            }

            @Override
            public void onFailed(IConstructionEntry newEntry) {
                APlatform platform = StructureAPI.getInstance().getPlatform();
                IColors colors = platform.getChatColors();
                String message = colors.red()+ "DEMOLITION FAILED " + colors.reset() + getStructureString(structure);
                handleEntry(newEntry, ConstructionStatus.STOPPED, false, message);
            }
        });
    }

}
