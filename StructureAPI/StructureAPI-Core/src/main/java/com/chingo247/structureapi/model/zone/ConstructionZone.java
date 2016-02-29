/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.model.zone;

import com.chingo247.structureapi.model.AccessType;
import com.chingo247.structureapi.model.owner.OwnerDomainNode;
import com.chingo247.structureapi.model.plot.Plot;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.neo4j.graphdb.Node;

/**
 *
 * @author Chingo
 */
public class ConstructionZone extends Plot {
    
    private Long id;
    private AccessType accessType;
    private Node underlyingNode;
    private CuboidRegion region;
    private String wgRegion;
    
    public ConstructionZone(Node node) {
        this(new ConstructionZoneNode(node));
    }
    
    public ConstructionZone(ConstructionZoneNode zoneNode) {
        super(zoneNode);
        this.id = zoneNode.getId();
        this.accessType = zoneNode.getAccessType();
        this.underlyingNode = zoneNode.getNode();
        this.region = zoneNode.getCuboidRegion();
        this.wgRegion = zoneNode.getWorldGuardRegion();
    }

    public Long getId() {
        return id;
    }

    public OwnerDomainNode getOwnerDomain() {
        return new OwnerDomainNode(underlyingNode);
    }

    public AccessType getAccessType() {
        return accessType;
    }

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

    public String getWorldGuardRegion() {
        return wgRegion;
    }

    public boolean hasWorldGuardRegion() {
        return wgRegion != null;
    }

    
    
    
    
}
