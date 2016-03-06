/*
 * Copyright (C) 2016 Chingo
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
package com.chingo247.structureapi.construction.producer;

import com.chingo247.structureapi.placement.options.PlaceOptions;
import com.sk89q.worldedit.blocks.BlockID;

/**
 *
 * @author Chingo
 */
public class ManualOptions extends PlaceOptions {

    private int foundationMaterial;
    private int foundationData;
    private int frameMaterial;
    private int frameData;
    private int fenceMaterial;
    private int fenceData;
    private int fenceHeight;
    
    private int frameSize;

    public ManualOptions() {
        this.foundationMaterial = BlockID.STONE;
        this.foundationData = 0;
        this.frameMaterial = BlockID.WOOD;
        this.fenceData = 0;
        this.fenceMaterial = BlockID.IRON_BARS;
        this.frameData = 0;
    }

    public int getFrameSize() {
        return frameSize;
    }

    public void setFrameSize(int frameSize) {
        this.frameSize = frameSize;
    }
    
    

    public int getFenceHeight() {
        return fenceHeight;
    }

    public void setFenceHeight(int fenceHeight) {
        this.fenceHeight = fenceHeight;
    }

    public int getFoundationMaterial() {
        return foundationMaterial;
    }

    public void setFoundationMaterial(int foundationMaterial) {
        this.foundationMaterial = foundationMaterial;
    }

    public int getFoundationData() {
        return foundationData;
    }

    public void setFoundationData(int foundationData) {
        this.foundationData = foundationData;
    }

    public int getFrameMaterial() {
        return frameMaterial;
    }

    public void setFrameMaterial(int frameMaterial) {
        this.frameMaterial = frameMaterial;
    }

    public int getFrameData() {
        return frameData;
    }

    public void setFrameData(int frameData) {
        this.frameData = frameData;
    }

    public int getFenceMaterial() {
        return fenceMaterial;
    }

    public void setFenceMaterial(int fenceMaterial) {
        this.fenceMaterial = fenceMaterial;
    }

    public int getFenceData() {
        return fenceData;
    }

    public void setFenceData(int fenceData) {
        this.fenceData = fenceData;
    }

}
