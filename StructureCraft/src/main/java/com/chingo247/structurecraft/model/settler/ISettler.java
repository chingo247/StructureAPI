/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.model.settler;

import com.chingo247.structurecraft.model.structure.StructureNode;
import com.chingo247.settlercraft.core.model.interfaces.IBaseSettler;
import java.util.List;

/**
 * Represents someone who owns a structure
 * @author Chingo
 */
public interface ISettler extends IBaseSettler {
    
    /**
     * Gets all the structures this Settler owns
     * @return The structures
     */
    public List<StructureNode> getStructures();
    
    /**
     * Gets all the structure this settler owns
     * @param skip The amount of nodes to skip
     * @param limit The max amount of nodes to return
     * @return The structures
     */
    public List<StructureNode> getStructures(int skip, int limit);
    
    /**
     * Counts the total amount of structures owned by this Settler
     * @return The total amount of strucutre owned
     */
    public int getStructureCount();
    
    
    
}
