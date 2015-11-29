/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.logging;

import java.util.UUID;

/**
 *
 * @author Chingo
 */
public class BlockLogEntry {
    
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

    public UUID getWorld() {
        return world;
    }
    
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public long getDate() {
        return date;
    }
    
    public int getZ() {
        return z;
    }

    public int getOldMaterial() {
        return oldMaterial;
    }

    public int getOldData() {
        return oldData;
    }

    public int getNewMaterial() {
        return newMaterial;
    }

    public int getNewData() {
        return newData;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public void setOldMaterial(int oldMaterial) {
        this.oldMaterial = oldMaterial;
    }

    public void setOldData(int oldData) {
        this.oldData = oldData;
    }

    public void setNewMaterial(int newMaterial) {
        this.newMaterial = newMaterial;
    }

    public void setNewData(int newData) {
        this.newData = newData;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public void setWorld(UUID world) {
        this.world = world;
    }
    
    
    
    
    
}
