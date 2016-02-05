/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.placing.constructionzone;

/**
 *
 * @author Chingo
 */
public interface IConstructionZonePlacerFactory {
    
    IConstructionZonePlacer createPlacer(String world);
    
}
