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

import com.chingo247.structurecraft.restriction.exception.ConstructionZoneRestrictionException;
import com.chingo247.structurecraft.model.settler.ISettlerRepository;
import com.chingo247.structurecraft.model.settler.SettlerRepositiory;
import com.chingo247.structurecraft.model.structure.IStructureRepository;
import com.chingo247.structurecraft.model.structure.StructureRepository;
import com.chingo247.structurecraft.model.world.IStructureWorldRepository;
import com.chingo247.structurecraft.model.world.StructureWorldRepository;
import com.chingo247.structurecraft.model.AccessType;
import com.chingo247.structurecraft.model.zone.ConstructionZoneNode;
import com.chingo247.structurecraft.model.zone.ConstructionZoneRepository;
import com.chingo247.structurecraft.model.zone.IConstructionZone;
import com.chingo247.structurecraft.model.zone.IConstructionZoneRepository;
import com.chingo247.structurecraft.restriction.exception.RestrictionViolationException;
import com.chingo247.structurecraft.util.RegionUtil;
import com.chingo247.xplatform.core.APlatform;
import com.chingo247.xplatform.core.IWorld;
import com.google.common.util.concurrent.Monitor;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.World;
import java.util.Collection;
import java.util.UUID;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 *
 * @author Chingo
 */
public abstract class ConstructionWorld implements World, IWorld {

    private final UUID uuid;
    private final Monitor monitor;
    private final APlatform platform;
    private final GraphDatabaseService graph;
    private final IStructureWorldRepository structureWorldRepository;
    private final IStructureRepository structureRepository;
    private final IConstructionZoneRepository constructionZoneRepository;
    private final ISettlerRepository settlerRepository;
    private final IStructureAPI structureAPI;
    private IWorldConfig worldConfig;

    ConstructionWorld(UUID uuid, Monitor monitor, IStructureAPI structureAPI, GraphDatabaseService graph) {
        this.uuid = uuid;
        this.monitor = monitor;
        this.platform = structureAPI.getPlatform();
        this.structureAPI = structureAPI;
        this.graph = graph;
        this.constructionZoneRepository = new ConstructionZoneRepository(graph);
        this.structureRepository = new StructureRepository(graph);
        this.structureWorldRepository = new StructureWorldRepository(graph);
        this.settlerRepository = new SettlerRepositiory(graph);
    }

    @Override
    public final UUID getUUID() {
        return uuid;
    }

    /**
     * Gets the world config
     *
     * @return The worlds config
     */
    public final IWorldConfig getWorldConfig() {
        return worldConfig;
    }


    
    
    

    
    
    private void checkStructureConstructionZoneRestrictions(CuboidRegion affectArea, UUID player) throws RestrictionViolationException {
        Collection<ConstructionZoneNode> zones = constructionZoneRepository.findWithin(uuid, affectArea, 2);

        // May not overlap multiple zones
        if (zones.size() == 2) {
            throw new ConstructionZoneRestrictionException("Structure overlaps multiple construction zones");
        }

        // Check if restricted to zones
        if (zones.isEmpty() && worldConfig.isRestrictedToZones()) {
            throw new ConstructionZoneRestrictionException("Structures may only be placed within construction zones");
        }

        // If zones != empty, check acces
        if (!zones.isEmpty()) {
            IConstructionZone zone = zones.iterator().next();
            CuboidRegion zoneRegion = zone.getCuboidRegion();

            if (zone.getAccessType() == AccessType.RESTRICTED) {
                throw new ConstructionZoneRestrictionException("Placing structures is restricted within this construction zone: "
                        + "\n" + "(" + zoneRegion.getMinimumPoint() + ", " + zoneRegion.getMaximumPoint());
            }

            if (zone.getAccessType() == AccessType.PRIVATE && !zone.getOwnerDomain().isOwner(player)) {
                throw new ConstructionZoneRestrictionException("You are not a member of this zone!"
                        + "\n" + "(" + zoneRegion.getMinimumPoint() + ", " + zoneRegion.getMaximumPoint());
            }

            if (!RegionUtil.isDimensionWithin(zoneRegion, affectArea)) {
                throw new ConstructionZoneRestrictionException("Structure is not within construction zone"
                        + "\n" + "(" + zoneRegion.getMinimumPoint() + ", " + zoneRegion.getMaximumPoint());
            }

        }

    }
}
