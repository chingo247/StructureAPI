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
package com.chingo247.structureapi.construction.contract;

import com.chingo247.structureapi.IStructureAPI;
import com.chingo247.structureapi.StructureAPI;
import com.chingo247.blockstore.IBlockStoreChunk;
import com.chingo247.blockstore.IBlockStoreRegion;
import com.chingo247.blockstore.safe.SafeBlockStore;
import com.chingo247.blockstore.safe.SafeBlockStoreReader;
import com.chingo247.structureapi.construction.awe.AWEPlacementTask;
import com.chingo247.structureapi.exeption.StructureException;
import com.chingo247.structureapi.construction.StructureEntry;
import com.chingo247.structureapi.construction.listener.ConstructionListener;
import com.chingo247.structureapi.construction.listener.RollbackListener;
import com.chingo247.structureapi.construction.producer.IPlacementProducer;
import com.chingo247.structureapi.construction.task.StructurePlacingTask;
import com.chingo247.structureapi.construction.task.StructureTask;
import com.chingo247.structureapi.model.structure.Structure;
import com.chingo247.structureapi.placement.BlockStoreChunkPlacement;
import com.chingo247.structureapi.placement.block.IBlockPlacement;
import com.chingo247.structureapi.placement.options.PlaceOptions;
import com.sk89q.worldedit.Vector;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import org.primesoft.asyncworldedit.api.IAsyncWorldEdit;

/**
 *
 * @author Chingo
 */
public class RollbackContract extends Contract {

    protected static final ConstructionListener ROLLBACK__LISTENER = new RollbackListener();

    public RollbackContract() {
    }

    @Override
    public ConstructionListener getConstructionListener() {
        return ROLLBACK__LISTENER;
    }

    @Override
    public IPlacementProducer<IBlockPlacement> getPlacementProducer() {
        return null;
    }

    @Override
    public void apply(StructureEntry entry, PlaceOptions placeOptions) throws StructureException {
        IStructureAPI structureAPI = StructureAPI.getInstance();
        IAsyncWorldEdit asyncWorldEdit = structureAPI.getAsyncWorldEditIntegration().getAsyncWorldEdit();
        Vector structureMin = entry.getStructure().getMin(); // Always place from the min position... 
        entry.addListener(ROLLBACK__LISTENER);

        Structure structure = entry.getStructure();
        File blockStoreFile = structure.getRollbackData().getBlockStoreDirectory();

        if (!blockStoreFile.exists()) {
            throw new StructureException("No rollback data available");
        }

        try {
            SafeBlockStoreReader reader = new SafeBlockStoreReader();
            SafeBlockStore blockStore = reader.read(blockStoreFile);

            Iterator<File> regionFileIt = blockStore.regionFileIterator();
            while (regionFileIt.hasNext()) {
                File nextRegionFile = regionFileIt.next();
                IBlockStoreRegion nextRegion = reader.readRegion(blockStore, nextRegionFile);

                Iterator<IBlockStoreChunk> chunkIt = nextRegion.iterator();
                while (chunkIt.hasNext()) {
                    IBlockStoreChunk chunk = chunkIt.next();
                    BlockStoreChunkPlacement placement = new BlockStoreChunkPlacement(chunk);
                    placement.rotate(structure.getDirection().getRotation());
                    placement.setReversed(true);
                    StructurePlacingTask task = new AWEPlacementTask(
                            asyncWorldEdit,
                            entry,
                            placement,
                            getPlayer(),
                            getEditSession(),
                            structureMin.add(chunk.getX(), 0, chunk.getZ())
                    );
                    task.setOptions(placeOptions);
                    entry.addTask(task);
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
