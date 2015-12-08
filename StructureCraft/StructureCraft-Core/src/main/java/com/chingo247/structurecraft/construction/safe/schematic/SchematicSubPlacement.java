/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.construction.safe.schematic;

import com.chingo247.structurecraft.placement.options.PlaceOptions;
import com.chingo247.structurecraft.model.structure.IStructure;
import com.chingo247.structurecraft.placement.BlockPlacement;
import com.chingo247.structurecraft.placement.interfaces.IBlockPlacement;
import com.chingo247.structurecraft.util.RegionUtil;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.parboiled.common.Preconditions;

/**
 *
 * @author Chingo
 */
class SchematicSubPlacement extends BlockPlacement {

    private CuboidRegion toPlace;
    private BlockPlacement placement;

    public SchematicSubPlacement(BlockPlacement placement, CuboidRegion toPlace) {
        super(placement.getWidth(), placement.getHeight(), placement.getLength());
        this.placement = placement;
        this.toPlace = toPlace;
    }

    @Override
    public void place(EditSession editSession, Vector pos, PlaceOptions option) {
        Vector min = toPlace.getMinimumPoint();
        Vector max = toPlace.getMaximumPoint();
        for (int y = min.getBlockY(); y < max.getBlockY(); y++) {
            for (int x = min.getBlockX(); x < max.getBlockX(); x++) {
                for (int z = min.getBlockZ(); z < max.getBlockZ(); z++) {

                    Vector currentPos = new BlockVector(x, y, z);
                    BaseBlock b = placement.getBlock(currentPos);
                    if (b == null) {
                        continue;
                    }
                    doBlock(editSession, pos, currentPos, b, option);
                }
            }
        }

    }

    @Override
    public Vector getOffset() {
        return placement.getOffset();
    }

    @Override
    public int getRotation() {
        return placement.getRotation();
    }

    @Override
    public final Vector getSize() {
        return RegionUtil.getSize(toPlace);
    }

    @Override
    public CuboidRegion getCuboidRegion() {
        return toPlace;
    }

    @Override
    public BaseBlock getBlock(Vector position) {
        return placement.getBlock(position);
    }

    @Override
    public boolean hasBlock(Vector position) {
        return placement.hasBlock(position);
    }

}
