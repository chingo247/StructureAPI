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
package com.chingo247.structureapi.construction.contract;

import com.chingo247.settlercraft.core.Direction;
import com.chingo247.structureapi.StructureAPI;
import com.chingo247.blockstore.safe.SafeBlockStore;
import com.chingo247.blockstore.safe.SafeBlockStoreReader;
import com.chingo247.structureapi.construction.StructureEntry;
import com.chingo247.structureapi.construction.awe.AWEPlacementTask;
import com.chingo247.structureapi.construction.listener.ConstructionListener;
import com.chingo247.structureapi.construction.producer.IPlacementProducer;
import com.chingo247.structureapi.exeption.StructureException;
import com.chingo247.structureapi.model.structure.Structure;
import com.chingo247.structureapi.placement.StructureBlock;
import com.chingo247.structureapi.placement.block.IBlockPlacement;
import com.chingo247.structureapi.placement.options.PlaceOptions;
import com.chingo247.blockstore.safe.SafeBlockStoreWriter;
import com.chingo247.settlercraft.core.SettlerCraft;
import com.chingo247.structureapi.construction.task.StructurePlacingTask;
import com.chingo247.structureapi.construction.task.StructureTask;
import com.chingo247.structureapi.model.structure.RollbackData;
import com.chingo247.structureapi.placement.RotationalPlacement;
import com.chingo247.structureapi.placement.block.BlockPlacement;
import com.chingo247.structureapi.placement.options.BlockMask;
import com.chingo247.structureapi.placement.options.BlockPredicate;
import com.chingo247.structureapi.util.WorldUtil;
import com.chingo247.structureapi.util.iterator.CuboidIterator;
import com.chingo247.xplatform.core.IPlugin;
import com.chingo247.xplatform.core.IScheduler;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.World;
import java.io.IOException;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import org.primesoft.asyncworldedit.api.IAsyncWorldEdit;

/**
 *
 * @author Chingo
 */
public class SafeContract extends Contract {

    private static final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(SafeContract.class.getName());

    private static final int CHUNK_SIZE = 16;
    private static final int CHUNK_HEIGHT = 256;
    private static final int MAX_BLOCKS_PER_TASK = CHUNK_SIZE * CHUNK_SIZE * CHUNK_HEIGHT;

    private Contract contract;

