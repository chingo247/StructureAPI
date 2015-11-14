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

import com.chingo247.structurecraft.construction.options.BuildOptions;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;

/**
 *
 * @author Chingo
 */
public class StructureLotPlacement extends AbstractPlacement<BuildOptions> {
    
    public StructureLotPlacement(Vector position, int width, int height, int length) {
        super(0, position, width, height, length);
    }

    @Override
    public void place(EditSession editSession, Vector pos, BuildOptions options) {
        // Does nothing at the moment... As StructureLot's have a different purpose
    }

    @Override
    public String getTypeName() {
        return PlacementTypes.STRUCTURE_LOT;
    }

    
}
