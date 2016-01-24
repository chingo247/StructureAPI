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
import com.chingo247.structurecraft.construction.contract.safe.schematic.IOSchematicSafeData;
import com.chingo247.structurecraft.construction.contract.safe.schematic.SchematicSafeData;
import com.chingo247.structurecraft.construction.listener.ConstructionListener;
import com.chingo247.structurecraft.construction.producer.IPlacementProducer;
import com.chingo247.structurecraft.exeption.StructureException;
import com.chingo247.structurecraft.model.structure.IStructure;
import com.chingo247.structurecraft.placement.IPlacement;
import com.chingo247.structurecraft.placement.StructureBlock;
import com.chingo247.structurecraft.placement.block.IBlockPlacement;
import com.chingo247.structurecraft.placement.options.PlaceOptions;
import com.chingo247.structurecraft.util.RegionUtil;
import com.chingo247.structurecraft.util.iterator.CuboidIterator;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.World;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.PriorityQueue;
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
        SchematicSafeData safeBlockData;
        if (rollbackFile.exists()) {
            try {
                System.out.println("Using existing Schematic Save Data");
                safeBlockData = IOSchematicSafeData.read(rollbackFile);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            System.out.println("Creating new Schematic Save Data");
            safeBlockData = new SchematicSafeData(placement.getWidth(), placement.getHeight(), placement.getLength());
        }

        System.out.println("Area: " + placementArea);

        // Create place areas...
        IContract entryContract = entry.getContract();
        UUID player = entryContract.getPlayer();
        World world = entryContract.getEditSession().getWorld();
        EditSession editSession = entryContract.getEditSession();

        PlaceOptions option = contract.getPlaceOptions() != null ? contract.getPlaceOptions() : new PlaceOptions();

        Iterator<Vector> traversalSafe = makeTraversal(placement, option);
        Iterator<Vector> traversalPlace = makeTraversal(placement, option);
        PriorityQueue<StructureBlock> placeLater = new PriorityQueue<>();

        Vector size = placement.getSize();
        int cubeX = option.getCubeX() <= 0 ? size.getBlockX() : option.getCubeX();
        int cubeY = option.getCubeY() <= 0 ? size.getBlockY() : option.getCubeY();
        int cubeZ = option.getCubeZ() <= 0 ? size.getBlockZ() : option.getCubeZ();
        int cubeblocks = cubeX * cubeY * cubeZ;
        
        System.out.println("CUBE BLOCKS: " + cubeblocks);

        int totalBlocks = placement.getWidth() * placement.getHeight() * placement.getLength();
        System.out.println("TOTAL BLOCKS: " + totalBlocks);
        int countBlock = 0;

        while (countBlock < totalBlocks) {
            entry.addTask(new SafeTask(entry, player, placement, world, safeBlockData, rollbackFile, traversalSafe, cubeblocks));
            SafePlacement safePlacement = new SafePlacement(placement, traversalPlace, cubeblocks, placeLater);
            entry.addTask(new AWEPlacementTask(
                    asyncWorldEdit, entry, safePlacement, player, editSession, structure.getMin())
            );
            countBlock += cubeblocks;
        }

        // Empties the last blocks in placelater-queue
        SafePlacement safePlacement = new SafePlacement(placement, traversalPlace, cubeblocks, placeLater);
        safePlacement.setLast(true);
        entry.addTask(new AWEPlacementTask(
                asyncWorldEdit, entry, safePlacement, player, editSession, structure.getMin())
        );

    }

    private Iterator<Vector> makeTraversal(IPlacement placement, PlaceOptions option) {

        Vector size = placement.getSize();

        return new CuboidIterator(
                option.getCubeX() <= 0 ? size.getBlockX() : option.getCubeX(),
                option.getCubeY() <= 0 ? size.getBlockY() : option.getCubeY(),
                option.getCubeY() <= 0 ? size.getBlockY() : option.getCubeY()
        ).iterate(size);
    }

}
