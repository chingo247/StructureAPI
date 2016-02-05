/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.model;

import com.chingo247.structureapi.model.structure.Structure;
import com.sk89q.worldedit.Vector;
import org.neo4j.graphdb.Node;

/**
 * An object that is related to the structure
 * @author Chingo
 */
public interface IStructureObject {
    
    Structure getStructure();
    
    Node getNode();
    
    String getName();
    
    double getX();
    
    double getY();
    
    double getZ();
    
    int getBlockX();
    
    int getBlockY();
    
    int getBlockZ();
    
    int getRelativeX();
    
    int getRelativeY();
    
    int getRelativeZ();
    
    Vector getPosition();
    
    Vector getRelativePosition();
    
}
