/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.worldguard.protection;

import com.chingo247.structureapi.model.plot.IPlot;

/**
 *
 * @author Chingo
 */
public interface IPlotProtector {
    
    /**
     * Name of the plugin/service that will protect the plot
     * @return The name
     */
    String getName();
    
    /**
     * Protects a plot
     * @param plot 
     */
    void protect(IPlot plot);
    
    /**
     * Removes protection from a plot, requires an active NEO4J transaction
     * @param plot 
     */
    void removeProtection(IPlot plot);
    
    /**
     * Checks whether a plot is protected
     * @param plot
     * @return True if plot was protected
     */
    boolean hasProtection(IPlot plot);
    
}
