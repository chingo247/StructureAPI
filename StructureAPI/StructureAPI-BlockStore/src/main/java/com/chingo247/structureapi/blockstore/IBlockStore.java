/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.blockstore;

import java.io.File;
import java.util.List;

/**
 *
 * @author Chingo
 */
public interface IBlockStore extends IBlockContainer, Iterable<IBlockStoreRegion> {
    
    String getVersion();
    
    String getName();
    
    /**
     * Gets the directory for this blockstore
     * @return The directory
     */
    File getDirectory();
    
    /**
     * Gets the width
     * @return The width
     */
    int getWidth();
    
    /**
     * Gets the length
     * @return The length
     */
    int getLength();
    
    /**
     * Gets the height
     * @return The height
     */
    int getHeight();
    
    /**
     * Gets the size of chunks, DEFAULT = 16
     * @return The size of chunks
     */
    int getChunkSize();
    
    
    
    /**
     * Gets the region for the given x and z coordinates.
     * Never returns null
     * @param x The x
     * @param z The z
     * @return The region
     */
    IBlockStoreRegion getRegion(int x, int z);
    
    List<IBlockStoreRegion> getLoadedRegions();
    
}
