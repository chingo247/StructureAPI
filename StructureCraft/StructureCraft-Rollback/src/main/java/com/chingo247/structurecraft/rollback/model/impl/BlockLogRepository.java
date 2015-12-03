/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.rollback.model.impl;

import com.chingo247.structurecraft.rollback.logging.IBlockLogEntry;
import com.chingo247.structurecraft.rollback.model.IBlockLogRepository;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

/**
 *
 * @author Chingo
 */
public class BlockLogRepository implements IBlockLogRepository {

    private GraphDatabaseService graph;

    public BlockLogRepository(GraphDatabaseService graph) {
        this.graph = graph;
    }

    @Override
    public BlockLogNode addBlock(IBlockLogEntry blockLogEntry) {
        // Block log data
        Node node = graph.createNode(DynamicLabel.label(BlockLogNode.LABEL));
        node.setProperty("xyz", blockLogEntry.getX() + "" + blockLogEntry.getY() + "" + blockLogEntry.getZ());
        node.setProperty("cx", blockLogEntry.getX());
        node.setProperty("cz", blockLogEntry.getZ());
        node.setProperty("x", blockLogEntry.getX());
        node.setProperty("y", blockLogEntry.getY());
        node.setProperty("z", blockLogEntry.getZ());
        node.setProperty("om", blockLogEntry.getOldMaterial());
        node.setProperty("od", blockLogEntry.getOldData());
        node.setProperty("nm", blockLogEntry.getNewMaterial());
        node.setProperty("nd", blockLogEntry.getNewData());
        node.setProperty("d", blockLogEntry.getDate());
        return new BlockLogNode(node);
    }

}
