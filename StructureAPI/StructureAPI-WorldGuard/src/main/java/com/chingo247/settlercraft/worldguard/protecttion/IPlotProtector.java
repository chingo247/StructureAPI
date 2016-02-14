/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.settlercraft.worldguard.protecttion;

import com.chingo247.structureapi.model.plot.IPlot;

/**
 *
 * @author Chingo
 */
public interface IPlotProtector<T extends IPlot> {
    
    /**
     * Name of the plugin/service that will protect the plot
     * @return The name
     */
    public String getName();
    
    /**
     * Protects a plot
     * @param structure 
     */
    public void protect(T structure);
    
    /**
     * Removes protection from a plot, requires an active NEO4J transaction
     * @param structure 
     */
    public void removeProtection(T structure);
    
    /**
     * Checks whether a plot is protected
     * @param structure
     * @return True if plot was protected
     */
    public boolean hasProtection(T structure);
    
}
