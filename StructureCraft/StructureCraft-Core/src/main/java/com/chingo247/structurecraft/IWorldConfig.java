/*
 * Copyright (C) 2015 Chingo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.chingo247.structurecraft;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Chingo
 */
public interface IWorldConfig {
    
    
    File getConfigFile();
    
    /**
     * Determines whether structures may be placed in this world
     * @return 
     */
    boolean allowsStructures();
    
    /**
     * Determines whether structure placing is restricted to zones only
     * @return 
     */
    boolean isRestrictedToZones();
    
    /**
     * Saves the changes to this world config file
     * @throws IOException 
     */
    void save() throws IOException;
    
    /**
     * Loads the configuration from the file
     * @throws IOException 
     */
    void load() throws IOException;
    
}
