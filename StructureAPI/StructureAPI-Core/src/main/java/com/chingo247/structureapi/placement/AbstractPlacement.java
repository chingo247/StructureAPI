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
package com.chingo247.structureapi.placement;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;

/**
 * 
 * @author Chingo
 * @param <T>
 */
public abstract class AbstractPlacement implements IPlacement, RotationalPlacement {
    
    
    protected final Vector position;
    protected int rotation = 0;
    protected int width;
    protected int height;
    protected int length;

    public AbstractPlacement(int width, int height, int length) {
        this(Vector.ZERO, width, height, length);
    }

    
    public AbstractPlacement(Vector relativePosition, int width, int height, int length) {
        this.position = relativePosition;
        this.length = length;
        this.height = height;
        this.width = width;
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
        this.rotation += rotation;
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
        return rotation;
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
