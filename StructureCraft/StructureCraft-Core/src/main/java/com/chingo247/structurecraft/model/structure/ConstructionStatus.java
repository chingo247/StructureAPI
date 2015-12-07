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
package com.chingo247.structurecraft.model.structure;

/**
 *
 * @author Chingo
 */
public enum ConstructionStatus {
    
    /**
     * When the construction has failed or was cancelled
     */
    ON_HOLD(0, false),
    /**
     * When the construction is being placed and there is no next task
     */
    BUILDING(1, true),
    /**
     * When the construction has been halted
     */
    STOPPED(2, false),
    /**
     * When the construction is a demolishing type
     */
    DEMOLISHING(3, true),
    /**
     * When construction has been completed
     */
    COMPLETED(4, false),
    /**
     * Structure has been queued
     */
    QUEUED(5, true),
    /**
     * Structure has been removed
     */
    REMOVED(6, false),
    /**
     * Structure is placing a fence
     */
    PLACING_FENCE(7, true),
    /**
     * Structure is rolling back
     */
    ROLLING_BACK(8, true),
    PERFORMING_OPERATION(9, true);
    
    


    private final int statusId;
    private final boolean inProgress;
    
    private ConstructionStatus(int statusId, boolean inProgress) {
        this.statusId = statusId;
        this.inProgress = inProgress;
    }

    public boolean isInProgress() {
        return inProgress;
    }

    public int getStatusId() {
        return statusId;
    }
    
    public static ConstructionStatus getStatus(String status) {
        switch(status.replaceAll(" ", "_")) {
            case "ON_HOLD":
                return ON_HOLD;
            case "BUILDING":
                return BUILDING;
            case "STOPPED":
                return STOPPED;
            case "DEMOLISHING":
                return DEMOLISHING;
            case "COMPLETED":
                return COMPLETED;
            case "QUEUED": 
                return QUEUED;
            case "REMOVED":
                return REMOVED;
            case "PLACING_FENCE":
                return PLACING_FENCE;
            case "ROLLING_BACK":
                return ROLLING_BACK;
            default:
                return PERFORMING_OPERATION;
        }
    }
    
    
    public static ConstructionStatus match(int statusId) {
        switch(statusId) {
            case 0: return ON_HOLD;
            case 1: return BUILDING;
            case 2: return STOPPED;
            case 3: return DEMOLISHING;
            case 4: return COMPLETED;
            case 5: return QUEUED;
            case 6: return REMOVED;
            case 7: return PLACING_FENCE;
            case 8: return ROLLING_BACK;
            case 11: return PERFORMING_OPERATION;
            default: throw new AssertionError("Unreachable");
        }
    }
    
    
}