    /**
     * Constructor
     *
     * @param contract The contract, may NOT inherit from RollbackContract
     */
    public SafeContract(Contract contract) {
        Preconditions.checkNotNull(contract, "contract may not be null");
        Preconditions.checkArgument(!(contract instanceof RollbackContract), "RollbackContract is not allowed!");
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
    public void apply(StructureEntry entry, PlaceOptions placeOptions) throws StructureException {
        Structure structure = entry.getStructure();
        IAsyncWorldEdit asyncWorldEdit = StructureAPI.getInstance().getAsyncWorldEditIntegration().getAsyncWorldEdit();
        IBlockPlacement placement = getPlacementProducer().produce(structure);

        CuboidRegion region = structure.getCuboidRegion();

        // Get or create rollback data
        RollbackData data = structure.getRollbackData();
        SafeBlockStore safeBlockStore;
        if (data.hasBlockStore()) {
            try {
                SafeBlockStoreReader reader = new SafeBlockStoreReader();
                safeBlockStore = reader.read(data.getBlockStoreDirectory());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            safeBlockStore = new SafeBlockStore(data.getBlockStoreDirectory(), region.getWidth(), region.getHeight(), region.getLength());
        }

        // Create place areas...
        Contract entryContract = entry.getContract();
        UUID player = entryContract.getPlayer();
        World world = SettlerCraft.getInstance().getWorld(entry.getStructure().getWorldUUID());

        Iterator<Vector> traversalSafe = new CuboidIterator(
                placeOptions.getCubeX() < 0 ? placement.getWidth() : placeOptions.getCubeX(),
                placeOptions.getCubeY() < 0 ? placement.getHeight() : placeOptions.getCubeY(),
                placeOptions.getCubeZ() < 0 ? placement.getLength() : placeOptions.getCubeZ()
        ).iterate(placement.getSize());

        Iterator<Vector> traversalPlace = new CuboidIterator(
                placeOptions.getCubeX() < 0 ? placement.getWidth() : placeOptions.getCubeX(),
                placeOptions.getCubeY() < 0 ? placement.getHeight() : placeOptions.getCubeY(),
                placeOptions.getCubeZ() < 0 ? placement.getLength() : placeOptions.getCubeZ()
        ).iterate(placement.getSize());

        PriorityQueue<StructureBlock> placeLater = new PriorityQueue<>();

        int totalBlocks = placement.getWidth() * placement.getHeight() * placement.getLength();

        int countBlock = 0;

        while (countBlock < totalBlocks) {
            SafeTask task = new SafeTask(entry, player, placement, world, safeBlockStore, traversalSafe, MAX_BLOCKS_PER_TASK);
            entry.addTask(task);
            SafePlacement safePlacement = new SafePlacement(placement, traversalPlace, MAX_BLOCKS_PER_TASK, placeLater);

            EditSession editSession = entryContract.getEditSessionFactory().createEditSession(structure, player);
            StructurePlacingTask placingTask = new AWEPlacementTask(
                    asyncWorldEdit, entry, safePlacement, player, editSession, structure.getMin());
            placingTask.setOptions(placeOptions);
            entry.addTask(placingTask);
            countBlock += MAX_BLOCKS_PER_TASK;
        }

        entry.addListener(getConstructionListener());

        // Empties the last blocks in placelater-queue
        SafePlacement safePlacement = new SafePlacement(placement, traversalPlace, MAX_BLOCKS_PER_TASK, placeLater);
        safePlacement.setLast(true);

        EditSession editSession = entryContract.getEditSessionFactory().createEditSession(structure, player);
        StructurePlacingTask task = new AWEPlacementTask(
                asyncWorldEdit, entry, safePlacement, player, editSession, structure.getMin());
        task.setOptions(placeOptions);
        entry.addTask(task);

    }

    private class SafeTask extends StructureTask {

        /**
         * The world that contains the region.
         */
        private final World world;
        /**
         * The schematicSaveData object.
         */
        private SafeBlockStore safeBlockStore;

        private int maxBlocks;
        private Iterator<Vector> traversal;
        private IBlockPlacement placement;

        /**
         * Constructor.
         *
         * @param entry The construction entry
         * @param submitter The player that has submitted the task
         * @param toSave The region that needs to be saved
         * @param world The world that contains the region
         * @param safeBlockData The safe block data that will be used to save
         * @param callback
         */
        public SafeTask(StructureEntry entry, UUID submitter, IBlockPlacement placement, World world, SafeBlockStore safeBlockStore, Iterator<Vector> traversal, int maxBlocks) {
            super(entry, submitter);
            this.world = world;
            this.traversal = traversal;
            this.maxBlocks = maxBlocks;
            this.placement = placement;
            this.safeBlockStore = safeBlockStore;
        }

        @Override
        protected void execute() {
            final IPlugin plugin = StructureAPI.getInstance().getPlugin();
            final IScheduler scheduler = plugin.getScheduler();
            final Vector position = getConstructionEntry().getStructure().getMin();

            scheduler.run(new Runnable() {

                @Override
                public void run() {
                    try {
                        int count = 0;

                        final Direction d;
                        if (placement instanceof RotationalPlacement) {
                            d = WorldUtil.getDirection(((RotationalPlacement) placement).getRotation());
                        } else {
                            d = Direction.EAST; // Default
                        }

//                    System.out.println(" ");
//                    System.out.println("SAFE TASK ");
                        int width = placement.getWidth();
                        int length = placement.getLength();

                        while (count < maxBlocks && traversal.hasNext()) {
                            Vector v = traversal.next();

                            Vector b;
                            Vector p;
                            switch (d) {
                                case EAST:
                                    b = v;
                                    p = position.add(b);
                                    break;
                                case WEST:
                                    b = new BlockVector((-v.getBlockX()) + (width - 1), v.getBlockY(), (-v.getBlockZ()) + (length - 1));
                                    p = position.add(b);
                                    break;
                                case NORTH:
                                    b = new BlockVector(v.getBlockZ(), v.getBlockY(), (-v.getBlockX()) + (width - 1));
                                    p = position.add(b);
                                    break;
                                case SOUTH:
                                    b = new BlockVector((-v.getBlockZ()) + (length - 1), v.getBlockY(), v.getBlockX());
                                    p = position.add(b);
                                    break;
                                default:
                                    throw new AssertionError("unreachable");
                            }
                            BaseBlock block = world.getBlock(p);
                            safeBlockStore.setBlockAt(p.subtract(position), block);

                            count++;
                        }
//                    System.out.println(" ");

                        // IO Operation should be performed async
                        ExecutorService es = StructureAPI.getInstance().getExecutor();
                        es.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    SafeBlockStoreWriter writer = new SafeBlockStoreWriter();
                                    writer.save(safeBlockStore);
                                } catch (Exception e) {
                                    setFailed(true);
                                    LOG.log(Level.SEVERE, e.getMessage(), e);
                                } finally {
                                    finish();
                                }
                            }
                        });
                    } catch (Exception e) {
                        LOG.log(Level.SEVERE, e.getMessage(), e);
                    }

                }
            });

        }

        @Override
        protected void onCancel() {
        }

    }

    private class SafePlacement extends BlockPlacement {

        private Iterator<Vector> traversal;
        private int maxBlocks;
        private IBlockPlacement placement;
        private Queue<StructureBlock> placeLater;
        private final Direction d;
        private boolean last = false;

        public SafePlacement(IBlockPlacement blockPlacement, Iterator<Vector> traversal, int maxBlocks, PriorityQueue<StructureBlock> placeLater) {
            super(blockPlacement.getWidth(), blockPlacement.getHeight(), blockPlacement.getLength());

            this.maxBlocks = maxBlocks;
            this.traversal = traversal;
            this.placement = blockPlacement;
            this.placeLater = placeLater;

            if (placement instanceof RotationalPlacement) {
                d = WorldUtil.getDirection(((RotationalPlacement) blockPlacement).getRotation());
            } else {
                d = Direction.EAST; // Default
            }
        }

        public boolean isLast() {
            return last;
        }

        public void setLast(boolean last) {
            this.last = last;
        }

        @Override
        public BaseBlock getBlock(Vector position) {
            return placement.getBlock(position);
        }

        @Override
        public void place(EditSession editSession, Vector pos, PlaceOptions option) {

            int placeLaterPlaced = 0;
            int placeLaterPause = 0;
            int count = 0;

            // Cube traverse this clipboard
            while (traversal.hasNext() && count < maxBlocks) {
                Vector v = traversal.next();
                BaseBlock clipboardBlock = getBlock(v);

                if (clipboardBlock == null) {
                    continue;
                }

                int priority = getPriority(clipboardBlock);

                if (priority == PRIORITY_FIRST) {
                    doBlock(editSession, pos, v, clipboardBlock, option);
                } else {
                    placeLater.add(new StructureBlock(v, clipboardBlock));
                }

                // For every X place intensive blocks
                if (placeLaterPause > 0 && clipboardBlock.getId() != 0) {
                    placeLaterPause--;
                } else {

                    // only place these when having a greater xz-cubevalue to avoid placing torches etc in air and break them later
                    while (placeLater.peek() != null
                            && (placeLater.peek().getPosition().getBlockY() < (option.getCubeY() * (Math.ceil(v.getBlockY() / option.getCubeY())))
                            || placeLater.peek().getPosition().getBlockX() < (option.getCubeX() * (Math.ceil(v.getBlockX() / option.getCubeX())))
                            || placeLater.peek().getPosition().getBlockZ() < (option.getCubeZ() * (Math.ceil(v.getBlockZ() / option.getCubeZ()))))) {
                        StructureBlock plb = placeLater.poll();
                        doBlock(editSession, pos, plb.getPosition(), plb.getBlock(), option);

                        placeLaterPlaced++;

                        if (plb.getPriority() == PRIORITY_LIQUID || BlockType.emitsLight(plb.getBlock().getId())) {
                            placeLaterPlaced++;
                        }

                        if (placeLaterPlaced >= MAX_PLACE_LATER_TO_PLACE) {
                            placeLaterPause = BLOCK_BETWEEN;
                            placeLaterPlaced = 0;
                        }
                    }
                }
                count++;
            }

//        System.out.println(" ");
            if (isLast()) {
                // Empty the queue
                while (placeLater.peek() != null) {
                    StructureBlock plb = placeLater.poll();
                    doBlock(editSession, pos, plb.getPosition(), plb.getBlock(), option);
                }
            }
        }

        @Override
        protected void doBlock(EditSession editSession, Vector position, Vector blockPosition, BaseBlock block, PlaceOptions option) {
            Vector p;

            switch (d) {
                case EAST:
                    p = position.add(blockPosition);
                    break;
                case WEST:
                    p = position.add((-blockPosition.getBlockX()) + (placement.getWidth() - 1), blockPosition.getBlockY(), (-blockPosition.getBlockZ()) + (placement.getLength() - 1));
                    block.rotate90();
                    block.rotate90();
                    break;
                case NORTH:
                    p = position.add(blockPosition.getBlockZ(), blockPosition.getBlockY(), (-blockPosition.getBlockX()) + (getWidth() - 1));
                    block.rotate90Reverse();
                    break;
                case SOUTH:
                    p = position.add((-blockPosition.getBlockZ()) + (placement.getLength() - 1), blockPosition.getBlockY(), blockPosition.getBlockX());
                    block.rotate90();
                    break;
                default:
                    throw new AssertionError("unreachable");
            }

            for (BlockPredicate bp : option.getIgnore()) {
                if (bp.evaluate(blockPosition, p, block)) {
                    return;
                }
            }

            for (BlockMask bm : option.getBlockMasks()) {
                bm.apply(blockPosition, p, block);
            }

            editSession.rawSetBlock(p, block);
        }

    }

}
