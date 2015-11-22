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

package com.chingo247.structurecraft.placement;

import com.chingo247.structurecraft.placement.interfaces.IBlockPlacement;
import static com.chingo247.settlercraft.core.Direction.EAST;
import static com.chingo247.settlercraft.core.Direction.NORTH;
import static com.chingo247.settlercraft.core.Direction.SOUTH;
import static com.chingo247.settlercraft.core.Direction.WEST;
import com.chingo247.structurecraft.util.iterator.CuboidIterator;
import com.chingo247.structurecraft.construction.options.BlockMask;
import com.chingo247.structurecraft.construction.options.BlockPredicate;
import com.chingo247.structurecraft.construction.options.PlaceOptions;
import com.chingo247.structurecraft.util.WorldUtil;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.BlockType;
import java.util.Iterator;
import java.util.PriorityQueue;

/**
 *
 * @author Chingo
 */
public abstract class BlockPlacement extends AbstractPlacement implements IBlockPlacement {

    protected static final int PRIORITY_REDSTONE = 5;
    protected static final int PRIORITY_FIRST = 4;
    protected static final int PRIORITY_LIQUID = 3;
    protected static final int PRIORITY_LATER = 2;
    protected static final int PRIORITY_FINAL = 1;

    protected final int BLOCK_BETWEEN;
    protected final int MAX_PLACE_LATER_TO_PLACE = 10;

    public BlockPlacement(int width, int height, int length) {
        super(width, height, length);
        this.BLOCK_BETWEEN = Math.round((float) ((getBlocks() * 0.001)));
    }

    public BlockPlacement(int rotation, Vector relativePosition, int width, int height, int length) {
        super(rotation, relativePosition, width, height, length);
        this.BLOCK_BETWEEN = Math.round((float) ((getBlocks() * 0.01)));
    }

    @Override
    public final int getBlocks() {
        return getWidth() * getHeight() * getLength();
    }

    @Override
    public void place(EditSession editSession, Vector pos, PlaceOptions option) {
        
        Iterator<Vector> traversal = new CuboidIterator(
                option.getCubeX() <= 0 ? getSize().getBlockX() : option.getCubeX(),
                option.getCubeY() <= 0 ? getSize().getBlockY() : option.getCubeY(),
                option.getCubeZ() <= 0 ? getSize().getBlockZ() : option.getCubeZ()
        ).iterate(getSize());
        
        
        PriorityQueue<StructureBlock> placeLater = new PriorityQueue<>();

        int placeLaterPlaced = 0;
        int placeLaterPause = 0;

        // Cube traverse this clipboard
        while (traversal.hasNext()) {
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
                        && placeLater.peek().getPosition().getBlockY() < v.getBlockY()) {
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

        }
        // Empty the queue
        while (placeLater.peek() != null) {
            StructureBlock plb = placeLater.poll();
            doBlock(editSession, pos, plb.getPosition(), plb.getBlock(), option);
        }
        
    }

    protected int getPriority(BaseBlock block) {
        if (isWater(block) || isLava(block)) {
            return PRIORITY_LIQUID;
        }
        
        if (BlockType.shouldPlaceLast(block.getId()) || BlockType.emitsLight(block.getId())) {
            return PRIORITY_LATER;
        }

        if (BlockType.shouldPlaceFinal(block.getId())) {
            return PRIORITY_FINAL;
        }

        return PRIORITY_FIRST;

    }

    protected boolean isLava(BaseBlock b) {
        int bi = b.getType();
        if (bi == BlockID.LAVA || bi == BlockID.STATIONARY_LAVA) {
            return true;
        }
        return false;
    }

    protected boolean isWater(BaseBlock b) {
        int bi = b.getType();
        if (bi == BlockID.WATER || bi == BlockID.STATIONARY_WATER) {
            return true;
        }
        return false;
    }

    @Override
    public abstract BaseBlock getBlock(Vector position);
    
    public BaseBlock getBlock(int x, int y, int z) {
        return getBlock(new BlockVector(x, y, z));
    }

    protected void doBlock(EditSession editSession, Vector position, Vector blockPosition, BaseBlock block, PlaceOptions option) {
        Vector p;
        
        switch (WorldUtil.getDirection(getRotation())) {
            case EAST:
                p = position.add(blockPosition);
                break;
            case WEST:
                p = position.add((-blockPosition.getBlockX()) + (getWidth() - 1), blockPosition.getBlockY(), (-blockPosition.getBlockZ()) + (getLength() - 1));
                block.rotate90();
                block.rotate90();
                break;
            case NORTH:
                p = position.add(blockPosition.getBlockZ(), blockPosition.getBlockY(), (-blockPosition.getBlockX()) + (getWidth() - 1));
                block.rotate90Reverse();
                break;
            case SOUTH:
                p = position.add((-blockPosition.getBlockZ()) + (getLength() - 1), blockPosition.getBlockY(), blockPosition.getBlockX());
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
