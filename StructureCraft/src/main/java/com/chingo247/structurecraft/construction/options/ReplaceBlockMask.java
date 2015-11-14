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
package com.chingo247.structurecraft.construction.options;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;

/**
 *
 * @author Chingo
 */
public class ReplaceBlockMask implements BlockMask {

    private final BlockPredicate predicate;
    private final int replacingMaterial;
    private final int replacingData;

    public ReplaceBlockMask(final BlockPredicate predicate, int material, int data) {
        this.predicate = predicate;
        this.replacingMaterial = material;
        this.replacingData = data;
        
        
    }

    @Override
    public BaseBlock apply(final Vector relativePosition, final Vector worldPosition, BaseBlock block) {
        if (predicate.evaluate(relativePosition, worldPosition, block)) {
            if (block == null) {
                block = new BaseBlock(replacingMaterial, replacingData);
            } else {

                if (replacingMaterial >= 0) {
                    block.setId(replacingMaterial);
                }
                if (replacingData >= 0) {
                    block.setData(replacingData);
                }
            }
        }
        return block;
    }

}
