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
package com.chingo247.structurecraft.construction.awe;

import com.chingo247.settlercraft.core.SettlerCraft;
import com.chingo247.structurecraft.placement.IPlacement;
import com.chingo247.xplatform.core.IScheduler;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.primesoft.asyncworldedit.api.IAsyncWorldEdit;
import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacer;
import org.primesoft.asyncworldedit.playerManager.PlayerEntry;

/**
 *
 * @author Chingo
 * @param <T> Options
 * @param <P> The placement
 */
public abstract class AbstractAWEPlacement implements IPlacement {
    
    protected final IAsyncWorldEdit awe;
    protected final IBlockPlacer placer;
    protected final IScheduler scheduler;
    protected final IPlacement placement;
    protected final PlayerEntry playerEntry;

    /**
     * Constructor.
     *
     * @param playerEntry The PlayerEntry
     * @param placement The placement
     */
    public AbstractAWEPlacement(IAsyncWorldEdit asyncWorldEdit, PlayerEntry playerEntry, IPlacement placement) {
        this.playerEntry = playerEntry;
        this.awe = asyncWorldEdit;
        this.placer = awe.getBlockPlacer();
        this.placement = placement;
        this.scheduler = SettlerCraft.getInstance().getPlatform().getServer().getScheduler(SettlerCraft.getInstance().getPlugin());
    }

   

    @Override
    public Vector getOffset() {
        return placement.getOffset();
    }

    

    /**
     * Get next job id for current player
     *
     * @return Job id
     */
    protected int getJobId() {
        return placer.getJobId(playerEntry);
    }

    @Override
    public CuboidRegion getCuboidRegion() {
        return placement.getCuboidRegion();
    }

    
}
