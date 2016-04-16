/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.worldguard.protection;

import org.neo4j.graphdb.Node;

/**
 *
 * @author Chingo
 */
public class WorldGuardRegionNode {

    private static final String REGION_PROPERTY = "region";
    private static final String CREATED_AT_PROPERTY = "createdAt";
    private static final String EXPIRED_PROPERTY = "expired";

    private Node underlyingNode;

    public WorldGuardRegionNode(Node underlyingNode) {
        this.underlyingNode = underlyingNode;
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
