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

import com.chingo247.settlercraft.core.Direction;
import com.chingo247.structurecraft.construction.StructureEntry;
import com.chingo247.structurecraft.placement.RotationalPlacement;
import com.chingo247.structurecraft.placement.StructureBlock;
import com.chingo247.structurecraft.placement.block.BlockPlacement;
import com.chingo247.structurecraft.placement.block.IBlockPlacement;
import com.chingo247.structurecraft.placement.options.BlockMask;
import com.chingo247.structurecraft.placement.options.BlockPredicate;
import com.chingo247.structurecraft.placement.options.PlaceOptions;
import com.chingo247.structurecraft.util.WorldUtil;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockType;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 *
 * @author Chingo
 */
class SafePlacement extends BlockPlacement {

    private Iterator<Vector> traversal;
    private int maxBlocks;
    private IBlockPlacement placement;
    private Queue<StructureBlock> placeLater;
    private final Direction d;
    private boolean last = false;
    private StructureEntry entry;

    public SafePlacement( IBlockPlacement blockPlacement, Iterator<Vector> traversal, int maxBlocks, PriorityQueue<StructureBlock> placeLater) {
        super(blockPlacement.getWidth(), blockPlacement.getHeight(), blockPlacement.getLength());

        this.maxBlocks = maxBlocks;
        this.traversal = traversal;
        this.placement = blockPlacement;
        this.placeLater = placeLater;
        if (placement instanceof RotationalPlacement) {
            d = WorldUtil.getDirection(((RotationalPlacement) placement).getRotation());
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

//        System.out.println(" ");
//        System.out.println(" SAFE PLACEMENT");
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
            count++;
        }
        
//        System.out.println(" ");
        
        if(isLast()) {
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
        
//        System.out.println("PlacePos: " + p +  ", rel: " + blockPosition);

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
