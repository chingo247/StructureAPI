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

import com.chingo247.structurecraft.store.BlockStoreChunk;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.blocks.BaseBlock;
import java.util.Map;

/**
 *
 * @author Chingo
 */
public class SafeBlockStoreChunk extends BlockStoreChunk implements ISafeBlockStoreChunk {
    
    private boolean dirty;
    
    public SafeBlockStoreChunk(SafeBlockStore blockStore, Map<String, Tag> chunkTagMap, int x, int z, Vector2D dimension) {
        super(blockStore, chunkTagMap, x, z, dimension);
    }

    @Override
    public boolean isWritten(int x, int y, int z) {
        ISafeBlockStoreSection section = (SafeBlockStoreSection) getSection(y);
        int sectionY = (y >> 4) * 16;
        return section.isWritten(x, y - sectionY, z);
    }

    @Override
    public void setBlockAt(int x, int y, int z, BaseBlock block) {
        super.setBlockAt(x, y, z, block); //To change body of generated methods, choose Tools | Templates.
        setDirty(true);
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
        ISafeBlockStoreSection section = (SafeBlockStoreSection) getSection(y);
        int sectionY = (y >> 4) * 16;
        section.setWritten(x, y - sectionY, z);
    }
    
}
