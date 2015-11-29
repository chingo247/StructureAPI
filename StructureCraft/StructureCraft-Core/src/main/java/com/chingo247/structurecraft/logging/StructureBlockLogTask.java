/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.logging;

import com.chingo247.structurecraft.model.logging.BlockLogNode;
import java.util.List;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 *
 * @author Chingo
 */
public class StructureBlockLogTask extends ABlockLogTask<StructureBlockLogEntry>{

    StructureBlockLogTask(GraphDatabaseService graph, List<StructureBlockLogEntry> blocks, int batchSize) {
        super(graph, blocks, batchSize);
    }

    @Override
    protected void onBlockAdded(StructureBlockLogEntry t, BlockLogNode addedBlock) {
        addedBlock.setStructure(t.getStructureNode());
    }

    
    
}
