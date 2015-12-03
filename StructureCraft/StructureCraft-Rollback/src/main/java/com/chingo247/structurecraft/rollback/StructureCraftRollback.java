/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.rollback;

import com.chingo247.settlercraft.core.concurrent.ThreadPoolFactory;
import com.chingo247.settlercraft.core.persistence.neo4j.Neo4jHelper;
import com.chingo247.structurecraft.StructureAPI;
import com.chingo247.structurecraft.construction.ConstructionExecutor;
import com.chingo247.structurecraft.construction.ConstructionPlan;
import com.chingo247.structurecraft.construction.IConstructionExecutor;
import com.chingo247.structurecraft.construction.IConstructionPlan;
import com.chingo247.structurecraft.construction.assigner.ITaskAssigner;
import com.chingo247.structurecraft.model.structure.IStructure;
import com.chingo247.structurecraft.rollback.logging.impl.StructureBlockLogger;
import com.chingo247.structurecraft.rollback.model.impl.BlockLogNode;
import com.chingo247.structurecraft.rollback.platform.IRollbackConfig;
import com.chingo247.structurecraft.rollback.platform.IRollbackPlugin;
import com.google.common.base.Preconditions;
import java.util.concurrent.ExecutorService;
import org.PrimeSoft.blocksHub.IBlocksHubApi;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

/**
 *
 * @author Chingo
 */
public class StructureCraftRollback {

    private IRollbackPlugin rollbackPlugin;
    private IBlocksHubApi blocksHubApi;

    private ExecutorService executorService;
    private static StructureCraftRollback instance;

    private GraphDatabaseService graph;
    private StructureBlockLogger structureBlockLogger;
    
    private final IRestorePlanFactory restorePlanFactory = new RestorePlanFactory();

    private StructureCraftRollback() {

    }

    public static StructureCraftRollback getInstance() {
        if (instance == null) {
            instance = new StructureCraftRollback();
        }
        return instance;
    }

    public void registerRollbackPlugin(IRollbackPlugin rollbackPlugin) {
        if (this.rollbackPlugin != null) {
            throw new RuntimeException("Already registered a rollback plugin!");
        }
        this.rollbackPlugin = rollbackPlugin;
    }

    public void registerBlocksHubAPI(IBlocksHubApi blocksHubApi) {
        if (this.blocksHubApi != null) {
            throw new RuntimeException("Already registered the BlockshubAPI!");
        }
        this.blocksHubApi = blocksHubApi;
    }

    public void initialize() {
        Preconditions.checkArgument(rollbackPlugin != null, "Rollback plugin was not set!");

        // Setup schema indexes
        try (Transaction tx = graph.beginTx()) {
            Neo4jHelper.createIndexIfNotExist(graph, BlockLogNode.label(), BlockLogNode.DATE_PROPERTY);
            tx.success();
        }

//        IRollbackConfig config = rollbackPlugin.getRollbackConfig();
        ThreadPoolFactory factory = new ThreadPoolFactory();

        this.executorService = factory.newCachedThreadPool(2, 2);
        this.graph = StructureAPI.getInstance().getGraphDatabase();
        this.structureBlockLogger = new StructureBlockLogger(executorService, graph);

    }

    public IRestorePlanFactory getRestorePlanFactory() {
        return restorePlanFactory;
    }
    
//   
    private class RestorePlanFactory implements IRestorePlanFactory {

        @Override
        public IConstructionPlan newRestorePlan(IStructure structure) {
            IConstructionExecutor executor = StructureAPI.getInstance().getConstructionExecutor();
            ITaskAssigner assigner = new SimpleAWERollbackAss();
            return new ConstructionPlan(executor, structure, assigner)
                    .setRecursive(false);
        }

    }

}
