/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.model.zone;

import com.chingo247.structurecraft.StructureAPI;
import com.chingo247.structurecraft.model.AccessType;
import com.chingo247.structurecraft.model.owner.OwnerDomain;
import com.chingo247.structurecraft.model.plot.Plot;
import com.chingo247.xplatform.core.IWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.neo4j.graphdb.Node;

/**
 *
 * @author Chingo
 */
public class ConstructionZone extends Plot implements IConstructionZone {
    
    private Long id;
    private AccessType accessType;
    private Node underlyingNode;
    private IWorld world;
    private CuboidRegion region;
    
    public ConstructionZone(Node node) {
        this(new ConstructionZoneNode(node));
    }
    
    public ConstructionZone(ConstructionZoneNode zoneNode) {
        super(zoneNode);
        this.id = zoneNode.getId();
        this.accessType = zoneNode.getAccessType();
        this.underlyingNode = zoneNode.getNode();
        this.region = zoneNode.getCuboidRegion();
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public OwnerDomain getOwnerDomain() {
        return new OwnerDomain(underlyingNode);
    }

    @Override
    public AccessType getAccessType() {
        return accessType;
    }

    @Override
    public Node getNode() {
        return underlyingNode;
    }

    @Override
    public Vector getMin() {
        return region.getMinimumPoint();
    }

    @Override
    public Vector getMax() {
        return region.getMaximumPoint();
    }

    @Override
    public CuboidRegion getCuboidRegion() {
        return region;
    }

    
    
    
    
}
