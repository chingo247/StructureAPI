
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
package com.chingo247.structureapi.util;


import com.chingo247.settlercraft.core.Direction;
import static com.chingo247.settlercraft.core.Direction.EAST;
import static com.chingo247.settlercraft.core.Direction.NORTH;
import static com.chingo247.settlercraft.core.Direction.SOUTH;
import static com.chingo247.settlercraft.core.Direction.WEST;
import com.sk89q.worldedit.Vector;




/**
 *
 * @author Chingo
 */
public class WorldUtil {

    private WorldUtil(){}
    
    /**
     * Translates a yaw to direction
     *
     * @param yaw The yaw
     * @return The direction
     */
    public static Direction getDirection(int yaw) {
        return getDirection((float)yaw);
    }
    
    private static float normalizeYaw(float yaw) {
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
    
    public static Direction getDirection(float yaw) {
        yaw = normalizeYaw(yaw);
        if (yaw >= 45f && yaw < 135f || yaw >= -315f && yaw < -225f) {
            return Direction.WEST;
        } else if (yaw >= 135f && yaw < 225f || yaw >= -225f && yaw < -135f) {
            return Direction.NORTH;
        } else if (yaw >= 225f && yaw < 315f || yaw >= -135f && yaw < -45f) {
            return Direction.EAST;
        } else /*(yaw >= 315 && yaw < 360 || yaw >= 0 && < 45) */ {
            return Direction.SOUTH;
        }
    }

    public static Vector translateLocation(Vector location, Direction direction, double xOffset, double yOffset, double zOffset) {
        switch (direction) {
            case EAST:
                return location.add(zOffset, yOffset, xOffset);
            case SOUTH:
                return location.add(-xOffset, yOffset, zOffset);
            case WEST:
                return location.add(-zOffset, yOffset, -xOffset);
            case NORTH:
                
                return location.add(xOffset, yOffset, -zOffset);
            default:
                throw new AssertionError("unreachable");
        }
    }
    
  

    /**
     * Gets the yaw for given direction
     *
     * @param direction The direction
     * @return float, yaw value
     */
    public static int getYaw(Direction direction) {
        switch (direction) {
            case SOUTH:
                return 0;
            case EAST:
                return -90;
            case NORTH:
                return -180;
            case WEST:
                return -270;
            default:
                throw new AssertionError("Unreachable");
        }
    }

}
