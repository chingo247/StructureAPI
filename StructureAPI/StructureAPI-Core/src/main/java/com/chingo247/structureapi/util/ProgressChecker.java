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

/**
 *
 * @author Chingo
 */
public class ProgressChecker {
    
    private double currentProgress;

    public ProgressChecker() {
        this.currentProgress = 0;        
    }
    
    /**
     * Checks if the difference between the last progress that was reported and the newProgress is greater than then min reportable
     * @param newProgress The new progress (or current progress) of a {@link IProgressable} or any other object that can progress
     * @param minReportable The min reportable progress value (so players/users will not get spammed to death)
     * @return True if the progress was greater than the minReportable progress
     */
    public boolean checkProgress(double newProgress, double minReportable) {
        if(Math.abs(newProgress - currentProgress) > minReportable) {
            this.currentProgress = newProgress;
            return true;
        }
        return false;
    }
    
}
