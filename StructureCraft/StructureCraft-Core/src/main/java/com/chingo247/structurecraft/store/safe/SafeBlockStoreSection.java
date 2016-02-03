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
package com.chingo247.structurecraft.store.safe;

import com.chingo247.structurecraft.store.BlockStoreSection;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.blocks.BaseBlock;
import java.util.Map;

/**
 *
 * @author Chingo
 */
public class SafeBlockStoreSection extends BlockStoreSection implements ISafeBlockStoreSection {
    
    private final byte[] written;
    private boolean dirty;
    
    public SafeBlockStoreSection(ISafeBlockStoreChunk bsc, Map<String, Tag> sectionTagMap, int y, int sectionHeight) {
        super(bsc, sectionTagMap, y, sectionHeight);
        this.written = new byte[numBlocks()];
    }

    @Override
    public boolean isWritten(int x, int y, int z) {
        int index = getArrayIndex(x, y, z);
        return written[index] == 1;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    @Override
    public void setWritten(int x, int y, int z) {
        int index = getArrayIndex(x, y, z);
        this.written[index] = 1;
    }

    @Override
    public void setBlockAt(int x, int y, int z, BaseBlock block) {
        if(!isWritten(x, y, z)) {
            super.setBlockAt(x, y, z, block); //To change body of generated methods, choose Tools | Templates.
            setWritten(x, y, z);
            setDirty(true);
        }
    }
    
    
    
}
