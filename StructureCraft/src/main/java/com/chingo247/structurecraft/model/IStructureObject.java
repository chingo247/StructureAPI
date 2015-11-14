/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.model;

import com.chingo247.structurecraft.model.structure.Structure;
import com.sk89q.worldedit.Vector;
import org.neo4j.graphdb.Node;

/**
 * An object that is related to the structure
 * @author Chingo
 */
public interface IStructureObject {
    
    public Structure getStructure();
    
    public Node getNode();
    
    public String getName();
    
    public double getX();
    
    public double getY();
    
    public double getZ();
    
    public int getBlockX();
    
    public int getBlockY();
    
    public int getBlockZ();
    
    public int getRelativeX();
    
    public int getRelativeY();
    
    public int getRelativeZ();
    
    public Vector getPosition();
    
    public Vector getRelativePosition();
    
}
