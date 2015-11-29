/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.construction.rollback;

import com.chingo247.structurecraft.construction.IPlacementSource;
import com.chingo247.settlercraft.core.model.world.WorldNode;
import com.chingo247.structurecraft.IStructureAPI;
import com.chingo247.structurecraft.StructureAPI;
import com.chingo247.structurecraft.construction.options.PlaceOptions;
import com.chingo247.structurecraft.model.Order;
import com.chingo247.structurecraft.model.RelTypes;
import com.chingo247.structurecraft.model.logging.BlockLogNode;
import com.chingo247.structurecraft.model.structure.IStructure;
import com.chingo247.structurecraft.model.structure.StructureNode;
import com.chingo247.structurecraft.placement.StructureBlock;
import com.chingo247.structurecraft.placement.interfaces.ICleanablePlacement;
import com.chingo247.structurecraft.placement.interfaces.IPlacement;
import com.chingo247.structurecraft.util.RegionUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.regions.CuboidRegion;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.parboiled.common.Preconditions;

/**
 *
 * @author Chingo
 */
class RollbackPlacementSource implements IPlacementSource {

    private int currentChunk;
    private int totalChunks;
    private int maxBlocks;
    private int skip;
    private ListIterator<Integer> chunkBlockCounts;
    private IStructure structure;
    private Long startDate, endDate;

    RollbackPlacementSource(IStructure structure) {
        this.structure = structure;
        this.startDate = structure.getCreatedAt() != null ? structure.getCreatedAt().getTime() : null;
        this.endDate = structure.getCompletedAt() != null ? structure.getCompletedAt().getTime() : null;
        this.maxBlocks = 200_000;
        this.skip = 0;
    }

    public void setMaxBlocks(int maxBlocks) {
        this.maxBlocks = maxBlocks;
    }

    private Result getBlocksResult(UUID world, long structure, long skip, long limit, Order order) {
        IStructureAPI structureAPI = StructureAPI.getInstance();
        Map<String, Object> params = Maps.newHashMap();
        params.put("sid", structure);
        // Set start- & endDate
        if (startDate != null) {
            params.put("from", startDate);
        }
        if (endDate != null) {
            params.put("to", endDate);
        }
        // Specifiy world...
        params.put("world", world.toString());

        // Set the limit AND skip
        if (skip > 0) {
            params.put("skip", skip);
        }
        if (limit > 0) {
            params.put("limit", limit);
        }

        String query
                = "MATCH (w:" + WorldNode.LABEL + " {" + WorldNode.UUID_PROPERTY + ":{world}}) "
                + "WITH w "
                + "MATCH (w)<-[:" + RelTypes.WITHIN + "]-(s:" + StructureNode.LABEL + " { " + StructureNode.ID_PROPERTY + ": {sid} }) "
                + "WITH s "
                + "MATCH (s)-[:" + RelTypes.HAS_BLOCK + "]->(b:" + BlockLogNode.LABEL + ") ";

        if (startDate != null && endDate != null) { // FROM AND TO
            query += "WHERE b.d >= {from} AND b.d <= {to} ";
        } else if (startDate != null) { // FROM ONLY
            query += "WHERE b.d >= {from} ";
        } else if (endDate != null) { // TO ONLY
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

    private List<Integer> getChunkBlockCounts() {
        return null;
    }

    @Override
    public IPlacement nextPlacement() {
        if (chunkBlockCounts == null) {
            List<Integer> cbc = getChunkBlockCounts();
            this.chunkBlockCounts = cbc.listIterator();
            this.totalChunks = cbc.size();
        }

        if (!hasNext()) {
            return null;
        }

        int limit = 0;
        while (chunkBlockCounts.hasNext()) {
            limit += chunkBlockCounts.next();
            currentChunk++;
            if (limit > maxBlocks) {
                break;
            }
        }

        IStructureAPI structureAPI = StructureAPI.getInstance();
        GraphDatabaseService graph = structureAPI.getGraphDatabase();
        List<StructureBlock> blocks = Lists.newArrayList();
        int count = 0;
        try (Transaction tx = graph.beginTx()) {
            Result r = getBlocksResult(structure.getWorldUUID(), structure.getId(), skip, limit, Order.DATE_DESCENDING);
            while (r.hasNext()) {
                Map<String, Object> map = r.next();
                BlockLogNode blockNode = new BlockLogNode((Node) map.get("block"));
                BaseBlock b = new BaseBlock(blockNode.getOldMaterial(), blockNode.getOldData());
                Vector p = new BlockVector(blockNode.getX(), blockNode.getY(), blockNode.getZ());
                blocks.add(new StructureBlock(p, b));
                count++;
            }

            tx.success();
        }
        System.out.println("count: " + count);
        skip = limit;
        return new RollbackPlacement(blocks);
    }

    @Override
    public boolean hasNext() {
        return this.chunkBlockCounts.hasNext();
    }

    private class RollbackPlacement implements IPlacement, ICleanablePlacement {

        private List<StructureBlock> blocks;
        private boolean cleanupAfter;

        public RollbackPlacement(List<StructureBlock> blocks) {
            Preconditions.checkNotNull(blocks, "Blocks was null");
            this.blocks = blocks;
            this.cleanupAfter = true;
        }

        @Override
        public void place(EditSession session, Vector pos, PlaceOptions option) {
            for (StructureBlock sb : blocks) {
                session.rawSetBlock(sb.getPosition(), sb.getBlock());
            }

            if (cleanupAfter) {
                cleanUp();
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

        @Override
        public void cleanUp() {
            this.blocks = null;
        }

        @Override
        public void setCleanupAfterUsage(boolean cleanUp) {
            this.cleanupAfter = cleanUp;
        }

        @Override
        public boolean cleanUpAfterUsage() {
            return cleanupAfter;
        }

    }

}
