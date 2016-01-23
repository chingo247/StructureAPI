/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.construction.contract.safe;

import com.chingo247.structurecraft.StructureAPI;
import com.chingo247.structurecraft.construction.task.StructureTask;
import com.chingo247.structurecraft.platform.IStructureAPIPlugin;
import com.chingo247.structurecraft.util.concurrent.AsyncLoad;
import com.chingo247.structurecraft.util.concurrent.AsyncTask;
import com.chingo247.xplatform.core.IScheduler;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.World;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import com.chingo247.structurecraft.construction.IStructureEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Chingo
 */
class SchematicSavingTask extends StructureTask {
    
    private static final Logger LOG = LoggerFactory.getLogger(SchematicSavingTask.class);

    /**
     * The region that needs to be saved.
     */
    private final CuboidRegion toSave;
    /**
     * The world that contains the region.
     */
    private final World world;
    /**
     * The schematicSaveData object.
     */
    private SchematicSaveData safeBlockData;

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
    public SchematicSavingTask(IStructureEntry entry, UUID submitter, CuboidRegion toSave, World world, SchematicSaveData safeBlockData) {
        super(entry, submitter);
        this.toSave = toSave;
        this.world = world;
        this.safeBlockData = safeBlockData;
    }

    @Override
    protected void execute() {
        final IStructureAPIPlugin plugin = StructureAPI.getInstance().getPlugin();
        final IScheduler scheduler = plugin.getScheduler();
        final Vector pos = getConstructionEntry().getStructure().getMin();

        // run sync as we use the platform's API
        scheduler.runSync(new SchematicSavingRunnable(pos) {
            /**
             * We retrieved blocks from the world
             */
            @Override
            public void succes() {
                // IO Operation should be performed async
                ExecutorService es = StructureAPI.getInstance().getExecutor();
                es.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            safeBlockData.write();
                        } catch (Exception e) {
                            setFailed(true);
                            safeBlockData = null;
                            LOG.error(e.getMessage(), e);
                        } finally {
                            finish();
                        }
                    }
                });
            }

            /**
             * Retrieving blocks from the world has failed
             */
            @Override
            public void fail() {
                setFailed(true);
                finish();
            }
        });

    }

    @Override
    protected void onCancel() {
    }

    /**
     * Defines a runnable that will save world data to a schematic
     */
    private abstract class SchematicSavingRunnable extends AsyncTask {

        private final Vector pos;

        /**
         * Constructor.
         *
         * @param pos The starting position
         */
        public SchematicSavingRunnable(final Vector pos) {
            this.pos = pos;
        }

        @Override
        public void execute() throws Exception {
            // Cube traverse this clipboard
            Vector min = toSave.getMinimumPoint();
            Vector max = toSave.getMaximumPoint();
            
            System.out.println("Min: " + min + " Max: " + max);

            for (int x = min.getBlockX(); x < max.getBlockX(); x++) {
                for (int z = min.getBlockZ(); z < max.getBlockZ(); z++) {
                    for (int y = min.getBlockY(); y < max.getBlockY(); y++) {
                        Vector relativePosition = new BlockVector(x, y, z);
                        Vector worldPos = new BlockVector(x + pos.getBlockX(), y + pos.getBlockY(), z + pos.getBlockZ());
                        BaseBlock b = world.getBlock(worldPos);
                        
                        LOG.info("WorldPos: " + worldPos + ", RelPos: " + relativePosition);
                        
                        safeBlockData.setBlock(relativePosition, b);
                    }
                }
            }
        }

    }
}
