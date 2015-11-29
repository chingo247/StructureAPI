/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.construction.assigner.impl.rollback;

import com.chingo247.settlercraft.core.model.world.WorldNode;
import com.chingo247.structurecraft.IStructureAPI;
import com.chingo247.structurecraft.construction.IConstructionEntry;
import com.chingo247.structurecraft.construction.ITaskCallback;
import com.chingo247.structurecraft.construction.assigner.AWETaskAssigner;
import com.chingo247.structurecraft.construction.options.PlaceOptions;
import com.chingo247.structurecraft.event.structure.StructureConstructionCancelledEvent;
import com.chingo247.structurecraft.event.structure.StructureConstructionQueued;
import com.chingo247.structurecraft.event.structure.StructureRestoreCompleteEvent;
import com.chingo247.structurecraft.event.structure.StructureRestoringEvent;
import com.chingo247.structurecraft.exeption.StructureException;
import com.chingo247.structurecraft.model.Order;
import com.chingo247.structurecraft.model.RelTypes;
import com.chingo247.structurecraft.model.logging.BlockLogNode;
import com.chingo247.structurecraft.model.structure.IStructure;
import com.chingo247.structurecraft.model.structure.StructureNode;
import com.chingo247.structurecraft.placement.interfaces.IPlacement;
import com.chingo247.structurecraft.util.RegionUtil;
import com.google.common.collect.Maps;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.regions.CuboidRegion;
import java.util.Map;
import java.util.UUID;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.primesoft.asyncworldedit.worldedit.AsyncEditSession;
import org.primesoft.asyncworldedit.worldedit.CancelabeEditSession;

/**
 *
 * @author Chingo
 */
class SimpleAWERollbackAssigner extends AWETaskAssigner {

    public SimpleAWERollbackAssigner(IStructureAPI structureAPI) {
        super(structureAPI);
    }

    @Override
    protected IPlacement getPlacementFor(IConstructionEntry entry) throws StructureException {
        return new RollbackPlacement(entry.getStructure());
    }

    @Override
    protected ITaskCallback getCallbackFor(final IConstructionEntry entry) {
        return new ITaskCallback() {

            @Override
            public void onComplete() {
                structureAPI.getEventDispatcher().dispatchEvent(new StructureRestoreCompleteEvent(entry.getStructure()));
            }

            @Override
            public void onCancelled() {
                structureAPI.getEventDispatcher().dispatchEvent(new StructureConstructionCancelledEvent(entry.getStructure()));
            }

            @Override
            public void onStarted() {
                structureAPI.getEventDispatcher().dispatchEvent(new StructureRestoringEvent(entry.getStructure()));
            }

            @Override
            public void onQueued() {
                structureAPI.getEventDispatcher().dispatchEvent(new StructureConstructionQueued(entry.getStructure()));
            }
        };
    }
    
    public Result getBlocksResult(UUID world, long structure, long from, long to, long skip, long limit, Order order) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("sid", structure);
        if (from > -1) {
            params.put("from", from);
        }
        if (to > 0) {
            params.put("to", to);
        }
        params.put("world", world.toString());
        if (skip > 0) {
            params.put("skip", skip);
        }
        if (limit > 0) {
            params.put("limit", limit);
        }

        String query
                = "MATCH (w:"+WorldNode.LABEL+" {"+WorldNode.UUID_PROPERTY+":{world}}) "
                + "WITH w "
                + "MATCH (w)<-[:"+RelTypes.WITHIN +"]-(s:"+StructureNode.LABEL+" { "+StructureNode.ID_PROPERTY+": {sid} }) "
                + "WITH s "
                + "MATCH (s)-[:"+RelTypes.HAS_BLOCK+"]->(b:"+BlockLogNode.LABEL+") ";

        if (from > -1 && to > -1) { // FROM AND TO
            query += "WHERE b.d >= {from} AND b.d <= {to} ";
        } else if (from > -1 && to <= 0) { // FROM ONLY
            query += "WHERE b.d >= {from} ";
        } else if (from < 0 && to >= 1) { // TO ONLY
            query += "WHERE b.d <= {to} ";
        }

        query += "RETURN b as block ";
        if (skip > 0) {
            query += "SKIP {skip} ";
        }
        
        query += " ORDER BY b.d " + (order == Order.DATE_ASCENDING ? "ASC" : "DESC ");
        
        if (limit > 0) {
            query += "LIMIT {limit}";
        }
        
        
        
        System.out.println("BLOCKS QUERY: " + query);
        long start = System.currentTimeMillis();
        Result r = structureAPI.getGraphDatabase().execute(query, params);
        System.out.println("Done in " + (System.currentTimeMillis() - start));
        return r;
    }
    
    private class RollbackPlacement implements IPlacement {
        
        private IStructure structure;

        public RollbackPlacement(IStructure structure) {
            this.structure = structure;
        }
        
        @Override
        public void place(EditSession session, Vector pos, PlaceOptions option) {
            GraphDatabaseService graph = structureAPI.getGraphDatabase();
            try(Transaction tx = graph.beginTx()) {
                Result r = getBlocksResult(structure.getWorldUUID(), structure.getId(), structure.getCreatedAt().getTime(), structure.getCompletedAt() != null ? structure.getCompletedAt().getTime() : -1, -1, -1, Order.DATE_DESCENDING);
                
                CancelabeEditSession cancelabeEditSession = null;
                cancelabeEditSession = (CancelabeEditSession) session;
                int count = 0;
                while(r.hasNext()) {
                    BlockLogNode logNode = new BlockLogNode((Node)r.next().get("block"));
                    Vector v = new BlockVector(logNode.getX(), logNode.getY(), logNode.getZ());
                    session.rawSetBlock(v, new BaseBlock(logNode.getOldMaterial(), logNode.getOldData()));
                    count++;
                    if(count % 1000 == 0 || count == 0) {
                        System.out.println("current count: " + count);
                    }
                }
                
                System.out.println("count: " + count);
                tx.success();
            }
        }

        @Override
        public Vector getOffset() {
            return Vector.ZERO;
        }

        @Override
        public Vector getSize() {
            return RegionUtil.getSize(structure.getCuboidRegion());
        }

        @Override
        public int getWidth() {
            return getSize().getBlockX();
        }

        @Override
        public int getHeight() {
            return getSize().getBlockY();
        }

        @Override
        public int getLength() {
            return getSize().getBlockZ();
        }

        @Override
        public CuboidRegion getCuboidRegion() {
            return structure.getCuboidRegion();
        }
        
    }
    
}
