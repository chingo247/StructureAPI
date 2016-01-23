/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.construction.task;

import com.chingo247.structurecraft.construction.IStructureEntry;
import com.chingo247.structurecraft.placement.options.PlaceOptions;
import java.util.UUID;

/**
 *
 * @author Chingo
 */
public abstract class StructurePlacingTask extends StructureTask {

    private PlaceOptions options;
    
    public StructurePlacingTask(IStructureEntry constructionEntry, UUID submitter) {
        super(constructionEntry, submitter);
    }

    public void setOptions(PlaceOptions options) {
        this.options = options;
    }

    public PlaceOptions getOptions() {
        return options;
    }

}
