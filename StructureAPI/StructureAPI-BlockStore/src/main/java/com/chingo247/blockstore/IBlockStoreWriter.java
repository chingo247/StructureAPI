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
package com.chingo247.blockstore;

import com.sk89q.jnbt.Tag;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 *
 * @author Chingo
 */
public interface IBlockStoreWriter<T extends IBlockStore> {
    
    void save(T blockstore) throws IOException;
    
    void save(T blockstore, File directory) throws IOException;
    
    void saveMetaData(T blockstore) throws IOException;
    
    Map<String,Tag> serializeMetaData(T blockstore);
    
    void saveRegion(IBlockStoreRegion region) throws IOException;
    
    void saveRegion(IBlockStoreRegion region, File directory) throws IOException;
    
}
