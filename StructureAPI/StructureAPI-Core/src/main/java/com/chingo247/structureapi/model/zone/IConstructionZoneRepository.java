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
package com.chingo247.structureapi.model.zone;

import com.chingo247.xplatform.core.ILocation;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import java.util.Collection;
import java.util.UUID;

/**
 *
 * @author Chingo
 */
public interface IConstructionZoneRepository {
    
    
    ConstructionZoneNode findById(long id);
    
    ConstructionZoneNode findOnPosition(UUID worldUUID, Vector position);
    
    ConstructionZoneNode findOnPosition(ILocation location);
    
    Collection<ConstructionZoneNode> findWithin(UUID worldUUID, CuboidRegion searchArea, int limit);
    
    boolean hasWithin(UUID worldUUID, CuboidRegion searchArea);
    
    Iterable<? extends ConstructionZoneNode> findAll();
    
    ConstructionZoneNode add(CuboidRegion region);
    
    void delete(long id);
    
    void delete(ConstructionZoneNode zone);
    
}
