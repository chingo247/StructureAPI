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
package com.chingo247.structureapi.platform.services.holograms;

import com.sk89q.worldedit.Vector;

/**
 *
 * @author Chingo
 */
public interface HologramsProvider {
    
    /**
     * Gets the name of the HologramProvider
     * @return The name of the HologramProvider
     */
    public String getName();
    
    
    /**
     * Creates a Hologram using the HologramProvider
     * @param plugin The plugin to register the hologram
     * @param world The target world
     * @param position The position of the Hologram
     * @return The hologram
     */
    public Hologram createHologram(String plugin, String world, Vector position);
    
}
