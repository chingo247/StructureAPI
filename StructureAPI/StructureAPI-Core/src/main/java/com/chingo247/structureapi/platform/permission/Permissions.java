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
    
    public static final String CONTENT_RELOAD_PLANS = PREFIX + "content.plans.reload";
    public static final String CONTENT_GENERATE_PLANS = PREFIX + "content.plans.generate";
    public static final String CONTENT_ROTATE_PLACEMENT = PREFIX + "content.rotate.placement";
    
    public static final String STRUCTURE_PLACE = PREFIX + "settler.structure.place";
    public static final String STRUCTURE_CREATE = PREFIX + "settler.structure.create";
    public static final String STRUCTURE_INFO = PREFIX + "settler.structure.info";
    public static final String STRUCTURE_LIST = PREFIX + "settler.structure.list";
    public static final String STRUCTURE_CONSTRUCTION = PREFIX + "settler.structure.construction";
    public static final String STRUCTURE_LOCATION = PREFIX + "settler.structure.location";
    
    public static final String STRUCTURE_BACKUP = PREFIX + "advanced.structure.backup";
    
    public static final String SETTLER_OPEN_PLANMENU = PREFIX + "settler.planmenu.open";
    public static final String SETTLER_OPEN_PLANSHOP = PREFIX + "settler.planshop.open";
    public static final String SETTLER_ME = PREFIX + "settler.me.info";
    
    public static final String CONSTRUCTIONZONE_CREATE = PREFIX + "constructionzone.create";
    public static final String CONSTRUCTIONZONE_DELETE = PREFIX + "constructionzone.delete";
    
    
    
    
    
}
