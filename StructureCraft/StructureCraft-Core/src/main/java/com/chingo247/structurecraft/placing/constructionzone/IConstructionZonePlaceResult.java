/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.placing.constructionzone;

import com.chingo247.structurecraft.placing.IPlaceResult;
import com.chingo247.structurecraft.model.zone.IConstructionZone;

/**
 *
 * @author Chingo
 */
public interface IConstructionZonePlaceResult extends IPlaceResult {
    

    /**
     * Gets the construction zone that has been placed. May return null if placing was not succesfull
     * @return The construction zone
     */
    IConstructionZone getPlacedConstructionZone();
    
}
