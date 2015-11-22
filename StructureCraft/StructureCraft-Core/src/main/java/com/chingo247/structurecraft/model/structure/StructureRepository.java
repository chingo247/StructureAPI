/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.model.structure;

import com.chingo247.settlercraft.core.Direction;
import com.chingo247.settlercraft.core.model.world.WorldNode;
import com.chingo247.structurecraft.model.RelTypes;
import com.chingo247.structurecraft.model.plot.PlotNode;
import com.chingo247.structurecraft.model.settler.SettlerNode;
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
import org.neo4j.graphdb.Result;

/**
 *
 * @author Chingo
 */
public class StructureRepository implements IStructureRepository {

    private static final Logger LOG = Logger.getLogger(IStructureRepository.class.getSimpleName());
    private final GraphDatabaseService graph;
    private boolean checked = false;

    public StructureRepository(GraphDatabaseService graph) {
        this.graph = graph;
    }

    @Override
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
        if(!checked) {
            Result r = graph.execute("MATCH (sid: ID_GENERATOR {name:'STRUCTURE_ID'}) "
                        + "RETURN sid "
                        + "LIMIT 1");
            if(!r.hasNext()) {
                graph.execute("CREATE (sid: ID_GENERATOR {name:'STRUCTURE_ID', nextId: 1})");
                checked = true;
                return 1;
            }
            checked = true;
        }
        
        // Work-around for getting the next Id
        // Sets the lock at this node by removing a non-existent property
        String idQuery = "MATCH (sid:ID_GENERATOR {name:'STRUCTURE_ID'}) "
                        +"REMOVE sid.lock " // NON-EXISTENT PROPERTY
                        +"SET sid.nextId = sid.nextId + 1 "
                        +"RETURN sid.nextId as nextId";
        Result r = graph.execute(idQuery);
        long id = (long) r.next().get("nextId");
        
        return id;
    }

    @Override
    public StructureNode addStructure(String name, Vector position, CuboidRegion region, Direction direction, double price) {
        long id = nextId();
        Node stNode = graph.createNode(StructureNode.label(), DynamicLabel.label(PlotNode.LABEL_PLOT));
        System.out.println("Adding structure with id: " + id);
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

    @Override
    public Collection<StructureNode> findWorldDeletedAfter(UUID worldUUID, long date) {
        List<StructureNode> structures = com.google.common.collect.Lists.newArrayList();

        Map<String, Object> params = Maps.newHashMap();
        params.put("worldId", worldUUID.toString());
        params.put("date", date);

        String query = "MATCH (world:" + WorldNode.LABEL + " { " + WorldNode.ID_PROPERTY + ": {worldId} })"
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

    @Override
    public Collection<StructureNode> findCreatedAfter(UUID worldUUID, long date) {
        List<StructureNode> structures = Lists.newArrayList();

        Map<String, Object> params = Maps.newHashMap();
        params.put("worldId", worldUUID.toString());
        params.put("date", date);

        String query = "MATCH (world:" + WorldNode.LABEL + " { " + WorldNode.ID_PROPERTY + ": {worldId} })"
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

    @Override
    public Collection<StructureNode> findByWorld(UUID worldUUID) {
        List<StructureNode> structures = Lists.newArrayList();

        Map<String, Object> params = Maps.newHashMap();
        params.put("worldId", worldUUID.toString());
        String query = "MATCH (world:" + WorldNode.LABEL + " { " + WorldNode.ID_PROPERTY + ": {worldId} })"
                + " WITH world "
                + " MATCH (world)<-[:" + RelTypes.WITHIN.name() + "]-(s:" + StructureNode.LABEL + ")"
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

    @Override
    public Collection<StructureNode> findByOwner(UUID settlerUUID) {
        List<StructureNode> structures = Lists.newArrayList();

        Map<String, Object> params = Maps.newHashMap();
        params.put("settlerUUID", settlerUUID.toString());
        String query = "MATCH (settler:" + SettlerNode.LABEL + " { " + SettlerNode.ID_PROPERTY + ": {settlerUUID} })"
                + " WITH settler "
                + " MATCH (settler)<-[:" + RelTypes.OWNED_BY.name() + "]-(structure:" + StructureNode.LABEL + ")"
                + " WHERE NOT " + StructureNode.CONSTRUCTION_STATUS_PROPERTY + " = " + ConstructionStatus.REMOVED.getStatusId()
                + " ORDER BY " + StructureNode.CREATED_AT_PROPERTY + " DESC "
                + " RETURN structure";

        Result r = graph.execute(query, params);

        while (r.hasNext()) {
            Map<String, Object> map = r.next();
            Object o = map.get("structure");
            if (o != null) {
                Node n = (Node) o;
                StructureNode sn = new StructureNode(n);
                structures.add(sn);
            }
        }
        return structures;
    }

    @Override
    public Collection<StructureNode> findStructuresWithin(UUID worldUUID, CuboidRegion region, int limit) {
        List<StructureNode> structures = new ArrayList<>();

        Map<String, Object> params = Maps.newHashMap();
        params.put("worldId", worldUUID.toString());
        if (limit > 0) {
            params.put("limit", limit);
        }

        String query
                = "MATCH (world:" + WorldNode.LABEL + " { " + WorldNode.ID_PROPERTY + ": {worldId} })"
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
    
    @Override
    public StructureNode findStructureOnPosition(UUID worldUUID, Vector position) {
        StructureNode structure = null;
        Map<String, Object> params = Maps.newHashMap();
        params.put("worldId", worldUUID.toString());
        String query
                = "MATCH ( world: " + WorldNode.LABEL + " { " + WorldNode.ID_PROPERTY + ": {worldId} })"
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

    @Override
    public boolean hasStructuresWithin(UUID worldUUID, CuboidRegion region) {
        return findStructuresWithin(worldUUID, region, 1).iterator().hasNext();
    }

    @Override
    public int countStructuresOfSettler(UUID settlerUUID) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("settlerUUID", settlerUUID.toString());
        String query = "MATCH (settler:" + SettlerNode.LABEL + " { " + SettlerNode.ID_PROPERTY + ": {settlerUUID} })"
                + " WITH settler "
                + " MATCH (settler)<-[:" + RelTypes.OWNED_BY.name() + "]-(structure:" + StructureNode.LABEL + ")"
                + " WHERE NOT " + StructureNode.CONSTRUCTION_STATUS_PROPERTY + " = " + ConstructionStatus.REMOVED.getStatusId()
                + " RETURN COUNT(structure) as total";

        Result r = graph.execute(query, params);

        int count = 0;
        if (r.hasNext()) {
            Map<String, Object> map = r.next();
            Object o = map.get("total");
            if (o != null) {
                count = (int) o;
            }
        }
        return count;
    }

    @Override
    public int countStructuresWithinWorld(UUID worldUUID) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("worldUUID", worldUUID.toString());
        String query = "MATCH (world:" + SettlerNode.LABEL + " { " + WorldNode.ID_PROPERTY + ": {worldUUID} })"
                + " WITH world "
                + " MATCH (world)<-[:" + RelTypes.WITHIN.name() + "]-(structure:" + StructureNode.LABEL + ")"
                + " WHERE NOT " + StructureNode.CONSTRUCTION_STATUS_PROPERTY + " = " + ConstructionStatus.REMOVED.getStatusId()
                + " RETURN COUNT(structure) as total";

        Result r = graph.execute(query, params);

        int count = 0;
        if (r.hasNext()) {
            Map<String, Object> map = r.next();
            Object o = map.get("total");
            if (o != null) {
                count = (int) o;
            }
        }
        return count;
    }

}
