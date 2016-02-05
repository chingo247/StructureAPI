/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.placing.structure;

/**
 *
 * @author Chingo
 */
public interface IStructurePlacerFactory {
    
    /**
     * Creates a new structure placer for a given world
     * @param world The world
     * @return The structure placer instance
     */
    IStructurePlacer createPlacer(String world);
    
}
