/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.rollback.logging;

import com.chingo247.xplatform.core.IWorld;


/**
 *
 * @author Chingo
 */
public interface IStructureCraftBlockLogger  {

    void logBlock(int x, int y, int z, String player, IWorld world, int oldMaterial, byte oldData, int newMaterial, byte newData);
    
}
