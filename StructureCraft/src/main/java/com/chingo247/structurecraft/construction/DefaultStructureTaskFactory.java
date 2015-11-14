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
package com.chingo247.structurecraft.construction;

import com.chingo247.structurecraft.construction.awe.AWEPlacementTask;
import com.chingo247.structurecraft.model.structure.ConstructionStatus;
import com.chingo247.structurecraft.model.structure.Structure;
import com.chingo247.structurecraft.model.structure.StructureNode;
import com.chingo247.structurecraft.construction.options.BlockPredicate;
import com.chingo247.structurecraft.construction.options.BuildOptions;
import com.chingo247.structurecraft.construction.options.DemolitionOptions;
import com.chingo247.structurecraft.construction.options.Options;
import com.chingo247.structurecraft.exeption.StructureException;
import com.chingo247.structurecraft.placement.BlockPlacement;
import com.chingo247.structurecraft.placement.DemolishingPlacement;
import com.chingo247.structurecraft.placement.interfaces.IPlacement;
import com.chingo247.structurecraft.placement.RestoringPlacement;
import com.chingo247.structurecraft.placement.interfaces.RotationalPlacement;
import com.chingo247.structurecraft.plan.interfaces.IStructurePlan;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.regions.CuboidRegion;
import java.util.UUID;
import org.neo4j.graphdb.Transaction;
import org.primesoft.asyncworldedit.AsyncWorldEditMain;
import org.primesoft.asyncworldedit.playerManager.PlayerEntry;

/**
 *
 * @author Chingo
 */
public class DefaultStructureTaskFactory implements IStructureTaskFactory {

    private static final String FIRST_BACKUP = "restore.snapshot";
    
    @Override
    public StructureTask build(EditSession session, UUID player, Structure structure) throws StructureException {
        return build(session, player, structure, new BuildOptions());
    }

    @Override
    public StructureTask build(EditSession session, UUID player, Structure structure, Options options) throws StructureException {
        StructureNode sn = new StructureNode(structure.getNode());

        // Get the placement and rotate
        IStructurePlan plan = structure.getStructurePlan();
        IPlacement p = plan.getPlacement();
        
        if (p instanceof RotationalPlacement) {
            RotationalPlacement rt = (RotationalPlacement) p;
            rt.rotate(structure.getDirection().getRotation());
        }
        
        // Get the AWE playerEntry
        PlayerEntry playerEntry = AsyncWorldEditMain.getInstance().getPlayerManager().getPlayer(player);
        ConstructionEntry ce = ConstructionManager.getInstance().getEntry(structure);
        
        System.out.println("[DefaultStructureTaskFactory]: ConstructionEntry " + ce);
        
        Vector pos = structure.getCuboidRegion().getMinimumPoint();
        
        // Make sure substructures are skipped during build
        for (StructureNode s : sn.getSubstructures()) {
            final CuboidRegion region = s.getCuboidRegion();

            options.addIgnore(new BlockPredicate() {

                @Override
                public boolean evaluate(Vector position, Vector worldPosition, BaseBlock block) {
                    return region.contains(worldPosition);
                }
            });
        }
        
        AWEPlacementTask task = new AWEPlacementTask(ConstructionStatus.BUILDING.name(), ce, p, playerEntry, session, pos, options);
        
        return task;

    }

    @Override
    public StructureTask demolish(EditSession session, UUID player, Structure structure) throws StructureException {
        return demolish(session, player, structure, new DemolitionOptions());
    }

    /**
     * Creates a task that will demolish a structure. <b>NOTE:</b> requires to be executed within a {@link Transaction}
     * @param session
     * @param player
     * @param structure
     * @param options
     * @return The StructureTask
     */
    @Override
    public StructureTask demolish(EditSession session, UUID player, Structure structure, Options options) throws StructureException {
        StructureNode sn = new StructureNode(structure.getNode());
        StructureNode parent = sn.getParent();
        DemolishingPlacement dp;
        CuboidRegion region = structure.getCuboidRegion();
        
        // If there is no parent or the structure doesn't have a placement inheriting from BlockPlacement
        // Then use the default removal method, which will clear the whole area
        if(parent == null || (!(parent.getStructurePlan() != null && parent.getStructurePlan().getPlacement() instanceof BlockPlacement))) {
            Vector size = region.getMaximumPoint().subtract(region.getMinimumPoint()).add(Vector.ONE);
            dp = new DemolishingPlacement(size);
        } else {
            // Otherwise use the parent to restore the area
            IPlacement parentPlacement = parent.getStructurePlan().getPlacement();
            if (parentPlacement instanceof RotationalPlacement) {
                RotationalPlacement rt = (RotationalPlacement) parentPlacement;
                rt.rotate(parent.getDirection().getRotation());
            }
            dp = new RestoringPlacement((BlockPlacement) parentPlacement);
        }
        
        // Get the AWE playerEntry
        PlayerEntry playerEntry = AsyncWorldEditMain.getInstance().getPlayerManager().getPlayer(player);
        ConstructionEntry ce = ConstructionManager.getInstance().getEntry(structure);
        Vector pos = structure.getCuboidRegion().getMinimumPoint();
        
        // Make sure the areas of substructures are skipped during block placement/removal
        for (StructureNode s : sn.getSubstructures()) {
            final CuboidRegion childRegion = s.getCuboidRegion();
            options.addIgnore(new BlockPredicate() {

                @Override
                public boolean evaluate(Vector position, Vector worldPosition, BaseBlock block) {
                    return childRegion.contains(worldPosition);
                }
            });
        }
        
        AWEPlacementTask task = new AWEPlacementTask(ConstructionStatus.DEMOLISHING.name(), ce, dp, playerEntry, session, pos, options);
        
        return task;
    }

   

}
