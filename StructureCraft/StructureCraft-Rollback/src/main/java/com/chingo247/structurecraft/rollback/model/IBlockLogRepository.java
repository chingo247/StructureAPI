/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.rollback.model;

import com.chingo247.structurecraft.rollback.logging.IBlockLogEntry;
import com.chingo247.structurecraft.rollback.model.impl.BlockLogNode;

/**
 *
 * @author Chingo
 */
public interface IBlockLogRepository {

    BlockLogNode addBlock(IBlockLogEntry blockLogEntry);
    
}
