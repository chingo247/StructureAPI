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

import com.chingo247.settlercraft.core.event.async.AsyncEventManager;
import com.chingo247.structurecraft.construction.ConstructionEntry;
import com.chingo247.structurecraft.construction.StructureTask;
import com.chingo247.structurecraft.construction.event.StructureTaskStartEvent;
import com.chingo247.structurecraft.event.async.StructureJobAddedEvent;
import com.chingo247.structurecraft.construction.options.Options;
import com.chingo247.structurecraft.placement.interfaces.IPlacement;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import org.primesoft.asyncworldedit.AsyncWorldEditMain;
import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacer;
import org.primesoft.asyncworldedit.playerManager.PlayerEntry;

/**
 * AWE Placement task, places a placement by using AsyncWorldEdit. Note this
 * task needs to be executed outside of the main thread!
 *
 * @author Chingo
 * @param <T> The Options Type
 */
public class AWEPlacementTask<T extends Options> extends StructureTask {

    private final IPlacement placement;
    private final PlayerEntry playerEntry;
    private final Vector position;
    private final EditSession editSession;
    private final T options;
    private int jobId;

    private boolean checked;

    /**
     * Constructor.
     *
     * @param action
     * @param connstructionEntry The constructionEntry
     * @param placement The placement
     * @param playerEntry The playerEntry
     * @param editSession The editsession
     * @param position The position
     * @param options The options to use when placing
     */
    public AWEPlacementTask(String action, ConstructionEntry connstructionEntry, IPlacement placement, PlayerEntry playerEntry, EditSession editSession, Vector position, T options) {
        super(action, connstructionEntry, playerEntry.getUUID());
        this.playerEntry = playerEntry;
        this.position = position;
        this.placement = placement;
        this.options = options;
        this.jobId = -1;
        this.editSession = editSession;
    }

    void setJobId(int id) {
        this.jobId = id;
    }

    public int getJobId() {
        return jobId;
    }

    boolean isChecked() {
        return checked;
    }

    void setChecked(boolean checked) {
        this.checked = checked;
    }

    @Override
    protected void execute() {
        final AWEPlacementTask t = this;
        AWEPlacement p = new AWEPlacement(playerEntry, placement, t.getUUID(), new IAWECallback() {

            @Override
            public void onJobAdded(AWEJobEntry job) {
                setJobId(job.getJobId());
                System.out.println("Added task " + t.getUUID() + ", jobId: " + jobId);
                AWEJobManager.getInstance().register(t);
                AsyncEventManager.getInstance().post(new StructureJobAddedEvent(getConstructionEntry().getStructure(), jobId, playerEntry));
            }

            @Override
            public void onCancelled() {
                cancel();
            }

            @Override
            public void onStarted() {
                AsyncEventManager.getInstance().post(new StructureTaskStartEvent(t));
            }
        }
        );
        
        p.place(editSession, position, options);
    }

    @Override
    protected void onCancel() {
        IBlockPlacer bp = AsyncWorldEditMain.getInstance().getBlockPlacer();
        bp.cancelJob(playerEntry, jobId);
        AWEJobManager.getInstance().unregister(this);
    }
    
    

}
