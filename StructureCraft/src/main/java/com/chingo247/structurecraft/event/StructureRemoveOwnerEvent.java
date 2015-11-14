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
package com.chingo247.structurecraft.event;

import com.chingo247.structurecraft.model.owner.OwnerType;
import com.chingo247.structurecraft.model.structure.Structure;
import java.util.UUID;

/**
 * Fired when an owner has been removed
 * @author Chingo
 */
public class StructureRemoveOwnerEvent extends StructureEvent {
    
    private final UUID removedOwner;
    private final OwnerType ownerType;

    public StructureRemoveOwnerEvent(UUID removedOwner, Structure structure, OwnerType type) {
        super(structure);
        this.removedOwner = removedOwner;
        this.ownerType = type;
    }

    /**
     * Gets the owner that has been removed
     * @return The removed owner
     */
    public UUID getRemovedOwner() {
        return removedOwner;
    }

    /**
     * Gets the type of the owner
     * @return The ownertype
     */
    public OwnerType getOwnerType() {
        return ownerType;
    }
    
    
    
}
