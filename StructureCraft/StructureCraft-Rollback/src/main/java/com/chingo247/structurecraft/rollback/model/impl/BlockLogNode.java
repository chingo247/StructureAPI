/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.rollback.model.impl;

import com.chingo247.structurecraft.rollback.model.IBlockLog;
import com.chingo247.structurecraft.model.structure.StructureNode;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

/**
 *
 * @author Chingo
 */
public class BlockLogNode implements IBlockLog {

    public static final String LABEL = "BLOCK_LOG";
    public static final String DATE_PROPERTY = "d";
    public static final String X_PROPERTY = "x";
    public static final String Y_PROPERTY = "y";
    public static final String Z_PROPERTY = "z";
    public static final String OLD_MATERIAL_PROPERTY = "om";
    public static final String OLD_DATA_PROPERTY = "od";
    public static final String NEW_MATERIAL_PROPERTY = "nm";
    public static final String NEW_DATA_PROPERTY = "nd";
    
    public static final String RELATION_HAS_BLOCK = "hb";
    
    public static Label label() {
        return DynamicLabel.label(LABEL);
    }

    private final Node underlyingNode;

    public BlockLogNode(Node underlyingNode) {
        this.underlyingNode = underlyingNode;
    }
    
    @Override
    public void setDate(long date) {
        this.underlyingNode.setProperty(DATE_PROPERTY, date);
    }
    
    @Override
    public long getDate() {
        return (long) underlyingNode.getProperty(DATE_PROPERTY);
    }
    
    @Override
    public int getX() {
        return (int)underlyingNode.getProperty(X_PROPERTY);
    }
    
    @Override
    public int getY() {
        return (int)underlyingNode.getProperty(Y_PROPERTY);
    }
    
    @Override
    public int getZ() {
        return (int)underlyingNode.getProperty(Z_PROPERTY);
    }
    
    @Override
    public int getOldMaterial() {
        return (int)underlyingNode.getProperty(OLD_MATERIAL_PROPERTY);
    }
    
    @Override
    public int getNewMaterial() {
        return (int)underlyingNode.getProperty(NEW_MATERIAL_PROPERTY);
    }
    
    @Override
    public int getOldData() {
        return (int)underlyingNode.getProperty(OLD_DATA_PROPERTY);
    }
    
    @Override
    public int getNewData() {
        return (int)underlyingNode.getProperty(NEW_DATA_PROPERTY);
    }
    
    @Override
    public void setX(int x) {
        underlyingNode.setProperty(X_PROPERTY, x);
    }

    @Override
    public void setY(int y) {
        underlyingNode.setProperty(Y_PROPERTY, y);
    }

    @Override
    public void setZ(int z) {
        underlyingNode.setProperty(Z_PROPERTY, z);
    }

    @Override
    public void setOldMaterial(int oldMaterial) {
        underlyingNode.setProperty(OLD_MATERIAL_PROPERTY, oldMaterial);
    }

    @Override
    public void setNewMaterial(int newMaterial) {
        underlyingNode.setProperty(NEW_MATERIAL_PROPERTY, newMaterial);
    }

    @Override
    public void setOldData(int oldData) {
        underlyingNode.setProperty(OLD_DATA_PROPERTY, oldData);
    }

    @Override
    public void setNewData(int newData) {
        underlyingNode.setProperty(NEW_DATA_PROPERTY, newData);
    }
    
    @Override
    public void setStructure(StructureNode structureNode) {
        structureNode.getNode().createRelationshipTo(underlyingNode, DynamicRelationshipType.withName(RELATION_HAS_BLOCK));   
    }

}
