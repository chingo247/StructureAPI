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
package com.chingo247.structurecraft.construction.listener;

import com.chingo247.structurecraft.construction.IStructureEntry;

/**
 *
 * @author Chingo
 */
public interface IConstructionListener {

    void onStarted(IStructureEntry entry);

    void onQueued(IStructureEntry entry);

    void onComplete(IStructureEntry entry);

    void onProgress(IStructureEntry entry);

    void onCancelled(IStructureEntry entry);
    
    void onFailed(IStructureEntry entry);

}
