/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.construction.rollback;

import com.chingo247.structurecraft.construction.IConstructionEntry;

/**
 *
 * @author Chingo
 */
public interface IRollbackConstructionEntry extends IConstructionEntry {
    
    IRollbackPlacementSource getPlacementSource();
    
}
