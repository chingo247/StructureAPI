/*
 * Copyright (C) 2016 Chingo
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
package com.chingo247.structureapi.construction;

import com.chingo247.structureapi.construction.listener.ConstructionListener;
import com.chingo247.structureapi.construction.producer.IPlacementProducer;
import com.chingo247.structureapi.exeption.StructureException;
import com.chingo247.structureapi.placement.block.IBlockPlacement;
import com.chingo247.structureapi.placement.options.PlaceOptions;
import com.chingo247.structureapi.placement.options.Traversal;
import java.util.UUID;
import org.primesoft.asyncworldedit.worldedit.AsyncEditSession;

/**
 *
 * @author Chingo
 */
public interface IContract {

    IContract setForced(boolean forced);

    IContract setStructureTraversal(Traversal traversal);

    IContract setRestrictive(boolean restrictive);

    IContract setRecursive(boolean recursive);

    IContract setReversedOrder(boolean reversed);

    IContract setPlayer(UUID player);

    IContract setEditsession(AsyncEditSession aes);

    AsyncEditSession getEditSession();

    UUID getPlayer();

    boolean isRecursive();

    boolean isRestrictive();

    boolean isReversed();

    boolean isForced();
    
    PlaceOptions getPlaceOptions();
    
    IContract setPlaceOptions(PlaceOptions options);

    Traversal getStructureTraversal();
    
    IPlacementProducer<IBlockPlacement> getPlacementProducer();
    
    ConstructionListener getConstructionListener();
    
    void apply(IStructureEntry entry) throws StructureException;
    
}
