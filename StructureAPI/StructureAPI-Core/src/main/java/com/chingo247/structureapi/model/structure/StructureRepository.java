/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.model.structure;

import com.chingo247.settlercraft.core.Direction;
import com.chingo247.settlercraft.core.model.settler.SettlerNode;
import com.chingo247.settlercraft.core.model.world.WorldNode;
import com.chingo247.structureapi.model.RelTypes;
import com.chingo247.structureapi.model.owner.StructureOwnership;
import com.chingo247.structureapi.model.plot.PlotNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Result;

/**
 *
 * @author Chingo
 */
public class StructureRepository {

    private static final Logger LOG = Logger.getLogger(StructureRepository.class.getSimpleName());
    private final GraphDatabaseService graph;
    private boolean checked = false;

    public StructureRepository(GraphDatabaseService graph) {
        this.graph = graph;
    }

    public StructureNode findById(Long id) {
        StructureNode structure = null;
        Map<String, Object> params = Maps.newHashMap();
        params.put("structureId", id);

        String query
                = " MATCH (s:" + StructureNode.LABEL + " { " + StructureNode.ID_PROPERTY + ": {structureId} })"
                + " RETURN s as structure";

        Result result = graph.execute(query, params);

        if (result.hasNext()) {
            Node n = (Node) result.next().get("structure");
            structure = new StructureNode(n);
        }

        return structure;
    }

    private long nextId() {
        if (!checked) {
            Result r = graph.execute("MATCH (sid: ID_GENERATOR {name:'STRUCTURE_ID'}) "
                    + "RETURN sid "
                    + "LIMIT 1");
            if (!r.hasNext()) {
                graph.execute("CREATE (sid: ID_GENERATOR {name:'STRUCTURE_ID', nextId: 1})");
                checked = true;
                return 1;
            }
            checked = true;
        }

        // Work-around for getting the next Id
        // Sets the lock at this node by removing a non-existent property
        String idQuery = "MATCH (sid:ID_GENERATOR {name:'STRUCTURE_ID'}) "
                + "REMOVE sid.lock " // NON-EXISTENT PROPERTY
                + "SET sid.nextId = sid.nextId + 1 "
                + "RETURN sid.nextId as nextId";
        Result r = graph.execute(idQuery);
        long id = (long) r.next().get("nextId");

        return id;
    }

    public StructureNode addStructure(String name, Vector position, CuboidRegion region, Direction direction, double price) {
        long id = nextId();
        Node stNode = graph.createNode(StructureNode.label(), DynamicLabel.label(PlotNode.LABEL_PLOT));
        stNode.setProperty(StructureNode.ID_PROPERTY, id);
        stNode.setProperty(StructureNode.NAME_PROPERTY, name);
        stNode.setProperty(StructureNode.CONSTRUCTION_STATUS_PROPERTY, ConstructionStatus.ON_HOLD.getStatusId());
        stNode.setProperty(StructureNode.DIRECTION_PROPERTY, direction.getDirectionId());
        stNode.setProperty(StructureNode.POS_X_PROPERTY, position.getBlockX());
        stNode.setProperty(StructureNode.POS_Y_PROPERTY, position.getBlockY());
        stNode.setProperty(StructureNode.POS_Z_PROPERTY, position.getBlockZ());
        stNode.setProperty(StructureNode.MIN_X_PROPERTY, region.getMinimumPoint().getBlockX());
        stNode.setProperty(StructureNode.MIN_Y_PROPERTY, region.getMinimumPoint().getBlockY());
        stNode.setProperty(StructureNode.MIN_Z_PROPERTY, region.getMinimumPoint().getBlockZ());
        stNode.setProperty(StructureNode.MAX_X_PROPERTY, region.getMaximumPoint().getBlockX());
        stNode.setProperty(StructureNode.MAX_Y_PROPERTY, region.getMaximumPoint().getBlockY());
        stNode.setProperty(StructureNode.MAX_Z_PROPERTY, region.getMaximumPoint().getBlockZ());
        stNode.setProperty(StructureNode.CREATED_AT_PROPERTY, System.currentTimeMillis());
        stNode.setProperty(StructureNode.SIZE_PROPERTY, region.getArea());
        stNode.setProperty(StructureNode.PRICE_PROPERTY, price);
        stNode.setProperty(StructureNode.PLOT_TYPE_PROPERTY, "Structure");

        Vector center = region.getCenter();
        stNode.setProperty(StructureNode.CENTER_X_PROPERTY, center.getX());
        stNode.setProperty(StructureNode.CENTER_Z_PROPERTY, center.getZ());
        stNode.setProperty(StructureNode.CENTER_Y_PROPERTY, center.getY());

        StructureNode structure = new StructureNode(stNode);
        return structure;
    }

