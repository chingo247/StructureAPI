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
package com.chingo247.structureapi.blockstore;

import com.sk89q.worldedit.blocks.BaseBlock;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Chingo
 */
public class BlockStoreTest {

    private BlockStore smallBlockStore;
    private BlockStore largeBlockStore;
    private static final String TEST_BASE = "temp_testing";
    private static final String SMALL_PATH = TEST_BASE + "/blockstore/small";
    private static final String LARGE_PATH = TEST_BASE + "/blockstore/large";
    

    public BlockStoreTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
        clear(new File(TEST_BASE));
        
    }

    @Before
    public void setUp() {
        File smallFile = new File(SMALL_PATH);
        File largeFile = new File(LARGE_PATH);

        smallFile.mkdirs();
        largeFile.mkdirs();

        clear(smallFile);
        clear(largeFile);

        smallFile.mkdirs();
        largeFile.mkdirs();

        smallBlockStore = new BlockStore(smallFile, 10, 10, 10);
        largeBlockStore = new BlockStore(largeFile, 2048, 100, 2048);
    }

    private static void clear(File directory) {
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                clear(file);
            }
            file.delete();
        }
        directory.delete();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testSetBlocks() {
        int mat = 8;
        int data = 2;

        for (int x = 0; x < smallBlockStore.getWidth(); x++) {
            for (int z = 0; z < smallBlockStore.getLength(); z++) {
                for (int y = 0; y < smallBlockStore.getHeight(); y++) {
                    smallBlockStore.setBlockAt(x, y, z, new BaseBlock(mat, data));
                }
            }
        }
        for (int x = 0; x < smallBlockStore.getWidth(); x++) {
            for (int z = 0; z < smallBlockStore.getLength(); z++) {
                for (int y = 0; y < smallBlockStore.getHeight(); y++) {
                    BaseBlock b = smallBlockStore.getBlockAt(x, y, z);
                    Assert.assertNotNull(b);
                    Assert.assertTrue(b.getId() == mat);
                    Assert.assertTrue(b.getData() ==  data);
                }
            }
        }

    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSetBlocksOutOfBounds() {
        int mat = 8;

        for (int x = 0; x < smallBlockStore.getWidth(); x++) {
            for (int z = 0; z < smallBlockStore.getLength(); z++) {
                for (int y = 0; y < smallBlockStore.getHeight() + 1; y++) {
                    smallBlockStore.setBlockAt(x, y, z, new BaseBlock(mat));
                }
            }
        }

    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSetBlocksOutOfBoundsNegative() {
        int mat = 8;
        for (int x = -1; x < smallBlockStore.getWidth(); x++) {
            for (int z = 0; z < smallBlockStore.getLength(); z++) {
                for (int y = 0; y < smallBlockStore.getHeight(); y++) {
                    smallBlockStore.setBlockAt(x, y, z, new BaseBlock(mat));
                }
            }
        }
    }

    @Test
    public void testRegionCreation() {
        int mat = 8;
        Assert.assertTrue(largeBlockStore.getLoadedRegions().isEmpty());
        largeBlockStore.setBlockAt(0, 0, 0, new BaseBlock(mat));
        Assert.assertTrue(largeBlockStore.getLoadedRegions().size() == 1);
        largeBlockStore.setBlockAt(BlockStore.REGION_SIZE, 0, 0, new BaseBlock(mat));
        Assert.assertTrue(largeBlockStore.getLoadedRegions().size() == 2);
        largeBlockStore.setBlockAt(BlockStore.REGION_SIZE, 0, BlockStore.REGION_SIZE, new BaseBlock(mat));
        Assert.assertTrue(largeBlockStore.getLoadedRegions().size() == 3);
        largeBlockStore.setBlockAt(0, 0, BlockStore.REGION_SIZE, new BaseBlock(mat));
        Assert.assertTrue(largeBlockStore.getLoadedRegions().size() == 4);
    }

    @Test
    public void testSavingLoading() {
        int mat = 8;
        int data = 2;
        
        for (int x = 0; x < smallBlockStore.getWidth(); x++) {
            for (int z = 0; z < smallBlockStore.getLength(); z++) {
                for (int y = 0; y < smallBlockStore.getHeight(); y++) {
                    smallBlockStore.setBlockAt(x, y, z, new BaseBlock(mat, data));
                }
            }
        }

        BlockStoreWriter writer = new BlockStoreWriter();
        try {
            writer.save(smallBlockStore);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        try {
            BlockStore blockstore = new BlockStoreReader().read(new File(SMALL_PATH));
            Assert.assertTrue(smallBlockStore.getName().equals(blockstore.getName()));
            Assert.assertTrue(smallBlockStore.getVersion().equals(blockstore.getVersion()));
            Assert.assertTrue(smallBlockStore.getWidth() == blockstore.getWidth());
            Assert.assertTrue(smallBlockStore.getHeight()== blockstore.getHeight());
            Assert.assertTrue(smallBlockStore.getLength()== blockstore.getLength());
            Assert.assertTrue(smallBlockStore.getChunkSize() == blockstore.getChunkSize());
            for (int x = 0; x < blockstore.getWidth(); x++) {
                for (int z = 0; z < blockstore.getLength(); z++) {
                    for (int y = 0; y < blockstore.getHeight(); y++) {
                        BaseBlock b = blockstore.getBlockAt(x, y, z);
                        Assert.assertNotNull(b);
                        Assert.assertTrue(b.getId() == mat);
//                        System.out.println("DATA: " + b.getData());
                        Assert.assertTrue(b.getData() ==  data);
                    }
                }
            }

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

    }

}
