/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.model.logging;

import com.chingo247.structurecraft.model.structure.StructureNode;
import org.neo4j.graphdb.Node;

/**
 *
 * @author Chingo
 */
public class BlockLogNode {

    public static final String LABEL = "BLOCK_LOG";

    private Node underlyingNode;

    public BlockLogNode(Node underlyingNode) {
        this.underlyingNode = underlyingNode;
    }
    
    public void setDate(long date) {
        this.underlyingNode.setProperty("d", date);
    }
    
    public long getDate() {
        return (long) underlyingNode.getProperty("d");
    }
    
    public int getX() {
        return (int)underlyingNode.getProperty("x");
    }
    
    public int getY() {
        return (int)underlyingNode.getProperty("y");
    }
    
    public int getZ() {
        return (int)underlyingNode.getProperty("z");
    }
    
    public int getOldMaterial() {
        return (int)underlyingNode.getProperty("om");
    }
    
    public int getNewMaterial() {
        return (int)underlyingNode.getProperty("nm");
    }
    
    public int getOldData() {
        return (int)underlyingNode.getProperty("od");
    }
    
    public int getNewData() {
        return (int)underlyingNode.getProperty("nd");
    }
    
    public void setX(int x) {
        underlyingNode.setProperty("x", x);
    }

    public void setY(int y) {
        underlyingNode.setProperty("y", y);
    }

    public void setZ(int z) {
        underlyingNode.setProperty("z", z);
    }

    public void setOldMaterial(int oldMaterial) {
        underlyingNode.setProperty("om", oldMaterial);
    }

    public void setNewMaterial(int newMaterial) {
        underlyingNode.setProperty("nm", newMaterial);
    }

    public void setOldData(int oldData) {
        underlyingNode.setProperty("od", oldData);
    }

    public void setNewData(int newData) {
        underlyingNode.setProperty("nd", newData);
    }
    
    public void setStructure(StructureNode structureNode) {
        structureNode.getNode().createRelationshipTo(underlyingNode, LogRelTypes.HAS_BLOCK);   
    }

}
