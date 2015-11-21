
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
package com.chingo247.structurecraft.construction.block;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.BlockType;

/**
 *
 * @author Chingo
 */
public class StructureBlock implements Comparable<StructureBlock> {

    private static final int PRIORITY_FIRST = 4;
    private static final int PRIORITY_LIQUID = 3;
    private static final int PRIORITY_LATER = 2;
    private static final int PRIORITY_FINAL = 1;

    private final Vector position;
    private final BaseBlock block;

    public StructureBlock(Vector p, BaseBlock b) {
        this.position = p;
        this.block = b;
    }

    public BaseBlock getBlock() {
        return block;
    }

    public Vector getPosition() {
        return position;
    }

    @Override
    public int compareTo(StructureBlock o) {

        int v = this.getPriority().compareTo(o.getPriority());
        if(v == 0) {
            Integer yMe = getPosition().getBlockY();
            Integer yO = getPosition().getBlockY();
            if(yO.compareTo(yMe) == 0) {
                Integer xMe = getPosition().getBlockX();
                Integer xO = getPosition().getBlockX();
                return xMe.compareTo(xO);
            }
            return yO.compareTo(yMe);
        }
        
        return v;
    }

    public Integer getPriority() {
        if (isWater(block) || isLava(block))  {
            return PRIORITY_LIQUID;
        }

        if (BlockType.shouldPlaceLast(block.getId())) {
            return PRIORITY_LATER;
        }

        if (BlockType.shouldPlaceFinal(block.getId())) {
            return PRIORITY_FINAL;
        }

        return PRIORITY_FIRST;

    }

    private boolean isLava(BaseBlock b) {
        Integer bi = b.getType();
        if (bi == BlockID.LAVA || bi == BlockID.STATIONARY_LAVA) {
            return true;
        }
        return false;
    }

    private boolean isWater(BaseBlock b) {
        Integer bi = b.getType();
        if (bi == BlockID.WATER || bi == BlockID.STATIONARY_WATER) {
            return true;
        }
        return false;
    }

}
