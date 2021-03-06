/*
 * Copyright (C) 2016 ching
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
package com.chingo247.structureapi.watchers;

import com.chingo247.structureapi.model.structure.Structure;
import com.google.common.collect.Maps;
import java.util.Map;

/**
 *
 * @author chingo
 */
public class Watcher implements IWatcher {
    
    
    protected Map<Long, Structure> registered;
    protected final Object mutex = new Object();
    

    public Watcher() {
        this.registered = Maps.newHashMap();
    }

    @Override
    public void register(Structure structure) {
        synchronized(mutex) {
            this.registered.put(structure.getId(), structure);
        }
    }

    @Override
    public void unregister(Structure structure) {
        synchronized(mutex) {
            this.registered.remove(structure.getId());
        }
    }

   
    
}
