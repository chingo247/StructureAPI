/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.worldguard.protection;

import com.google.common.base.Preconditions;
import org.neo4j.graphdb.Node;

/**
 *
 * @author Chingo
 */
public class WorldGuardRegionNode {

    public static final String LABEL = "WORLDGUARD_REGION";
    public static final String REGION_PROPERTY = "region";
    public static final String CREATED_AT_PROPERTY = "createdAt";
    public static final String EXPIRED_PROPERTY = "expired";

    private Node underlyingNode;

    public WorldGuardRegionNode(Node node) {
        Preconditions.checkNotNull(node, "node may not be null");
        this.underlyingNode = node;
    }

    public Node getNode() {
        return underlyingNode;
    }
    
    public void setRegion(String region) {
        this.underlyingNode.setProperty(REGION_PROPERTY, region);
    }

    public String getRegion() {
        return (String) this.underlyingNode.getProperty(REGION_PROPERTY);
    }

    public void setCreatedAt(long date) {
        this.underlyingNode.setProperty(CREATED_AT_PROPERTY, date);
    }

    public long getCreatedAt() {
        return (long) this.underlyingNode.getProperty(CREATED_AT_PROPERTY);
    }

    public void setExpired(boolean date) {
        this.underlyingNode.setProperty(EXPIRED_PROPERTY, date);
    }

    public long getExpired() {
        return (long) this.underlyingNode.getProperty(EXPIRED_PROPERTY);
    }

}
