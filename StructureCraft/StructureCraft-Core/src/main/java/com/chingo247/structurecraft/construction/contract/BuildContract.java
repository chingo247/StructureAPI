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
package com.chingo247.structurecraft.construction.contract;

import com.chingo247.structurecraft.IStructureAPI;
import com.chingo247.structurecraft.StructureAPI;
import com.chingo247.structurecraft.construction.awe.AWEPlacementTask;
import com.chingo247.structurecraft.construction.producer.BlockPlacementProducer;
import com.chingo247.structurecraft.exeption.StructureException;
import com.chingo247.structurecraft.placement.IPlacement;
import com.chingo247.structurecraft.construction.producer.IPlacementProducer;
import com.chingo247.structurecraft.construction.IStructureEntry;
import com.chingo247.structurecraft.construction.listener.BuildListener;
import com.chingo247.structurecraft.construction.producer.BuildPlacementProducer;
import com.chingo247.structurecraft.placement.block.IBlockPlacement;
import com.chingo247.structurecraft.placement.options.PlaceOptions;
import com.sk89q.worldedit.Vector;
import org.primesoft.asyncworldedit.api.IAsyncWorldEdit;

/**
 *
 * @author Chingo
 */
public class BuildContract extends AContract {
    
    protected static final BlockPlacementProducer BUILD_PRODUCER = new BuildPlacementProducer();
    protected static final BuildListener BUILD_LISTENER = new BuildListener();
    

    @Override
    public IPlacementProducer<IBlockPlacement> getPlacementProducer() {
        return BUILD_PRODUCER;
    }

    @Override
    public BuildListener getConstructionListener() {
        return BUILD_LISTENER;
    }

    @Override
    public void apply(IStructureEntry entry) throws StructureException {
        IStructureAPI structureAPI = StructureAPI.getInstance();
        IAsyncWorldEdit asyncWorldEdit = structureAPI.getAsyncWorldEditIntegration().getAsyncWorldEdit();
        Vector position = entry.getStructure().getMin(); // Always place from the min position... 
        IPlacement placement = BUILD_PRODUCER.produce(entry.getStructure());                
        AWEPlacementTask task = new AWEPlacementTask(
                        asyncWorldEdit,
                        entry,
                        placement,
                        getPlayer(),
                        getEditSession(),
                        position
                );
        task.setOptions(new PlaceOptions());        
        entry.addListener(BUILD_LISTENER);        
        entry.addTask(task);
    }

//   

   

}