    public Collection<StructureNode> findStructuresDeletedAfter(UUID worldUUID, long date) {
        List<StructureNode> structures = com.google.common.collect.Lists.newArrayList();

        Map<String, Object> params = Maps.newHashMap();
        params.put("worldId", worldUUID.toString());
        params.put("date", date);

        String query = "MATCH (world:" + WorldNode.LABEL + " { " + WorldNode.UUID_PROPERTY + ": {worldId} })"
                + " WITH world "
                + " MATCH (world)<-[:" + RelTypes.WITHIN.name() + "]-(s:" + StructureNode.LABEL + ")"
                + " WHERE s." + StructureNode.DELETED_AT_PROPERTY + " > {date}"
                + " RETURN s";

        Result r = graph.execute(query, params);

        while (r.hasNext()) {
            Map<String, Object> map = r.next();

            for (Object o : map.values()) {
                Node n = (Node) o;
                StructureNode sn = new StructureNode(n);
                structures.add(sn);
            }
        }
        return structures;
    }

    public Collection<StructureNode> findCreatedAfter(UUID worldUUID, long date) {
        List<StructureNode> structures = Lists.newArrayList();

        Map<String, Object> params = Maps.newHashMap();
        params.put("worldId", worldUUID.toString());
        params.put("date", date);

        String query = "MATCH (world:" + WorldNode.LABEL + " { " + WorldNode.UUID_PROPERTY + ": {worldId} })"
                + " WITH world "
                + " MATCH (world)<-[:" + RelTypes.WITHIN.name() + "]-(s:" + StructureNode.LABEL + ")"
                + " WHERE s." + StructureNode.CREATED_AT_PROPERTY + " > {date}"
                + " RETURN s";

        Result r = graph.execute(query, params);

        while (r.hasNext()) {
            Map<String, Object> map = r.next();

            for (Object o : map.values()) {
                Node n = (Node) o;
                StructureNode sn = new StructureNode(n);
                structures.add(sn);
            }
        }
        return structures;
    }

    public Collection<StructureNode> findByWorld(UUID worldUUID, int skip, int limit) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("worldId", worldUUID.toString());
        if (skip > 0) {
            params.put("skip", skip);
        }
        if (limit > 0) {
            params.put("limit", params);
        }

        String query = "MATCH (world:" + WorldNode.LABEL + " { " + WorldNode.UUID_PROPERTY + ": {worldId} })"
                + " WITH world "
                + " MATCH (world)<-[:" + RelTypes.WITHIN.name() + "]-(s:" + StructureNode.LABEL + ")";
        
        query += " RETURN s";
        if (skip > 0) {
            query += " SKIP {skip}";
        }
        if (limit > 0) {
            query += " LIMIT {limit}";
        }

        Result r = graph.execute(query, params);

