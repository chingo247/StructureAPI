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
package com.chingo247.structureapi.event.zone;

import com.chingo247.structureapi.model.owner.OwnerType;
import com.chingo247.structureapi.model.zone.IConstructionZone;
import java.util.UUID;

/**
 *
 * @author Chingo
 */
public class ConstructionZoneRemoveOwnerEvent extends ConstructionZoneEvent {
    
    private UUID player;
    private OwnerType type;

    public ConstructionZoneRemoveOwnerEvent(IConstructionZone zone, UUID player, OwnerType type) {
        super(zone);
        
        this.type = type;
        this.player = player;
    }

    public OwnerType getType() {
        return type;
    }
    
    public UUID getPlayer() {
        return player;
    }
    
}
