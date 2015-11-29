/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.logging;

import com.chingo247.structurecraft.model.logging.BlockLogEntry;
import com.chingo247.structurecraft.model.logging.BlockLogNode;
import com.chingo247.structurecraft.model.logging.BlockLogRepository;
import com.chingo247.structurecraft.model.logging.IBlockLogRepository;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

/**
 *
 * @author Chingo
 */
public abstract class ABlockLogTask<T extends BlockLogEntry> implements Runnable {

    private static final Logger LOG = Logger.getLogger(ABlockLogTask.class.getName());
    
    protected final List<T> entries;
    protected final IBlockLogRepository blockLogRepository;
    protected final GraphDatabaseService graph;
    private final int batchSize;

    public ABlockLogTask(GraphDatabaseService graph, List<T> blocks, int batchSize) {
        this.entries = blocks;
        this.graph = graph;
        this.blockLogRepository = new BlockLogRepository(graph);
        this.batchSize = batchSize;
    }

    protected abstract void onBlockAdded(T t, BlockLogNode addedBlock);

    @Override
    public void run() {
        try {
            long start = System.currentTimeMillis();
            Iterator<T> it = entries.iterator();
            while(it.hasNext()) {
                int count = 0;
                try (Transaction tx = graph.beginTx()) {
                   
                    while (it.hasNext() && count < batchSize) {
                        T data = it.next();

                        // Log block
                        BlockLogNode logNode = blockLogRepository.addBlock(data);
                        onBlockAdded(data, logNode);
                        count++;
                    }
                    tx.success();
                }
                System.out.println(" ");
                System.out.println("THREAD-" + Thread.currentThread().getId() + ": Logged " + count + " blocks in " + (System.currentTimeMillis() - start) + "ms");
                System.out.println(" ");
            }
            

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

}
