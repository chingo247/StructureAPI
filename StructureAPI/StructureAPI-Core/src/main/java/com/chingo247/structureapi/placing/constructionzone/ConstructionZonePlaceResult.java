/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.placing.constructionzone;

import com.chingo247.structureapi.model.zone.ConstructionZone;
import com.chingo247.structureapi.placing.IPlaceResult;


/**
 *
 * @author Chingo
 */
class ConstructionZonePlaceResult implements IPlaceResult<ConstructionZone> {
    
    private ConstructionZone constructionZone;
    private String error = null;
    

    void setError(String message) {
        this.error = message;
    }
    
    void setZone(ConstructionZone zone) {
        this.constructionZone = zone;
    }
    
    
    @Override
    public ConstructionZone getPlaced() {
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
