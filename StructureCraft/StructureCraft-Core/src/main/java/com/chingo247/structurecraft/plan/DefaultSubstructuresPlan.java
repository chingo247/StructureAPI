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


package com.chingo247.structurecraft.plan;

import com.chingo247.structurecraft.plan.interfaces.IStructurePlan;
import com.chingo247.structurecraft.plan.interfaces.ISubstructurePlan;
import com.chingo247.structurecraft.plan.io.exception.PlanException;
import com.chingo247.structurecraft.placement.interfaces.IPlacement;
import com.chingo247.structurecraft.plan.io.export.StructurePlanExporter;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

/**
 *
 * @author Chingo
 */
public final class DefaultSubstructuresPlan extends AbstractStructurePlan implements ISubstructurePlan {

    private final DefaultSubstructuresPlan parent;
    private final IPlacement mainPlacement;
    private final Set<IStructurePlan> plans;
    private final Set<IPlacement> placements; // Substructure - placeable

    public DefaultSubstructuresPlan(String id, File structurePlanFile, DefaultSubstructuresPlan parent, IPlacement placement) {
        super(id, structurePlanFile);
        this.parent = parent;
        this.mainPlacement = placement;
        this.placements = Sets.newHashSet();
        this.plans = Sets.newHashSet();
    }

    @Override
    public boolean removePlacement(IPlacement placement) {
        return placements.remove(placement);
    }

    @Override
    public void addPlacement(IPlacement placement) {
        placements.add(placement);
    }

    @Override
    public void addStructurePlan(IStructurePlan plan) {
        if (matchesParentRecursively(plan.getFile())) {
            throw new PlanException("Plans may not be equal to any of its ancestors.");
        }
        plans.add(plan);
    }

    @Override
    public boolean removeStructurePlan(IStructurePlan plan) {
        return plans.remove(plan);
    }

    @Override
    public IPlacement getPlacement() {
        return mainPlacement;
    }

    @Override
    public DefaultSubstructuresPlan getParent() {
        return parent;
    }

    @Override
    public Collection<IPlacement> getSubPlacements() {
        return placements;
    }

    @Override
    public Collection<IStructurePlan> getSubStructurePlans() {
        return plans;
    }

    /**
     * Will check if the corresponding plan matches any Ancestors (recursively)
     *
     * @param plan The StructurePlan to check
     * @return True if it matches any ancestors
     */
    public boolean matchesParentRecursively(File file) {
        if (hash(file).equals(getPathHash())) {
            return true;
        } else if (parent != null) {
            return parent.matchesParentRecursively(file);
        } else {
            return false;
        }
    }

    @Override
    public synchronized void save() throws IOException {
        StructurePlanExporter exporter = new StructurePlanExporter();
        exporter.export(this, getFile().getParentFile(), getName() + ".xml", true);
    }

}
