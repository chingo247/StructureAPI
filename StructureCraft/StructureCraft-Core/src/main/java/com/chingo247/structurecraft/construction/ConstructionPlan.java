/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.construction;

import com.chingo247.structurecraft.construction.options.IPlaceOptionsAssigner;
import com.chingo247.structurecraft.construction.assigner.ITaskAssigner;
import com.chingo247.structurecraft.construction.options.Traversal;
import com.chingo247.structurecraft.construction.options.PlaceOptions;
import com.chingo247.structurecraft.model.structure.IStructure;
import java.util.UUID;
import org.primesoft.asyncworldedit.worldedit.AsyncEditSession;

/**
 *
 * @author Chingo
 */
public class ConstructionPlan implements IConstructionPlan {
    
    private static final IPlaceOptionsAssigner DEFAULT_OPTIONS_ASSIGNER = new IPlaceOptionsAssigner() {

        @Override
        public PlaceOptions getPlaceOptionsFor(IConstructionEntry entry) {
            return new PlaceOptions();
        }
    };
    
    private final IStructure structure;
    private final IConstructionExecutor executor;
    private final ITaskAssigner assigner;
    private boolean recursive, restrictive, reversed;
    private UUID player;
    private AsyncEditSession editSession;
    private Traversal traveral;
    private IPlaceOptionsAssigner optionsAssigner;
    private boolean useForce;
    
    

    public ConstructionPlan(IConstructionExecutor executor, IStructure structure, ITaskAssigner assigner) {
        this.structure = structure;
        this.executor = executor;
        this.assigner = assigner;
    }

    @Override
    public boolean isForced() {
        return useForce;
    }


    @Override
    public IConstructionPlan setStructureTraversal(Traversal traversal) {
        this.traveral = traversal;
        return this;
    }

    @Override
    public IConstructionPlan setForced(boolean useForce) {
        this.useForce = useForce;
        return this;
    }
    
    @Override
    public ITaskAssigner getAssigner() {
        return assigner;
    }

    @Override
    public AsyncEditSession getEditSession() {
        return editSession;
    }

    @Override
    public UUID getPlayer() {
        return player;
    }

    @Override
    public IStructure getStructure() {
        return structure;
    }
    
    @Override
    public boolean isRecursive() {
        return recursive;
    }

    @Override
    public boolean isRestrictive() {
        return restrictive;
    }

    @Override
    public boolean isReversed() {
        return reversed;
    }
    
    @Override
    public IConstructionPlan setRestrictive(boolean restrictive) {
        this.restrictive = restrictive;
        return this;
    }

    @Override
    public IConstructionPlan setRecursive(boolean recursive) {
        this.recursive = recursive;
        return this;
    }

    @Override
    public IConstructionPlan setReversedOrder(boolean reversed) {
        this.reversed = reversed;
        return this;
    }

    @Override
    public IConstructionPlan setPlayer(UUID player) {
        this.player = player;
        return this;
    }

    @Override
    public IConstructionPlan setEditsession(AsyncEditSession aes) {
        this.editSession = aes;
        return this;
    }
    
    @Override
    public IConstructionPlan setOptionsAssigner(IPlaceOptionsAssigner optionsAssigner) {
        this.optionsAssigner = optionsAssigner;
        return this;
    }

    
    @Override
    public void execute() {
        executor.execute(this);
    }

    @Override
    public Traversal getStructureTraversal() {
        return traveral;
    }

    @Override
    public IPlaceOptionsAssigner getOptionsAssigner() {
        if(optionsAssigner == null) {
            return DEFAULT_OPTIONS_ASSIGNER;
        }
        return optionsAssigner;
    }

    
    
}
