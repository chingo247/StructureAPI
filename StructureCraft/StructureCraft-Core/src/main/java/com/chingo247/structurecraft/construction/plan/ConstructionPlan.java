/*
 * Copyright (C) 2015 Chingo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.chingo247.structurecraft.construction.plan;

import com.chingo247.structurecraft.construction.IConstructionEntry;
import com.chingo247.structurecraft.construction.IConstructionExecutor;
import com.chingo247.structurecraft.construction.ITaskAssigner;
import com.chingo247.structurecraft.placement.options.Traversal;
import com.chingo247.structurecraft.model.structure.IStructure;
import com.chingo247.structurecraft.placement.IPlacement;
import java.util.UUID;
import org.primesoft.asyncworldedit.worldedit.AsyncEditSession;

/**
 *
 * @author Chingo
 */
public abstract class ConstructionPlan implements IConstructionPlan {
    
    private final IStructure structure;
    private final IConstructionExecutor executor;
    private final ITaskAssigner assigner;
    private boolean recursive, restrictive, reversed;
    private UUID player;
    private AsyncEditSession editSession;
    private Traversal traveral;
    private boolean useForce;
    
    public ConstructionPlan(IConstructionExecutor executor, IStructure structure, ITaskAssigner assigner) {
        this.structure = structure;
        this.executor = executor;
        this.assigner = assigner;
    }
    
    @Override
    public abstract IPlacement getPlacement(IStructure structure) throws Exception;
    
    public abstract void register(IConstructionEntry entry) throws Exception;

    @Override
    public boolean isForced() {
        return useForce;
    }

    public ConstructionPlan setStructureTraversal(Traversal traversal) {
        this.traveral = traversal;
        return this;
    }

    public ConstructionPlan setForced(boolean useForce) {
        this.useForce = useForce;
        return this;
    }
    
    public ITaskAssigner getAssigner() {
        return assigner;
    }

    public AsyncEditSession getEditSession() {
        return editSession;
    }

    public UUID getPlayer() {
        return player;
    }

    public IStructure getStructure() {
        return structure;
    }
    
    public boolean isRecursive() {
        return recursive;
    }

    public boolean isRestrictive() {
        return restrictive;
    }

    
    public boolean isReversed() {
        return reversed;
    }
    
    
    public ConstructionPlan setRestrictive(boolean restrictive) {
        this.restrictive = restrictive;
        return this;
    }

    
    public ConstructionPlan setRecursive(boolean recursive) {
        this.recursive = recursive;
        return this;
    }

    
    public ConstructionPlan setReversedOrder(boolean reversed) {
        this.reversed = reversed;
        return this;
    }

    
    public ConstructionPlan setPlayer(UUID player) {
        this.player = player;
        return this;
    }
    
    

    
    public ConstructionPlan setEditsession(AsyncEditSession aes) {
        this.editSession = aes;
        return this;
    }
    
    
    public void execute() {
        executor.execute(this);
    }

    
    public Traversal getStructureTraversal() {
        return traveral;
    }


    
    
}
