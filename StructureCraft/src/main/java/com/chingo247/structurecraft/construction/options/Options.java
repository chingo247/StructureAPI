/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.construction.options;

import com.chingo247.structurecraft.construction.StructureTraversal;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Chingo
 */
public class Options {
    
    private int cubeX;
    private int cubeY;
    private int cubeZ;
    private final List<BlockPredicate> toIgnore;
    private final List<BlockMask> masks;
    private StructureTraversal traveral;
    private boolean traverseReversed;
    private boolean useForce;

    public Options() {
        this.cubeX = 16;
        this.cubeY = -1;
        this.cubeZ = 16;
        this.toIgnore = new ArrayList<>();
        this.masks = new ArrayList<>();
        this.traveral = StructureTraversal.BREADTH_FIRST;
        this.useForce = false;
        this.traverseReversed = false;
    }
    
    /**
     * Adds a block predicate, when the block predicate returns true, the block will not be placed
     * during the place of the placement
     * @param blockPredicate The blockPredicate
     */
    public void addIgnore(BlockPredicate blockPredicate) {
        toIgnore.add(blockPredicate);
    }

    /**
     * Returns all the BlockPredicates that will be determine if a block should be ignored
     * @return The blockPredicates
     */
    public List<BlockPredicate> getIgnore() {
        return toIgnore;
    }
    
    public List<BlockMask> getBlockMasks() {
        return masks;
    } 
    
    public void setUseForce(boolean useForce) {
        this.useForce = useForce;
    }

    public boolean isForced() {
        return useForce;
    }
    
    public void addBlockMask(BlockMask mask) {
        this.masks.add(mask);
    }
    
    public int getCubeX() {
        return cubeX;
    }

    public void setCubeX(int cubeX) {
        this.cubeX = cubeX;
    }

    public int getCubeY() {
        return cubeY;
    }

    public void setCubeY(int cubeY) {
        this.cubeY = cubeY;
    }

    public int getCubeZ() {
        return cubeZ;
    }

    public void setCubeZ(int cubeZ) {
        this.cubeZ = cubeZ;
    }
    
    public void setTraveral(StructureTraversal traveral) {
        this.traveral = traveral;
    }

    public StructureTraversal getTraveral() {
        return traveral;
    }

    public void setTraversingReversed(boolean traverseReversed) {
        this.traverseReversed = traverseReversed;
    }

    public boolean isTraversingReversed() {
        return traverseReversed;
    }
    
    
    
   
    
}
