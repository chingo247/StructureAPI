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
package com.chingo247.structureapi.selection;

import com.sk89q.worldedit.Vector;
import java.util.Objects;
import java.util.UUID;

/**
 *
 * @author Chingo
 */
public class Selection {
    
    protected final  UUID player;
    protected final  Vector start;
    protected final  Vector end;

    public Selection(UUID player, Vector start, Vector end) {
        this.player = player;
        this.start = start;
        this.end = end;
    }

    public UUID getPlayer() {
        return player;
    }

    public Vector getStart() {
        return start;
    }

    public Vector getEnd() {
        return end;
    }

    

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + Objects.hashCode(this.start);
        hash = 47 * hash + Objects.hashCode(this.end);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Selection other = (Selection) obj;
        if (!Objects.equals(this.start, other.start)) {
            return false;
        }
        if (!Objects.equals(this.end, other.end)) {
            return false;
        }
        return true;
    }
    
    
    
}
