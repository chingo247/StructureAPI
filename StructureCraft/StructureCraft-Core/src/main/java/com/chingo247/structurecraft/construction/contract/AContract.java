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
package com.chingo247.structurecraft.construction.contract;

import com.chingo247.structurecraft.construction.IContract;
import com.chingo247.structurecraft.construction.listener.ConstructionListener;
import com.chingo247.structurecraft.construction.producer.BlockPlacementProducer;
import com.chingo247.structurecraft.placement.options.PlaceOptions;
import com.chingo247.structurecraft.placement.options.Traversal;
import java.util.UUID;
import java.util.logging.Logger;
import org.primesoft.asyncworldedit.worldedit.AsyncEditSession;

/**
 *
 * @author Chingo
 */
public abstract class AContract implements IContract {

    private static final Logger LOG = Logger.getLogger(AContract.class.getName());
    
    

    private boolean recursive, restrictive, reversed;
    private UUID player;
    private AsyncEditSession editSession;
    private Traversal traveral;
    private boolean useForce;
    private PlaceOptions placeOptions;

    public AContract() {
        this.placeOptions = new PlaceOptions();
    }
    
    public abstract ConstructionListener getConstructionListener();

    @Override
    public boolean isForced() {
        return useForce;
    }
    
    public void setPlaceOptions(PlaceOptions placeOptions) {
        this.placeOptions = placeOptions;
    }
    
    public PlaceOptions getPlaceOptions() {
        return placeOptions;
    }

    public AContract setStructureTraversal(Traversal traversal) {
        this.traveral = traversal;
        return this;
    }

    public AContract setForced(boolean useForce) {
        this.useForce = useForce;
        return this;
    }

    public AsyncEditSession getEditSession() {
        return editSession;
    }

    public UUID getPlayer() {
        return player;
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

    public AContract setRestrictive(boolean restrictive) {
        this.restrictive = restrictive;
        return this;
    }

    public AContract setRecursive(boolean recursive) {
        this.recursive = recursive;
        return this;
    }

    public AContract setReversedOrder(boolean reversed) {
        this.reversed = reversed;
        return this;
    }

    public AContract setPlayer(UUID player) {
        this.player = player;
        return this;
    }

    public AContract setEditsession(AsyncEditSession aes) {
        this.editSession = aes;
        return this;
    }

    public Traversal getStructureTraversal() {
        return traveral;
    }

    

}
