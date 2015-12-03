/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.rollback.model;

import com.chingo247.structurecraft.model.structure.StructureNode;

/**
 *
 * @author Chingo
 */
public interface IBlockLog {

    long getDate();

    int getNewData();

    int getNewMaterial();

    int getOldData();

    int getOldMaterial();

    int getX();

    int getY();

    int getZ();

    void setDate(long date);

    void setNewData(int newData);

    void setNewMaterial(int newMaterial);

    void setOldData(int oldData);

    void setOldMaterial(int oldMaterial);

    void setStructure(StructureNode structureNode);

    void setX(int x);

    void setY(int y);

    void setZ(int z);
    
}
