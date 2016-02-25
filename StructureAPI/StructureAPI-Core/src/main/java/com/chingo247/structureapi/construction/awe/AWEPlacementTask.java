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
package com.chingo247.structureapi.construction.awe;

import com.chingo247.structureapi.StructureAPI;
import com.chingo247.structureapi.construction.task.ITaskStartedListener;
import com.chingo247.structureapi.construction.task.StructurePlacingTask;
import com.chingo247.structureapi.event.task.StructureTaskStartEvent;
import com.chingo247.structureapi.placement.options.PlaceOptions;
import com.chingo247.structureapi.placement.IPlacement;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import java.util.UUID;
import org.primesoft.asyncworldedit.api.IAsyncWorldEdit;
import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacer;
import org.primesoft.asyncworldedit.playerManager.PlayerEntry;
import com.chingo247.structureapi.construction.StructureEntry;
import com.chingo247.structureapi.event.task.StructureTaskQueuedEvent;

/**
 * AWE Placement task, places a placement by using AsyncWorldEdit. Note this
 * task needs to be executed outside of the main thread!
 *
 * @author Chingo
 * @param <T> The Options Type
 */
public class AWEPlacementTask extends StructurePlacingTask {

    private final IPlacement placement;
    private final PlayerEntry playerEntry;
    private final Vector position;
    private final EditSession editSession;
    private int jobId;
    private final IAsyncWorldEdit asyncWorldEdit;

    private boolean checked;

    /**
     * Constructor.
     *
     * @param callback
     * @param structureEntry The constructionEntry
     * @param placement The placement
     * @param playerOrRandomUUID A player or random UUID used to track the process. Jobs will be queued on this specific UUID.
     * @param editSession The editsession
     * @param position The position
     * @param options The options to use when placing
     */
    public AWEPlacementTask(IAsyncWorldEdit asyncWorldEdit, StructureEntry structureEntry, IPlacement placement, UUID playerOrRandomUUID, EditSession editSession, Vector position) {
        super(structureEntry, playerOrRandomUUID);
        this.playerEntry = asyncWorldEdit.getPlayerManager().getPlayer(playerOrRandomUUID);
        this.position = position;
        this.placement = placement;
        this.jobId = -1;
        this.editSession = editSession;
        this.asyncWorldEdit = asyncWorldEdit;
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
        AWEPlacement p = new AWEPlacement(asyncWorldEdit, playerEntry, placement, t.getUUID(), new IAWECallback() {

            @Override
            public void onJobAdded(AWEJobEntry job) {
                setJobId(job.getJobId());
                AWEJobManager.getInstance().register(t);
                StructureAPI.getInstance().getEventDispatcher().dispatchEvent(new StructureTaskQueuedEvent(AWEPlacementTask.this));
            }

            @Override
            public void onCancelled() {
                cancel();
            }

            @Override
            public void onStarted() {
                for(ITaskStartedListener listener : getListeners()) {
                    listener.onStarted(AWEPlacementTask.this);
                }
                StructureAPI.getInstance().getEventDispatcher().dispatchEvent(new StructureTaskStartEvent(t));
            }
        }
        );
        
        p.place(editSession, position, getOptions() != null ? getOptions() : new PlaceOptions());
    }

    @Override
    protected void onCancel() {
        IBlockPlacer bp = asyncWorldEdit.getBlockPlacer();
        bp.cancelJob(playerEntry, jobId);
        AWEJobManager.getInstance().unregister(this);
    }
    
    

}
