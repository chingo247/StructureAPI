/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.model.logging;

import com.chingo247.structurecraft.model.structure.StructureNode;
import java.util.UUID;

/**
 *
 * @author Chingo
 */
public class BlockLogEntry implements IBlockLogEntry {
    
    private int x, y, z;
    private int oldMaterial, oldData, newMaterial, newData;
    private long date;
    private UUID world;
    
    public BlockLogEntry(UUID world, int x, int y, int z, int oldMaterial, int oldData, int newMaterial, int newData) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.oldMaterial = oldMaterial;
        this.oldData = oldData;
        this.newMaterial = newMaterial;
        this.newData = newData;
        this.date = System.currentTimeMillis();
        this.world = world;
    }
    
    public BlockLogEntry(UUID world, int x, int y, int z, int oldMaterial, byte oldData, int newMaterial, byte newData) {
        this(world , x, y, z, oldMaterial, (int)oldData, newMaterial, (int)newData);
    }

    @Override
    public UUID getWorld() {
        return world;
    }
    
    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public long getDate() {
        return date;
    }
    
    @Override
    public int getZ() {
        return z;
    }

    @Override
    public int getOldMaterial() {
        return oldMaterial;
    }

    @Override
    public int getOldData() {
        return oldData;
    }

    @Override
    public int getNewMaterial() {
        return newMaterial;
    }

    @Override
    public int getNewData() {
        return newData;
    }

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public void setZ(int z) {
        this.z = z;
    }

    @Override
    public void setOldMaterial(int oldMaterial) {
        this.oldMaterial = oldMaterial;
    }

    @Override
    public void setOldData(int oldData) {
        this.oldData = oldData;
    }

    @Override
    public void setNewMaterial(int newMaterial) {
        this.newMaterial = newMaterial;
    }

    @Override
    public void setNewData(int newData) {
        this.newData = newData;
    }

    @Override
    public void setDate(long date) {
        this.date = date;
    }

    @Override
    public void setWorld(UUID world) {
        this.world = world;
    }
    
    
    
    
    
}
