/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.construction.assigner.awe;

import com.chingo247.settlercraft.core.model.world.WorldNode;
import com.chingo247.structurecraft.IStructureAPI;
import com.chingo247.structurecraft.StructureAPI;
import com.chingo247.structurecraft.construction.IConstructionEntry;
import com.chingo247.structurecraft.construction.ITaskCallback;
import com.chingo247.structurecraft.construction.assigner.ITaskAssigner;
import com.chingo247.structurecraft.construction.awe.AWEPlacementTask;
import com.chingo247.structurecraft.construction.options.PlaceOptions;
import com.chingo247.structurecraft.event.structure.StructureConstructionCancelledEvent;
import com.chingo247.structurecraft.event.structure.StructureConstructionQueued;
import com.chingo247.structurecraft.event.structure.StructureRollbackCompleteEvent;
import com.chingo247.structurecraft.event.structure.StructureRollbackEvent;
import com.chingo247.structurecraft.event.structure.StructureRollbackProgressUpdateEvent;
import com.chingo247.structurecraft.exeption.StructureException;
import com.chingo247.structurecraft.model.Order;
import com.chingo247.structurecraft.model.RelTypes;
import com.chingo247.structurecraft.model.logging.BlockLogNode;
import com.chingo247.structurecraft.model.structure.IStructure;
import com.chingo247.structurecraft.model.structure.StructureNode;
import com.chingo247.structurecraft.placement.StructureBlock;
import com.chingo247.structurecraft.placement.interfaces.IPlacement;
import com.chingo247.structurecraft.util.Progress;
import com.chingo247.structurecraft.util.RegionUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.regions.CuboidRegion;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.primesoft.asyncworldedit.api.IAsyncWorldEdit;
import org.primesoft.asyncworldedit.worldedit.AsyncEditSession;

/**
 *
 * @author Chingo
 */
class SimpleAWERollbackAss implements ITaskAssigner {

    private static final int MAX_BLOCKS = 50_000;

    private long countBlocks(long structure, Date from, Date to) {
        final IStructureAPI structureAPI = StructureAPI.getInstance();
        Map<String, Object> params = Maps.newHashMap();
        params.put("sid", structure);
        if (from != null) {
            params.put("from", from.getTime());
        }
        if (to != null) {
            params.put("to", to.getTime());
        }

        String query
                = "MATCH (s:" + StructureNode.LABEL + " { " + StructureNode.ID_PROPERTY + ": {sid} }) "
                + "WITH s "
                + "MATCH (s)-[:" + RelTypes.HAS_BLOCK + "]->(b:" + BlockLogNode.LABEL + ") ";

        if (from != null && to != null) { // FROM AND TO
            query += "WHERE b.d >= {from} AND b.d <= {to} ";
        } else if (from != null) { // FROM ONLY
            query += "WHERE b.d >= {from} ";
        } else if (to != null) { // TO ONLY
            query += "WHERE b.d <= {to} ";
        }
        query += "RETURN count(b) as total ";
        System.out.println("query: " + query);

        Result r = structureAPI.getGraphDatabase().execute(query, params);
        long count = 0;
        System.out.println("HAS NEXT " + r.hasNext());

        if (r.hasNext()) {
            Map<String, Object> rMap = r.next();
            count = (long) rMap.get("total");
            System.out.println("COUNT " + count);
        }
        return count;
    }

