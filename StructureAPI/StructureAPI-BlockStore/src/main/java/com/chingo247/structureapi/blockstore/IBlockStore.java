/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.blockstore;

import java.io.File;

/**
 *
 * @author Chingo
 */
public interface IBlockStore extends IBlockContainer, Iterable<IBlockStoreRegion> {
    
    /**
     * Gets the directory for this blockstore
     * @return The directory
     */
    File getDirectory();
    
    /**
     * Gets the maxWidth, returns -1 if width is infinite
     * @return The max width
     */
    int getWidth();
    
    /**
     * Gets the maxLength, returns -1 if length is infinite
     * @return The max length
     */
    int getLength();
    
    /**
     * Gets the region for the given x and z coordinates.
     * Never returns null
     * @param x The x
     * @param z The z
     * @return The region
     */
    IBlockStoreRegion getRegion(int x, int z);
    
    
}
