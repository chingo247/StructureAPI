/*
 * Copyright (C) 2015 ching
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

import com.chingo247.structureapi.model.AccessType;
import com.chingo247.settlercraft.core.model.world.WorldNode;
import com.chingo247.settlercraft.core.persistence.neo4j.NodeHelper;
import com.chingo247.structureapi.model.RelTypes;
import com.chingo247.structureapi.model.plot.PlotNode;
import com.chingo247.xplatform.core.ILocation;
import com.google.common.collect.Maps;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Result;

/**
 *
 * @author ching
 */
public class ConstructionZoneRepository {

    private final GraphDatabaseService graph;

    public ConstructionZoneRepository(GraphDatabaseService graph) {
        this.graph = graph;
    }

    private long nextId() {
        String idQuery = "MERGE (sid: ID_GENERATOR {name:'CONSTRUCTIONZONE_ID'}) "
                       + "ON CREATE SET sid.nextId = 1 "
                       + "ON MATCH SET sid.nextId = sid.nextId + 1 "
                       + "RETURN sid.nextId as nextId";
        
        Result r = graph.execute(idQuery);
        long id = (Long) r.next().get("nextId");
        return id;
    }

    public ConstructionZoneNode findById(long id) {
        ConstructionZoneNode zone = null;
        Map<String, Object> params = Maps.newHashMap();
        params.put("id", id);

        String query
                = " MATCH (s:" + ConstructionZoneNode.LABEL + " { " + ConstructionZoneNode.ID_PROPERTY + ": {id} })"
                + " RETURN s as zone";

        Result result = graph.execute(query, params);

        while (result.hasNext()) {
            Node n = (Node) result.next().get("zone");
            zone = new ConstructionZoneNode(n);
            break;
        }
        return zone;
    }

    public ConstructionZoneNode findOnPosition(ILocation location) {
        return findOnPosition(
                location.getWorld().getUUID(),
                new BlockVector(location.getBlockX(), location.getBlockY(), location.getBlockZ())
        );
    }

    public ConstructionZoneNode findOnPosition(UUID worldUUID, Vector position) {
        ConstructionZoneNode constructionZone = null;
        Map<String, Object> params = Maps.newHashMap();
        params.put("worldId", worldUUID.toString());
        String query
                = "MATCH ( world: " + WorldNode.LABEL + " { " + WorldNode.UUID_PROPERTY + ": {worldId} })"
                + " WITH world "
                + " MATCH (world)<-[:" + RelTypes.WITHIN + "]-(s:" + ConstructionZoneNode.LABEL + ")"
                + " WHERE "
                + " s." + ConstructionZoneNode.MAX_X_PROPERTY + " >= " + position.getBlockX() + " AND s." + ConstructionZoneNode.MIN_X_PROPERTY + " <= " + position.getBlockX()
                + " AND s." + ConstructionZoneNode.MAX_Y_PROPERTY + " >= " + position.getBlockY() + " AND s." + ConstructionZoneNode.MIN_Y_PROPERTY + " <= " + position.getBlockY()
                + " AND s." + ConstructionZoneNode.MAX_Z_PROPERTY + " >= " + position.getBlockZ() + " AND s." + ConstructionZoneNode.MIN_Z_PROPERTY + " <= " + position.getBlockZ()
                + " RETURN s as zone"
                + " LIMIT 1";

        Result result = graph.execute(query, params);
        while (result.hasNext()) {
            Map<String, Object> map = result.next();
            Node n = (Node) map.get("zone");
            constructionZone = new ConstructionZoneNode(n);
        }
        return constructionZone;
    }

    public Collection<ConstructionZoneNode> findWithin(UUID worldUUID, CuboidRegion searchArea, int limit) {
        List<ConstructionZoneNode> zones = new ArrayList<>();

        Map<String, Object> params = Maps.newHashMap();
        params.put("worldId", worldUUID.toString());
        if (limit > 0) {
            params.put("limit", limit);
        }

        Vector min = searchArea.getMinimumPoint();
        Vector max = searchArea.getMaximumPoint();

        String query
                = "MATCH (world:" + WorldNode.LABEL + " { " + WorldNode.UUID_PROPERTY + ": {worldId} })"
                + " WITH world "
                + " MATCH (world)<-[:" + RelTypes.WITHIN.name() + "]-(s:" + ConstructionZoneNode.LABEL + ")"
                + " WHERE "
                + " s." + ConstructionZoneNode.MAX_X_PROPERTY + " >= " + min.getBlockX() + " AND s." + ConstructionZoneNode.MIN_X_PROPERTY + " <= " + max.getBlockX()
                + " AND s." + ConstructionZoneNode.MAX_Y_PROPERTY + " >= " + min.getBlockY() + " AND s." + ConstructionZoneNode.MIN_Y_PROPERTY + " <= " + max.getBlockY()
                + " AND s." + ConstructionZoneNode.MAX_Z_PROPERTY + " >= " + min.getBlockZ() + " AND s." + ConstructionZoneNode.MIN_Z_PROPERTY + " <= " + max.getBlockZ()
                + " RETURN s";

        if (limit > 0) {
            query += " LIMIT {limit}";
        }

        Result result = graph.execute(query, params);
        while (result.hasNext()) {
            Map<String, Object> map = result.next();
            for (Object o : map.values()) {
                zones.add(new ConstructionZoneNode((Node) o));
            }
        }

        return zones;
    }

    public Iterable<ConstructionZoneNode> findAll() {
        return NodeHelper.makeIterable(graph.findNodes(ConstructionZoneNode.label()), ConstructionZoneNode.class);
    }
    
    public ConstructionZoneNode add(CuboidRegion region, AccessType accessType) {
        Vector min = region.getMinimumPoint();
        Vector max = region.getMaximumPoint();
        Node n = graph.createNode(PlotNode.plotLabel(), ConstructionZoneNode.label());
        n.setProperty(ConstructionZoneNode.ID_PROPERTY, nextId());
        ConstructionZoneNode zone = new ConstructionZoneNode(n);
        zone.setAccessType(accessType);
        zone.setMinX(min.getBlockX());
        zone.setMinY(min.getBlockY());
        zone.setMinZ(min.getBlockZ());
        zone.setMaxX(max.getBlockX());
        zone.setMaxY(max.getBlockY());
        zone.setMaxZ(max.getBlockZ());
        return zone;
    }

    public ConstructionZoneNode add(CuboidRegion region) {
        return add(region, AccessType.PRIVATE);
    }

    public void delete(long id) {
        ConstructionZoneNode zone = findById(id);
        if (zone != null) {
            delete(zone);
        }
    }

    public void delete(ConstructionZoneNode zone) {
        Node n = zone.getNode();
        for(Relationship rel : n.getRelationships()) {
            rel.delete();
        }
        n.delete();
    }

    public boolean hasWithin(UUID worldUUID, CuboidRegion searchArea) {
        return !findWithin(worldUUID, searchArea, 1).isEmpty();
    }

}
