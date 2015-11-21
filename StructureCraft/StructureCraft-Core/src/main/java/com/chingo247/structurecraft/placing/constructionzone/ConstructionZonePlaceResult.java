/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.placing.constructionzone;

import com.chingo247.structurecraft.model.zone.IConstructionZone;

/**
 *
 * @author Chingo
 */
class ConstructionZonePlaceResult implements IConstructionZonePlaceResult {
    
    private IConstructionZone constructionZone;
    private String error = null;
    

    void setError(String message) {
        this.error = message;
    }
    
    void setZone(IConstructionZone zone) {
        this.constructionZone = zone;
    }
    
    
    @Override
    public IConstructionZone getPlacedConstructionZone() {
        return constructionZone;
    }

    @Override
    public boolean succes() {
        return constructionZone != null;
    }

    @Override
    public String error() {
        return error;
    }
    
}