    @Override
    public void assignTasks(AsyncEditSession session, UUID playerOrRandomUUID, final IConstructionEntry entry) throws StructureException, IOException {
        final IStructureAPI structureAPI = StructureAPI.getInstance();
        IAsyncWorldEdit asyncWorldEdit = structureAPI.getAsyncWorldEditIntegration().getAsyncWorldEdit();
        Vector position = entry.getStructure().getMin(); // Always place from the min position... 
        IStructure structure = entry.getStructure();

        Date from = structure.getCreatedAt();
        Date to = structure.getCompletedAt();
        long start = System.currentTimeMillis();
        long total;
        try (Transaction tx = structureAPI.getGraphDatabase().beginTx()) {
            total = countBlocks(structure.getId(), from, to);
            tx.success();
        }
        System.out.println("count rollback blocks in " + (System.currentTimeMillis() - start) + "ms");
        System.out.println("total is " + total);

        if (total != 0) {
            System.out.println("Assigning rollback tasks");
            int skip = 0;
            int tasks = 0;
            while (skip < total) {
                final int blocksToSkip = skip;
                final Progress progress = new Progress((double) total, (double) Math.min(skip + MAX_BLOCKS, total));

                AWERollbackPlacement placement = new AWERollbackPlacement(structure, from, to, skip, MAX_BLOCKS);
                AWEPlacementTask task = new AWEPlacementTask(
                        asyncWorldEdit,
                        entry,
                        placement,
                        playerOrRandomUUID,
                        session,
                        position,
                        new ITaskCallback() {

                            @Override
                            public void onComplete() {
                                if (!entry.hasNextTask()) {
                                    structureAPI.getEventDispatcher().dispatchEvent(new StructureRollbackCompleteEvent(entry.getStructure()));
                                } else {
                                    structureAPI.getEventDispatcher().dispatchEvent(
                                            new StructureRollbackProgressUpdateEvent(
                                                    entry.getStructure(),
                                                    progress)
                                    );
                                }
                            }

                            @Override
                            public void onCancelled() {
                                structureAPI.getEventDispatcher().dispatchEvent(new StructureConstructionCancelledEvent(entry.getStructure()));
                            }

                            @Override
                            public void onStarted() {
                                if(blocksToSkip == 0) {
                                    structureAPI.getEventDispatcher().dispatchEvent(new StructureRollbackEvent(entry.getStructure()));
                                }
                            }

                            @Override
                            public void onQueued() {
                                if(blocksToSkip == 0) {
                                    structureAPI.getEventDispatcher().dispatchEvent(new StructureConstructionQueued(entry.getStructure()));
                                }
                            }
                        }
                );
                entry.addTask(task);
                skip += MAX_BLOCKS;
                tasks++;

            }
            System.out.println("Rollback tasks: " + tasks);
        }
    }

    private class AWERollbackPlacement implements IPlacement {

        private final long limit, skip;
        private Date from, to;
        private IStructure structure;

        public AWERollbackPlacement(IStructure structure, Date from, Date to, long skip, long limit) {
            this.limit = limit;
            this.skip = skip;
            this.structure = structure;
            this.from = from;
            this.to = to;
        }

        @Override
        public void place(final EditSession session, Vector pos, PlaceOptions option) {
            List<StructureBlock> blocks = Lists.newArrayList();
            int count = 0;
            long start = System.currentTimeMillis();
            try (Transaction tx = StructureAPI.getInstance().getGraphDatabase().beginTx()) {
                long queryTime = System.currentTimeMillis();
                Result r = getBlocksResult(structure.getId(), from, to, skip, limit, Order.DATE_ASCENDING);
                System.out.println("Blocks query in " + (System.currentTimeMillis() - queryTime));
                
                while (r.hasNext()) {
                    Node n = (Node) r.next().get("block");
                    BlockLogNode bln = new BlockLogNode(n);
                    Vector blockPos = new BlockVector(bln.getX(), bln.getY(), bln.getZ());
                    BaseBlock block = new BaseBlock(
                            bln.getOldMaterial(),
                            bln.getOldData()
                    );

                    session.rawSetBlock(blockPos, block);
                    count++;

                    blocks.add(new StructureBlock(blockPos, block));
                }
                tx.success();
            }
            
            System.out.println("Done... (" + count + ") in " + (System.currentTimeMillis() - start) + "ms");

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

        private Result getBlocksResult(long structure, Date from, Date to, long skip, long limit, Order order) {
            final IStructureAPI structureAPI = StructureAPI.getInstance();
            Map<String, Object> params = Maps.newHashMap();
            params.put("sid", structure);
            if (from != null) {
                params.put("from", from.getTime());
            }
            if (to != null) {
                params.put("to", to.getTime());
            }
            if (skip > 0) {
                params.put("skip", skip);
            }
            if (limit > 0) {
                params.put("limit", limit);
            }

            String query
                    = "MATCH (s:" + StructureNode.LABEL + " { " + StructureNode.ID_PROPERTY + ": {sid} }) "
                    + "WITH s "
                    + "MATCH (s)-[:" + RelTypes.HAS_BLOCK + "]->(b:" + BlockLogNode.LABEL + ") ";

            if (from != null && to != null) { // FROM AND TO
                query += "WHERE b.d >= {from} AND b.d <= {to} ";
            } else if (from != null) { // FROM ONLY
                query += "WHERE b.d >= {from} ";
            } else if (to != null) { // TO ONLY
                query += "WHERE b.d <= {to} ";
            }

            query += "RETURN b as block ";

            query += " ORDER BY b.d " + (order == Order.DATE_ASCENDING ? "ASC" : "DESC");

            if (skip > 0) {
                query += " SKIP {skip} ";
            }

            if (limit > 0) {
                query += " LIMIT {limit}";
            }

            System.out.println("BLOCKS QUERY: " + query);
            long start = System.currentTimeMillis();
            Result r = structureAPI.getGraphDatabase().execute(query, params);
            System.out.println("Done in " + (System.currentTimeMillis() - start));
            return r;
        }

    }

}
