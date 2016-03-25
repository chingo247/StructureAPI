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
package com.chingo247.structureapi.worldguard.plugin;

/**
 * Permissions as of 2.2.0 in format {on-what}.{operation}
 * @author Chingo
 */
public class Permissions {
    
    private Permissions() {}
    
    private static final String PREFIX = "structureapi.wg.";
    
    public static final String EXPIRE_SINGLE = PREFIX + "expire.single";
    public static final String EXPIRE_WORLD = PREFIX + "expire.world";
    public static final String EXPIRE_ALL = PREFIX + "expire.all";
    public static final String PROTECT_SINGLE = PREFIX + "protect.single";
    public static final String PROTECT_WORLD = PREFIX + "protect.world";
    public static final String PROTECT_ALL = PREFIX + "protect.all";
    
   
    
    
    
    
}
