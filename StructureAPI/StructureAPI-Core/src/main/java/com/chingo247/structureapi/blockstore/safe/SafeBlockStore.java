/*
 * The MIT License
 *
 * Copyright 2016 Chingo.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.chingo247.structureapi.blockstore.safe;

import com.chingo247.structureapi.blockstore.BlockStore;
import com.chingo247.structureapi.blockstore.IBlockStoreRegion;
import com.sk89q.jnbt.Tag;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 *
 * @author Chingo
 */
public class SafeBlockStore extends BlockStore {

    public SafeBlockStore(File directory, int width, int height, int length) {
        super(directory, width, height, length);
    }

    @Override
    protected IBlockStoreRegion newRegion(File regionFile, Map<String, Tag> root, int width, int height, int length) {
        return new SafeBlockStoreRegion(this, regionFile, root, width, height, length);
    }

    @Override
    protected IBlockStoreRegion readRegion(File regionFile) throws IOException {
        SafeBlockStoreReader reader = new SafeBlockStoreReader();
        return reader.readRegion(this, regionFile);
    }
    
}
