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
package com.chingo247.structureapi.platform.permission;

/**
 * Permissions as of 2.2.0 in format {who/which}.{what}.{operation}
 * @author Chingo
 */
public class Permissions {
    
    private Permissions() {}
    
    private static final String PREFIX = "settlercraft.";
    
    public static final String CONTENT_RELOAD_PLANS = PREFIX + "plans.reload";
    public static final String CONTENT_GENERATE_PLANS = PREFIX + "plans.generate";
    public static final String CONTENT_ROTATE_PLACEMENT = PREFIX + "schematic.rotate";
    
    public static final String STRUCTURE_PLACE = PREFIX + "structure.place";
    public static final String STRUCTURE_CREATE = PREFIX + "structure.create";
    public static final String STRUCTURE_INFO = PREFIX + "structure.info";
    public static final String STRUCTURE_LIST = PREFIX + "structure.list";
    public static final String STRUCTURE_CONSTRUCTION = PREFIX + "structure.construction";
    public static final String STRUCTURE_LOCATION = PREFIX + "structure.location";
    
    public static final String OPEN_PLANMENU = PREFIX + "planmenu.open";
    public static final String OPEN_PLANSHOP = PREFIX + "planshop.open";
    public static final String SETTLER_ME = PREFIX + "settler.info";
    
    public static final String CONSTRUCTIONZONE_CREATE = PREFIX + "constructionzone.create";
    public static final String CONSTRUCTIONZONE_DELETE = PREFIX + "constructionzone.delete";
    
    
    
    
    
}
