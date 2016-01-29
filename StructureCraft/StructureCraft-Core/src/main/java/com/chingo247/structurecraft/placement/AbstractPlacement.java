/*
 * Copyright (C) 2015 Chingo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.chingo247.structurecraft.placement;

import com.chingo247.settlercraft.core.Direction;
import com.chingo247.structurecraft.util.WorldUtil;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;

/**
 * 
 * @author Chingo
 * @param <T>
 */
public abstract class AbstractPlacement implements IPlacement, RotationalPlacement {
    
    protected static final int DEFAULT_ROTATION = -90;
    
    protected final Vector position;
    private int rotation = DEFAULT_ROTATION;
    protected int width;
    protected int height;
    protected int length;
    private final int axisOffset;

    public AbstractPlacement(int width, int height, int length) {
        this(0, Vector.ZERO, width, height, length);
    }

    
    public AbstractPlacement(int axisOffset, Vector relativePosition, int width, int height, int length) {
        this.position = relativePosition;
        this.length = length;
        this.height = height;
        this.width = width;
        this.axisOffset = axisOffset;
    }

    @Override
    public CuboidRegion getCuboidRegion() {
        return new CuboidRegion(Vector.ZERO, new Vector(width, height, length));
    }

    @Override
    public Vector getOffset() {
        return position;
    }
    

    @Override
    public final void rotate(int rotation) {
        Direction currentDirection = WorldUtil.getDirection(getRotation());
        System.out.println("CURRENT_DIRECTION: " + currentDirection + ", CURRENT_ROTATION: " + getRotation());
        
        this.rotation += rotation;
        this.rotation = (int) (normalizeYaw(this.rotation));
        
        Direction newDirection = WorldUtil.getDirection(getRotation());
        System.out.println("NEW_DIRECTION: " + newDirection + ", NEW_ROTATION: " + getRotation());
        
        if (((currentDirection == Direction.EAST || currentDirection == Direction.WEST) && (newDirection == Direction.NORTH || newDirection == Direction.SOUTH))
                || ((currentDirection == Direction.NORTH || currentDirection == Direction.SOUTH) && (newDirection == Direction.WEST || newDirection == Direction.EAST))) {
            int temp = width;
            width = length;
            length = temp;
            System.out.println("SWITCH!");
        } else {
             System.out.println("NO SWITCH!");
        }
        
        
        this.rotation = (int) (normalizeYaw(this.rotation));
    }
    
     private float normalizeYaw(float yaw) {
        float ya = yaw;
        if(yaw > 360) {
            int times = (int)((ya - (ya % 360)) / 360);
            int normalizer = times * 360;
            ya -= normalizer;
        } else if (yaw < -360) {
            ya = Math.abs(ya);
            int times = (int)((ya - (ya % 360)) / 360);
            int normalizer = times * 360;
            ya = yaw + normalizer;
        }
        return ya;
    } 
    
    @Override
    public Vector getSize() {
        return new Vector(width, height, length);
    }
    
    @Override
    public int getRotation() {
        return rotation + axisOffset;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public int getWidth() {
        return width;
    }
    
}
