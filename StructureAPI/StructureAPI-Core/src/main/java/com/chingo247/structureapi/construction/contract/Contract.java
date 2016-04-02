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
package com.chingo247.structureapi.construction.contract;

import com.chingo247.structureapi.construction.PlaceOptionsFactory;
import com.chingo247.structureapi.construction.StructureEditSessionFactory;
import com.chingo247.structureapi.construction.StructureEntry;
import com.chingo247.structureapi.construction.engine.BlockPlaceEngine;
import com.chingo247.structureapi.construction.listener.ConstructionListener;
import com.chingo247.structureapi.construction.producer.IPlacementProducer;
import com.chingo247.structureapi.exeption.StructureException;
import com.chingo247.structureapi.model.structure.Structure;
import com.chingo247.structureapi.placement.block.IBlockPlacement;
import com.chingo247.structureapi.placement.options.PlaceOptions;
import com.chingo247.structureapi.placement.options.Traversal;
import java.util.UUID;
import java.util.logging.Logger;

/**
 *
 * @author Chingo
 */
public abstract class Contract  {

    private static final Logger LOG = Logger.getLogger(Contract.class.getName());
    
    private static final PlaceOptionsFactory DEFAULT_FACTORY = new PlaceOptionsFactory() {

        @Override
        public PlaceOptions makeOptions(Structure structure) {
            PlaceOptions options = new PlaceOptions();
            options.setCubeY(structure.getCuboidRegion().getHeight() / 2);
            return options;
        }
    };

    private boolean recursive, restrictive, reversed;
    private UUID player;
    private StructureEditSessionFactory editSessionFactory;
    private Traversal traveral;
    private boolean useForce;
    private PlaceOptionsFactory placeOptionsFactory;

    public Contract() {
    }
    
    public abstract ConstructionListener getConstructionListener();

    public boolean isForced() {
        return useForce;
    }
    
    public abstract IPlacementProducer<IBlockPlacement> getPlacementProducer();
    
    public abstract void apply(StructureEntry entry, PlaceOptions  placeOptions) throws StructureException;
    
    public Contract setPlaceOptionsFactory(PlaceOptionsFactory placeOptionsFactory) {
        this.placeOptionsFactory = placeOptionsFactory;
        return this;
    }
    
    public PlaceOptionsFactory getPlaceOptionsFactory() {
        if(placeOptionsFactory == null) {
            return DEFAULT_FACTORY;
        }
        return placeOptionsFactory;
    }

    public Contract setStructureTraversal(Traversal traversal) {
        this.traveral = traversal;
        return this;
    }

    public Contract setForced(boolean useForce) {
        this.useForce = useForce;
        return this;
    }

    public StructureEditSessionFactory getEditSessionFactory() {
        return editSessionFactory;
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

    public Contract setRestrictive(boolean restrictive) {
        this.restrictive = restrictive;
        return this;
    }

    public Contract setRecursive(boolean recursive) {
        this.recursive = recursive;
        return this;
    }

    public Contract setReversedOrder(boolean reversed) {
        this.reversed = reversed;
        return this;
    }

    public Contract setPlayer(UUID player) {
        this.player = player;
        return this;
    }

    public Contract setEditSessionFactory(StructureEditSessionFactory aes) {
        this.editSessionFactory = aes;
        return this;
    }

    public Traversal getStructureTraversal() {
        return traveral;
    }

    

}
