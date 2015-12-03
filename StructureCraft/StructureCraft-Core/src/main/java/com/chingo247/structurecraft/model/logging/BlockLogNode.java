/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.model.logging;

import com.chingo247.structurecraft.model.RelTypes;
import com.chingo247.structurecraft.model.structure.StructureNode;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

/**
 *
 * @author Chingo
 */
public class BlockLogNode {

    public static final String LABEL = "BLOCK_LOG";
    public static final String DATE_PROPERTY = "d";
    public static final String X_PROPERTY = "x";
    public static final String Y_PROPERTY = "y";
    public static final String Z_PROPERTY = "z";
    public static final String OLD_MATERIAL_PROPERTY = "om";
    public static final String OLD_DATA_PROPERTY = "od";
    public static final String NEW_MATERIAL_PROPERTY = "nm";
    public static final String NEW_DATA_PROPERTY = "nd";
    
    public static Label label() {
        return DynamicLabel.label(LABEL);
    }

    private final Node underlyingNode;

    public BlockLogNode(Node underlyingNode) {
        this.underlyingNode = underlyingNode;
    }
    
    public void setDate(long date) {
        this.underlyingNode.setProperty(DATE_PROPERTY, date);
    }
    
    public long getDate() {
        return (long) underlyingNode.getProperty(DATE_PROPERTY);
    }
    
    public int getX() {
        return (int)underlyingNode.getProperty(X_PROPERTY);
    }
    
    public int getY() {
        return (int)underlyingNode.getProperty(Y_PROPERTY);
    }
    
    public int getZ() {
        return (int)underlyingNode.getProperty(Z_PROPERTY);
    }
    
    public int getOldMaterial() {
        return (int)underlyingNode.getProperty(OLD_MATERIAL_PROPERTY);
    }
    
    public int getNewMaterial() {
        return (int)underlyingNode.getProperty(NEW_MATERIAL_PROPERTY);
    }
    
    public int getOldData() {
        return (int)underlyingNode.getProperty(OLD_DATA_PROPERTY);
    }
    
    public int getNewData() {
        return (int)underlyingNode.getProperty(NEW_DATA_PROPERTY);
    }
    
    public void setX(int x) {
        underlyingNode.setProperty(X_PROPERTY, x);
    }

    public void setY(int y) {
        underlyingNode.setProperty(Y_PROPERTY, y);
    }

    public void setZ(int z) {
        underlyingNode.setProperty(Z_PROPERTY, z);
    }

    public void setOldMaterial(int oldMaterial) {
        underlyingNode.setProperty(OLD_MATERIAL_PROPERTY, oldMaterial);
    }

    public void setNewMaterial(int newMaterial) {
        underlyingNode.setProperty(NEW_MATERIAL_PROPERTY, newMaterial);
    }

    public void setOldData(int oldData) {
        underlyingNode.setProperty(OLD_DATA_PROPERTY, oldData);
    }

    public void setNewData(int newData) {
        underlyingNode.setProperty(NEW_DATA_PROPERTY, newData);
    }
    
    public void setStructure(StructureNode structureNode) {
        structureNode.getNode().createRelationshipTo(underlyingNode, RelTypes.HAS_BLOCK);   
    }

}
