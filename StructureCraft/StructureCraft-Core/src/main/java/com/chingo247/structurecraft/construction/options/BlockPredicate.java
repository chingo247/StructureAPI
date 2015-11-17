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
public interface BlockPredicate {
    
    public static final BlockPredicate TRUE = new BlockPredicate() {

        @Override
        public boolean evaluate(Vector position, Vector worldPosition, BaseBlock block) {
            return true;
        }
    };
    
    /**
     * Evaluates the position
     * @param position The relative position to evaluate
     * @param worldPosition
     * @param block The block to evaluate
     * @return true if evaluation expression is true
     */
    public boolean evaluate(Vector position, Vector worldPosition, BaseBlock block);
    
}
