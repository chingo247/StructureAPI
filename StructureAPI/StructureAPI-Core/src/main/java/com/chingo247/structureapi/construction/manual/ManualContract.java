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
package com.chingo247.structureapi.construction.manual;

import com.chingo247.settlercraft.core.Direction;
import com.chingo247.structureapi.construction.StructureEntry;
import com.chingo247.structureapi.construction.contract.BuildContract;
import com.chingo247.structureapi.construction.producer.ManualOptions;
import com.chingo247.structureapi.exeption.StructureException;
import com.chingo247.structureapi.exeption.StructurePlanException;
import com.chingo247.structureapi.placement.block.SchematicPlacement;
import com.chingo247.structureapi.placement.options.BlockMask;
import com.chingo247.structureapi.placement.options.BlockPredicate;
import com.chingo247.structureapi.placement.options.PlaceOptions;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.math.convolution.HeightMap;
import com.sk89q.worldedit.regions.CuboidRegion;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chingo
 */
public class ManualContract extends BuildContract {

    public ManualContract() {
    }
    
    

    @Override
    public void apply(StructureEntry entry, final PlaceOptions placeOptions) throws StructureException {
        applyManual(entry, placeOptions);
        super.apply(entry, placeOptions);
    }
    
    public void applyManual(StructureEntry entry, final PlaceOptions placeOptions) {
        final CuboidRegion region = entry.getStructure().getCuboidRegion();
        
        final int width;
        final int length;
        Direction d = entry.getStructure().getDirection();
        
        if(d == Direction.EAST || d == Direction.WEST) {
            width = region.getWidth();
            length = region.getLength();
        } else {
            width = region.getLength();
            length = region.getWidth();
        }
        
        
        final BlockPredicate framepredicate = new BlockPredicate() {

            @Override
            public boolean evaluate(Vector position, Vector worldPosition, BaseBlock block) {
                return position.getBlockY() > 0
                        && block.getId() != 0 && block.getId() != BlockID.WATER && block.getId() != BlockID.LAVA;
            }
        };
        
        final int max = Math.max(width, length);
        
        final BlockMask frameMask = new BlockMask() {

            @Override
            public BlockPredicate getBlockPredicate() {
                return framepredicate;
            }

            @Override
            public BaseBlock apply(Vector relativePosition, Vector worldPosition, BaseBlock block) {
                if (framepredicate.evaluate(relativePosition, worldPosition, block)) {
                    // Defaults
                    int frameMaterial = BlockID.WOOD;
                    int frameData = 0;
                    int frameSize = Math.min(3, max);
                    if (placeOptions instanceof ManualOptions) {
                        ManualOptions options = (ManualOptions) placeOptions;
                        frameMaterial = options.getFrameMaterial();
                        frameData = options.getFrameData();
                        frameSize = options.getFrameSize();
                    }

                    if (/*relativePosition.getBlockY() % frameSize == 0 || */
                             relativePosition.getBlockX() % frameSize == 0 
                            || relativePosition.getBlockZ() % frameSize == 0
                            ) {
                        return new BaseBlock(frameMaterial, frameData);
                    } else {
                        return new BaseBlock(0);
                    }
                } else {
                    return block;
                }
            }
        };

        final BlockPredicate fencePredicate = new BlockPredicate() {

            @Override
            public boolean evaluate(Vector position, Vector worldPosition, BaseBlock block) {
                int fenceHeight = 1;
                if (placeOptions instanceof ManualOptions) {
                    fenceHeight = ((ManualOptions) placeOptions).getFenceHeight();
                }
                
                
                return position.getBlockY() > 0
                        && block.getId() == 0 
                        && position.getBlockY() < fenceHeight + 1 // Ignores first layer
                        
                        && ((position.getBlockX() == 0 || position.getBlockX() == width - 1)
                        || (position.getBlockZ() == 0 || position.getBlockZ() == length - 1));
            }
        };
        final BlockMask fenceMask = new BlockMask() {

            @Override
            public BlockPredicate getBlockPredicate() {
                return fencePredicate;
            }

            @Override
            public BaseBlock apply(Vector relativePosition, Vector worldPosition, BaseBlock block) {
                if (fencePredicate.evaluate(relativePosition, worldPosition, block)) {
                    // Defaults
                    int fenceMaterial = BlockID.IRON_BARS;
                    int fenceData = 0;
                    if (placeOptions instanceof ManualOptions) {
                        ManualOptions options = (ManualOptions) placeOptions;
                        fenceMaterial = options.getFrameMaterial();
                        fenceData = options.getFrameData();
                    }
                    return new BaseBlock(fenceMaterial, fenceData);
                }
                return block;
            }
        };

        final BlockPredicate foundationPredicate = new BlockPredicate() {

            @Override
            public boolean evaluate(Vector position, Vector worldPosition, BaseBlock block) {
                return position.getBlockY() == 0
                        && (block.getId() != 0 && block.getId() != BlockID.WATER && block.getId() != BlockID.LAVA);
                        
                        
            }
        };
        final BlockMask foundationMask = new BlockMask() {

            @Override
            public BlockPredicate getBlockPredicate() {
                return foundationPredicate;
            }

            @Override
            public BaseBlock apply(Vector relativePosition, Vector worldPosition, BaseBlock block) {
                if (foundationPredicate.evaluate(relativePosition, worldPosition, block)) {
                    // Defaults
                    int foundationMaterial = BlockID.STONE;
                    int foundationData = 0;
                    if (placeOptions instanceof ManualOptions) {
                        ManualOptions options = (ManualOptions) placeOptions;
                        foundationMaterial = options.getFrameMaterial();
                        foundationData = options.getFrameData();
                    }
                    return new BaseBlock(foundationMaterial, foundationData);
                }
                return block;
            }
        };
        
        placeOptions.addBlockMask(foundationMask);
        placeOptions.addBlockMask(frameMask);
        placeOptions.addBlockMask(fenceMask);
        
        
    }

}
