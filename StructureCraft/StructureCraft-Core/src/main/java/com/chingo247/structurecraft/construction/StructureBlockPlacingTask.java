/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.construction;

import com.chingo247.structurecraft.construction.options.PlaceOptions;
import java.util.UUID;

/**
 *
 * @author Chingo
 */
public abstract class StructureBlockPlacingTask extends StructureTask {

    private PlaceOptions options;
    
    public StructureBlockPlacingTask(ITaskCallback callback, IConstructionEntry constructionEntry, UUID submitter) {
        super(callback, constructionEntry, submitter);
    }

    public StructureBlockPlacingTask(IConstructionEntry constructionEntry, UUID submitter) {
        super(constructionEntry, submitter);
    }

    public void setOptions(PlaceOptions options) {
        this.options = options;
    }

    public PlaceOptions getOptions() {
        return options;
    }

}
