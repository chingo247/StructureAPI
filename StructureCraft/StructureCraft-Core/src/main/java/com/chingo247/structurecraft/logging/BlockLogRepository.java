/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.logging;

import com.chingo247.structurecraft.model.logging.BlockLogNode;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

/**
 *
 * @author Chingo
 */
class BlockLogRepository {

    private GraphDatabaseService graph;

    public BlockLogRepository(GraphDatabaseService graph) {
        this.graph = graph;
    }

    public BlockLogNode addBlock(BlockLogEntry blockLogEntry) {
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
