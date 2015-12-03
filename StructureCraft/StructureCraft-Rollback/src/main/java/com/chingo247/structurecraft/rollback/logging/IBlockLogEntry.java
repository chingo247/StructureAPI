/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.rollback.logging;

import java.util.UUID;

/**
 *
 * @author Chingo
 */
public interface IBlockLogEntry {

    long getDate();

    int getNewData();

    int getNewMaterial();

    int getOldData();

    int getOldMaterial();

    UUID getWorld();

    int getX();

    int getY();

    int getZ();

    void setDate(long date);

    void setNewData(int newData);

    void setNewMaterial(int newMaterial);

    void setOldData(int oldData);

    void setOldMaterial(int oldMaterial);

    void setWorld(UUID world);

    void setX(int x);

    void setY(int y);

    void setZ(int z);
    
}
