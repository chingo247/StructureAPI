/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.model.hologram;

import com.chingo247.structureapi.model.hologram.StructureHologramNode;
import com.chingo247.structureapi.model.structure.StructureNode;
import com.sk89q.worldedit.Vector;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Chingo
 */
public interface IStructureHologramRepository {
    
    public StructureHologramNode addHologram(StructureNode structure, Vector relativePosition);
    
    public Collection<StructureHologramNode> findAll();
    
    
}
