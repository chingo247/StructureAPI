/*
 * Copyright (C) 2015 Chingo
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
package com.chingo247.structurecraft.model.schematic;

import org.neo4j.graphdb.Node;

/**
 *
 * @author Chingo
 */
public interface ISchematicData {
    
    public Node getNode();
    public int getWidth();
    public int getHeight();
    public int getLength();
    public long getXXHash64();
    public String getName();
    public long getLastImport();
    public void setLastImport(long date);
    public void delete();
    
    
}
