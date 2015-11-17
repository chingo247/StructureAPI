/*
 * Copyright (C) 2015 ching
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.chingo247.structurecraft.model.zone;

import com.chingo247.structurecraft.model.AccessType;
import com.chingo247.settlercraft.core.persistence.neo4j.NodeHelper;
import com.chingo247.structurecraft.model.owner.OwnerDomain;
import com.chingo247.structurecraft.model.plot.PlotNode;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

/**
 *
 * @author ching
 */
public class ConstructionZoneNode extends PlotNode implements IConstructionZone {
    
    public static final String LABEL = "CONSTRUCTION_ZONE";
    public static final String ACCESS_TYPE_PROPERTY = "ACCESS_TYPE"; // 
    public static final String ID_PROPERTY = "ID";
    private OwnerDomain ownerDomain;
    
    public static Label label() {
        return DynamicLabel.label(LABEL);
    }

    public ConstructionZoneNode(Node node) {
        super(node);
    }

    @Override
    public OwnerDomain getOwnerDomain() {
        return ownerDomain;
    }
    
    @Override
    public Long getId() {
        return NodeHelper.getLong(underlyingNode, ID_PROPERTY, null);
    }
    
    @Override
    public AccessType getAccessType() {
        int type = NodeHelper.getInt(underlyingNode, ACCESS_TYPE_PROPERTY, -1);
        if(type == -1) {
            return null;
        } else {
            return AccessType.getAccessType(type);
        }
    }
    
    public void setAccessType(AccessType accessType) {
        underlyingNode.setProperty(ACCESS_TYPE_PROPERTY, accessType.getTypeId());
    }

    
}
