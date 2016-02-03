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
package com.chingo247.structurecraft.store;

import com.sk89q.jnbt.Tag;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Chingo
 */
public class BlockStoreSectionFactory implements IBlockStoreSectionFactory<IBlockStoreSection> {
    
    private BlockStoreChunk chunk;

    public BlockStoreSectionFactory(BlockStoreChunk BlockStoreChunk) {
        this.chunk = BlockStoreChunk;
    }
    
    @Override
    public IBlockStoreSection newSection(Tag sectionTagOrNull, int y, int sectionHeight) {
        IBlockStoreSection section;
        if(sectionTagOrNull == null) {
            section = new BlockStoreSection(chunk, new HashMap<String, Tag>(), y, sectionHeight);
        } else {
            section = new BlockStoreSection(chunk, (Map) sectionTagOrNull.getValue(), y, sectionHeight);
        }
        return section;
    }
    
}
