/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.rollback.platform;

import java.io.File;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 *
 * @author Chingo
 */
public interface IRollbackPlugin {
    /**
     * Gets the plugin directory
     * @return The plugin directory
     */
    File getWorkingDirectory();
    /**
     * Gets the name of the plugin.
     * @return The name of the plugin
     */
    String getName();

    
}
