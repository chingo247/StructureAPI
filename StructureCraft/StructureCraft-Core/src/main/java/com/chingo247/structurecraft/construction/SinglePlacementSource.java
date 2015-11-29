/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.construction;

import com.chingo247.structurecraft.placement.interfaces.IPlacement;

/**
 *
 * @author Chingo
 */
public class SinglePlacementSource implements IPlacementSource {
    
    private IPlacement placement;

    public SinglePlacementSource(IPlacement placement) {
        this.placement = placement;
    }
    
    @Override
    public IPlacement nextPlacement() {
        IPlacement reference = placement;
        placement = null;
        return reference;
    }

    @Override
    public boolean hasNext() {
        return false;
    }
    
    
    
}
