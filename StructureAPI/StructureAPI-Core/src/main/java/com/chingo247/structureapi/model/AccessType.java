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
package com.chingo247.structureapi.model;

/**
 *
 * @author Chingo
 */
public enum AccessType {
    
    /**
     * Everyone has access
     */
    PUBLIC(0),
    /**
     * Only owners have access
     */
    PRIVATE(1),
    
    /**
     * No one is allowed
     */
    RESTRICTED(2);

    
    private final int typeId;
    
    private AccessType(int id) {
        this.typeId = id;
    }

    public int getTypeId() {
        return typeId;
    }
    
    public static final AccessType getAccessType(int typeId) {
        switch(typeId) {
            case 0: return PUBLIC;
            case 1: return PRIVATE;
            case 2: return RESTRICTED;
            default: throw new IllegalArgumentException("No type for id '" + typeId + "'");
        }
    }
    
    
}
