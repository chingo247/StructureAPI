/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.construction.save.schematic;

import com.chingo247.structurecraft.StructureAPI;
import com.chingo247.structurecraft.construction.IConstructionEntry;
import com.chingo247.structurecraft.construction.StructureTask;
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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chingo
 */
class SchematicSavingTask extends StructureTask {

    /**
     * Logger.
     */
    private static final Logger LOG = Logger.getLogger(SchematicSavingTask.class.getName());

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
    public SchematicSavingTask(IConstructionEntry entry, UUID submitter, CuboidRegion toSave, World world, SchematicSaveData safeBlockData) {
        super(entry, submitter);
        this.toSave = toSave;
        this.world = world;
        this.safeBlockData = safeBlockData;
    }

    @Override
    protected void execute() {
        final ExecutorService executorService = StructureAPI.getInstance().getExecutor();
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
                            LOG.log(Level.SEVERE, e.getMessage(), e);
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

            for (int x = min.getBlockX(), relX = 0; x < max.getBlockX(); x++, relX++) {
                for (int z = min.getBlockZ(), relZ = 0; z < max.getBlockZ(); z++, relZ++) {
                    for (int y = min.getBlockY(), relY = 0; y < max.getBlockY(); y++, relY++) {
                        Vector relativePosition = new BlockVector(relX, relY, relZ);
                        Vector worldPos = new BlockVector(x + pos.getBlockX(), y + pos.getBlockY(), z + pos.getBlockZ());
                        BaseBlock b = world.getBlock(worldPos);
                        safeBlockData.setBlock(relativePosition, b);
                    }
                }
            }
        }

    }
}