        List<StructureNode> structures = Lists.newArrayList();
        while (r.hasNext()) {
            Map<String, Object> map = r.next();

            for (Object o : map.values()) {
                Node n = (Node) o;
                StructureNode sn = new StructureNode(n);
                structures.add(sn);
            }
        }
        return structures;
    }

    public Collection<StructureNode> findByWorld(UUID worldUUID) {
        return findByWorld(worldUUID, -1, -1);
    }

    public Collection<StructureOwnership> findByOwner(UUID settlerUUID, int skip, int limit) {
        List<StructureOwnership> ownerships = Lists.newArrayList();

        Map<String, Object> params = Maps.newHashMap();
        params.put("settlerUUID", settlerUUID.toString());
        if(skip > 0) {
            params.put("skip", skip);
        }
        if(limit > 0) {
            params.put("limit", limit);
        }
        
        String query = "MATCH (settler:" + SettlerNode.LABEL + " { " + SettlerNode.UUID_PROPERTY + ": {settlerUUID} })"
                + " WITH settler "
                + " MATCH (settler)<-[r:" + RelTypes.OWNED_BY.name() + "]-(s:" + StructureNode.LABEL + ")"
                + " WHERE NOT s." + StructureNode.CONSTRUCTION_STATUS_PROPERTY + " = " + ConstructionStatus.REMOVED.getStatusId();
                query+= " RETURN settler, r";
                query+= " ORDER BY s." + StructureNode.CREATED_AT_PROPERTY + " DESC ";
                if(skip > 0) {
                    query += " SKIP {skip}";
                }
                if(limit > 0) {
                    query += " LIMIT {limit}";
                }

        Result r = graph.execute(query, params);

        while (r.hasNext()) {
            Map<String, Object> map = r.next();
            Object o = map.get("settler");
            Object relation = map.get("r");
//            System.out.println("has next");
            if (o != null) {
                Node settler = (Node) o;
                SettlerNode sn = new SettlerNode(settler);
                ownerships.add(new StructureOwnership(sn, (Relationship) relation));
            }
        }
        return ownerships;
    }
    
    public Collection<StructureOwnership> findByOwner(UUID settlerUUID) {
        return findByOwner(settlerUUID, -1, -1);
    }

    public Collection<StructureNode> findStructuresWithin(UUID worldUUID, CuboidRegion region, int limit) {
        List<StructureNode> structures = new ArrayList<>();

        Map<String, Object> params = Maps.newHashMap();
        params.put("worldId", worldUUID.toString());
        if (limit > 0) {
            params.put("limit", limit);
        }

        String query
                = "MATCH (world:" + WorldNode.LABEL + " { " + WorldNode.UUID_PROPERTY + ": {worldId} })"
                + " WITH world "
                + " MATCH (world)<-[:" + RelTypes.WITHIN.name() + "]-(s:" + StructureNode.LABEL + ")"
                + " WHERE s." + StructureNode.DELETED_AT_PROPERTY + " IS NULL"
                + " AND NOT s." + StructureNode.CONSTRUCTION_STATUS_PROPERTY + " = " + ConstructionStatus.REMOVED.getStatusId()
                + " AND s." + StructureNode.MAX_X_PROPERTY + " >= " + region.getMinimumPoint().getBlockX() + " AND s." + StructureNode.MIN_X_PROPERTY + " <= " + region.getMaximumPoint().getBlockX()
                + " AND s." + StructureNode.MAX_Y_PROPERTY + " >= " + region.getMinimumPoint().getBlockY() + " AND s." + StructureNode.MIN_Y_PROPERTY + " <= " + region.getMaximumPoint().getBlockY()
                + " AND s." + StructureNode.MAX_Z_PROPERTY + " >= " + region.getMinimumPoint().getBlockZ() + " AND s." + StructureNode.MIN_Z_PROPERTY + " <= " + region.getMaximumPoint().getBlockZ()
                + " RETURN s";

        if (limit > 0) {
            query += " LIMIT {limit}";
        }

        Result result = graph.execute(query, params);
        while (result.hasNext()) {
            Map<String, Object> map = result.next();
            for (Object o : map.values()) {
                structures.add(new StructureNode((Node) o));
            }
        }

        return structures;
    }

    public StructureNode findStructureOnPosition(UUID worldUUID, Vector position) {
        StructureNode structure = null;
        Map<String, Object> params = Maps.newHashMap();
        params.put("worldId", worldUUID.toString());
        String query
                = "MATCH ( world: " + WorldNode.LABEL + " { " + WorldNode.UUID_PROPERTY + ": {worldId} })"
                + " WITH world "
                + " MATCH (world)<-[:" + RelTypes.WITHIN + "]-(s:" + StructureNode.LABEL + ")"
                + " WHERE s." + StructureNode.DELETED_AT_PROPERTY + " IS NULL"
                + " AND s." + StructureNode.MAX_X_PROPERTY + " >= " + position.getBlockX() + " AND s." + StructureNode.MIN_X_PROPERTY + " <= " + position.getBlockX()
                + " AND s." + StructureNode.MAX_Y_PROPERTY + " >= " + position.getBlockY() + " AND s." + StructureNode.MIN_Y_PROPERTY + " <= " + position.getBlockY()
                + " AND s." + StructureNode.MAX_Z_PROPERTY + " >= " + position.getBlockZ() + " AND s." + StructureNode.MIN_Z_PROPERTY + " <= " + position.getBlockZ()
                + " RETURN s as structure"
                + " ORDER BY s." + StructureNode.SIZE_PROPERTY + " ASC " // Smallest structure is anchor point for new structure, the new structure must fit within this structure to qualify as substructure
                + " LIMIT 1";

        Result result = graph.execute(query, params);
        while (result.hasNext()) {
            Map<String, Object> map = result.next();
            Node n = (Node) map.get("structure");
            structure = new StructureNode(n);
        }
        return structure;
    }

    public boolean hasStructuresWithin(UUID worldUUID, CuboidRegion region) {
        return findStructuresWithin(worldUUID, region, 1).iterator().hasNext();
    }

    public long countStructuresOfSettler(UUID settlerUUID) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("settlerUUID", settlerUUID.toString());
        String query = "MATCH (settler:" + SettlerNode.LABEL + " { " + SettlerNode.UUID_PROPERTY + ": {settlerUUID} })"
                + " WITH settler "
                + " MATCH (settler)<-[:" + RelTypes.OWNED_BY.name() + "]-(s:" + StructureNode.LABEL + ")"
                + " WHERE NOT s." + StructureNode.CONSTRUCTION_STATUS_PROPERTY + " = " + ConstructionStatus.REMOVED.getStatusId()
                + " RETURN COUNT(s) as total";

        Result r = graph.execute(query, params);

        long count = 0;
        if (r.hasNext()) {
            Map<String, Object> map = r.next();
            Object o = map.get("total");
            if (o != null) {
                count = (long) o;
            }
        }
        return count;
    }

    public long countStructuresWithinWorld(UUID worldUUID) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("worldUUID", worldUUID.toString());
        String query = "MATCH (world:" + WorldNode.LABEL + " { " + WorldNode.UUID_PROPERTY + ": {worldUUID} })"
                + " WITH world "
                + " MATCH (world)<-[:" + RelTypes.WITHIN.name() + "]-(s:" + StructureNode.LABEL + ")"
                + " WHERE NOT s." + StructureNode.CONSTRUCTION_STATUS_PROPERTY + " = " + ConstructionStatus.REMOVED.getStatusId()
                + " RETURN COUNT(s) as total";

        Result r = graph.execute(query, params);

        long count = 0;
        if (r.hasNext()) {
            Map<String, Object> map = r.next();
            Object o = map.get("total");
            if (o != null) {
                count = (long) o;
            }
        }
        
        
        return count;
    }

    public boolean delete(long id) {
        StructureNode structureNode = findById(id);
        if (structureNode != null) {
            for (Relationship rel : structureNode.getNode().getRelationships()) {
                rel.delete();
            }
            structureNode.getNode().delete();
            return true;
        }
        return false;
    }

}
