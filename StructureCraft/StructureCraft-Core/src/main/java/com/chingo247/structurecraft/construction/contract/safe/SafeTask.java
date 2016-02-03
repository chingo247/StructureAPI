/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.construction.contract.safe;

import com.chingo247.settlercraft.core.Direction;
import com.chingo247.structurecraft.StructureAPI;
import com.chingo247.structurecraft.construction.task.StructureTask;
import com.chingo247.structurecraft.platform.IStructureAPIPlugin;
import com.chingo247.xplatform.core.IScheduler;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.world.World;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import com.chingo247.structurecraft.construction.IStructureEntry;
import com.chingo247.structurecraft.placement.RotationalPlacement;
import com.chingo247.structurecraft.placement.block.IBlockPlacement;
import com.chingo247.structurecraft.store.safe.SafeBlockStore;
import com.chingo247.structurecraft.util.WorldUtil;
import com.sk89q.worldedit.BlockVector;
import java.io.File;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Chingo
 */
class SafeTask extends StructureTask {

    private static final Logger LOG = LoggerFactory.getLogger(SafeTask.class);

    /**
     * The world that contains the region.
     */
    private final World world;
    /**
     * The schematicSaveData object.
     */
    private SafeBlockStore safeBlockStore;

    private File safeDataFile;

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
    public SafeTask(IStructureEntry entry, UUID submitter, IBlockPlacement placement, World world, SafeBlockStore safeBlockStore, Iterator<Vector> traversal, int maxBlocks) {
        super(entry, submitter);
        this.world = world;
        this.traversal = traversal;
        this.maxBlocks = maxBlocks;
        this.placement = placement;
        this.safeBlockStore = safeBlockStore;
    }

    @Override
    protected void execute() {
        final IStructureAPIPlugin plugin = StructureAPI.getInstance().getPlugin();
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
                        System.out.println("SafeTask: " + d);

                        BaseBlock block = world.getBlock(p);

                        System.out.println("Saving: " + p.subtract(position));
                        
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
                                safeBlockStore.save();
                            } catch (Exception e) {
                                setFailed(true);
                                LOG.error(e.getMessage(), e);
                            } finally {
                                finish();
                            }
                        }
                    });
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }

            }
        });

    }

    @Override
    protected void onCancel() {
    }

}
