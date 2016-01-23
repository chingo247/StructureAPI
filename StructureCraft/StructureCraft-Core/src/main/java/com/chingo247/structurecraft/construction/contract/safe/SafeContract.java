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
package com.chingo247.structurecraft.construction.contract.safe;

import com.chingo247.structurecraft.StructureAPI;
import com.chingo247.structurecraft.construction.awe.AWEPlacementTask;
import com.chingo247.structurecraft.construction.IContract;
import com.chingo247.structurecraft.construction.IStructureEntry;
import com.chingo247.structurecraft.construction.contract.AContract;
import com.chingo247.structurecraft.construction.listener.ConstructionListener;
import com.chingo247.structurecraft.construction.producer.IPlacementProducer;
import com.chingo247.structurecraft.exeption.StructureException;
import com.chingo247.structurecraft.model.structure.IStructure;
import com.chingo247.structurecraft.placement.block.IBlockPlacement;
import com.chingo247.structurecraft.util.RegionUtil;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.World;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;
import org.primesoft.asyncworldedit.api.IAsyncWorldEdit;

/**
 *
 * @author Chingo
 */
public class SafeContract extends AContract {
    
    private static final int CHUNK_SIZE = 16;

    private IContract contract;

    public SafeContract(IContract contract) {
        Preconditions.checkNotNull(contract, "contract may not be null");
        this.contract = contract;
    }
    
    @Override
    public ConstructionListener getConstructionListener() {
        return contract.getConstructionListener();
    }

    @Override
    public IPlacementProducer<IBlockPlacement> getPlacementProducer() {
        return contract.getPlacementProducer();
    }

    @Override
    public void apply(IStructureEntry entry) throws StructureException {
        IStructure structure = entry.getStructure();
        IAsyncWorldEdit asyncWorldEdit = StructureAPI.getInstance().getAsyncWorldEditIntegration().getAsyncWorldEdit();
        IBlockPlacement placement = getPlacementProducer().produce(structure);
        CuboidRegion placementArea = placement.getCuboidRegion();
        
        // Get or create rollback data
        File rollbackFile = structure.getRollbackData().getRollbackSchematic();
        SchematicSaveData safeBlockData;
        if(rollbackFile.exists()) {
            try {
                System.out.println("Using existing Schematic Save Data");
                safeBlockData = SchematicSaveData.load(rollbackFile);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            System.out.println("Creating new Schematic Save Data");
            safeBlockData = new SchematicSaveData(rollbackFile, placementArea);
        }
        
        System.out.println("Area: " + placementArea);
        
        // Create place areas...
        Collection<CuboidRegion> chunks = RegionUtil.getChunkCubes(placementArea, CHUNK_SIZE, RegionUtil.ORDERED_XZ);

        IContract entryContract = entry.getContract();
        UUID player = entryContract.getPlayer();
        World world = entryContract.getEditSession().getWorld();
        EditSession editSession = entryContract.getEditSession();
        
        for (CuboidRegion subarea : chunks) {
            System.out.println("Subarea: " + subarea);
            entry.addTask(new SchematicSavingTask(entry, player, subarea, world, safeBlockData));
            entry.addTask(new AWEPlacementTask(
                    asyncWorldEdit, entry, new SchematicSubPlacement(placement, subarea), player, editSession, structure.getMin())
            );
        }
    }
    
    
    
    
}
