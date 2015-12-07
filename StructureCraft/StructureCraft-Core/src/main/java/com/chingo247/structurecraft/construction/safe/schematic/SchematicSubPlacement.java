/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.construction.safe.schematic;

import com.chingo247.structurecraft.construction.options.PlaceOptions;
import com.chingo247.structurecraft.placement.BlockPlacement;
import com.chingo247.structurecraft.placement.interfaces.IBlockPlacement;
import com.chingo247.structurecraft.util.RegionUtil;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.regions.CuboidRegion;

/**
 *
 * @author Chingo
 */
class SchematicSubPlacement extends BlockPlacement {

    private CuboidRegion toPlace;
    private IBlockPlacement placement;

    public SchematicSubPlacement(IBlockPlacement placement, CuboidRegion toPlace) {
        super(placement.getWidth(), placement.getHeight(), placement.getLength());
        this.placement = placement;
        this.toPlace = toPlace;
    }

    @Override
    public void place(EditSession editSession, Vector pos, PlaceOptions option) {
        // Cube traverse this clipboard
        Vector min = toPlace.getMinimumPoint();
        Vector max = toPlace.getMaximumPoint();

        for (int x = min.getBlockX(); x < max.getBlockX(); x++) {
            for (int z = min.getBlockX(); z < max.getBlockX(); z++) {
                for (int y = min.getBlockX(); y < max.getBlockX(); y++) {
                    Vector currentPos = new BlockVector(x, y, z);
                    BaseBlock b = placement.getBlock(currentPos);
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
    public final Vector getSize() {
        return RegionUtil.getSize(toPlace);
    }

    @Override
    public int getWidth() {
        return getSize().getBlockX();
    }

    @Override
    public int getHeight() {
        return getSize().getBlockY();
    }

    @Override
    public int getLength() {
        return getSize().getBlockZ();
    }

    @Override
    public CuboidRegion getCuboidRegion() {
        return toPlace;
    }

    @Override
    public BaseBlock getBlock(Vector position) {
        return placement.getBlock(position);
    }

}
