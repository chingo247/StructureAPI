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
package com.chingo247.structureapi.model.owner;

/**
 * Defines the variety of owner types.
 * @author Chingo
 */
public enum OwnerType {
    
    MEMBER(0, "MEMBERS"),
    OWNER(1, "OWNERS"),
    MASTER(2, "MASTERS");
    
    private final int id;
    private final String plural;

    private OwnerType(int id, String plural) {
        this.id = id;
        this.plural = plural;
    }

    public int getTypeId() {
        return id;
    }

    public String plural() {
        return plural;
    }
    
    
    public static OwnerType match(int id) {
        switch(id) {
            case 0: return MEMBER;
            case 1: return OWNER;
            case 2: return MASTER;
            default: throw new AssertionError("Unreachable");
        }
        
    }
    
}
