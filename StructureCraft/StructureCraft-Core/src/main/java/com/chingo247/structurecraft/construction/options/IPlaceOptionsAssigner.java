/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.construction.options;

import com.chingo247.structurecraft.construction.IConstructionEntry;
import com.chingo247.structurecraft.construction.options.PlaceOptions;

/**
 *
 * @author Chingo
 */
public interface IPlaceOptionsAssigner {
    
    
    PlaceOptions getPlaceOptionsFor(IConstructionEntry entry);
    
}
