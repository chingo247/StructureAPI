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

import com.chingo247.structureapi.model.structure.IStructure;

/**
 *
 * @author ching
 */
public class PhysicsWatch {

    private final BlockWatcher blockleafDecayWatcher = new BlockWatcher();
    private final BlockWatcher blockBreakWatcher = new BlockWatcher();
    private final BlockWatcher blockPhysicsWatcher = new BlockWatcher();
    private final BlockWatcher blockSpreadWatcher = new BlockWatcher();
    private final BlockWatcher blockIgniteWatcher = new BlockWatcher();
    private final BlockWatcher blockFlowWatcher = new BlockWatcher();
    
    private final BlockWatcher[] watchers = new BlockWatcher[] {
        blockleafDecayWatcher,
        blockBreakWatcher,
        blockPhysicsWatcher,
        blockSpreadWatcher,
        blockIgniteWatcher,
        blockFlowWatcher
    };

    public PhysicsWatch() {
    }
    
    public void register(IStructure structure) {
//        System.out.println("Register: " + structure.getId() + " " + structure.getName());
        for(BlockWatcher watcher : watchers) {
            watcher.register(structure);
        }
    }
    
    public void unregister(IStructure structure) {
//         System.out.println("Unregister: " + structure.getId() + " " + structure.getName());
        for(BlockWatcher watcher : watchers) {
            watcher.unregister(structure);
        }
    }
    
    public void handleBlockFlow(ICancellableBlockEvent blockEvent) {
        if (blockFlowWatcher.watch(blockEvent)) {
            blockEvent.cancel();
        }
    }
    
    public void handleBlockLeafDecay(ICancellableBlockEvent blockEvent) {
        if(blockleafDecayWatcher.watch(blockEvent)) {
            blockEvent.cancel();
        }
    }
    
    public void handleBlockBreak(ICancellableBlockEvent blockEvent) {
        if (blockBreakWatcher.watch(blockEvent)) {
            blockEvent.cancel();
        }
    }
    
    public void handleBlockPhysics(ICancellableBlockEvent blockEvent) {
        if(blockPhysicsWatcher.watch(blockEvent)) {
            blockEvent.cancel();
        } 
    }
    
    public void handleBlockSpread(ICancellableBlockEvent blockEvent) {
        if (blockSpreadWatcher.watch(blockEvent)) {
            blockEvent.cancel();
        }
    }
    
    public void handleBlockIgnite(ICancellableBlockEvent blockEvent) {
        if (blockIgniteWatcher.watch(blockEvent)) {
            blockEvent.cancel();
        }
    }

    private class BlockWatcher extends Watcher {

        public boolean watch(ICancellableBlockEvent blockEvent) {
            synchronized (mutex) {
                for (IStructure structure : registered.values()) {
                    if (structure.getWorldName().equals(blockEvent.getWorld()) && structure.getCuboidRegion().contains(blockEvent.getPosition())) {
                        return true;
                    }
                }
            }
            return false;
        }

    }

}
