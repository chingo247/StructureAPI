/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.model.logging;

/**
 *
 * @author Chingo
 */
public interface IBlockLogRepository {

    BlockLogNode addBlock(BlockLogEntry blockLogEntry);
    
}
