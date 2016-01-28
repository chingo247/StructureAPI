/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.bukkit.listener;

import com.chingo247.structurecraft.watchers.ICancellableBlockEvent;
import com.chingo247.structurecraft.watchers.PhysicsWatch;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;

/**
 *
 * @author ching
 */
public class PhysicsListener implements Listener {
    
    private PhysicsWatch watch;

    public PhysicsListener(PhysicsWatch watch) {
        this.watch = watch;
    }
    
    @EventHandler
    public void onBlockLeafDecay(LeavesDecayEvent event) {
        this.watch.handleBlockLeafDecay(new CancellableBlockEvent(event));
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        this.watch.handleBlockBreak(new CancellablePlayerBlockEvent(event.getPlayer() != null, event));
    }
    
    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        this.watch.handleBlockIgnite(new CancellablePlayerBlockEvent(event.getPlayer() != null, event));
    }
    
    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        this.watch.handleBlockIgnite(new CancellableBlockEvent(event));
    }
    
    @EventHandler
    public void onBlockFlow(BlockFromToEvent event)  {
        this.watch.handleBlockFlow(new CancellableBlockEvent(event));
    }
    
    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        this.watch.handleBlockPhysics(new CancellableBlockEvent(event));
    }
   
    
    @EventHandler
    public void onBlockSpread(BlockSpreadEvent event) {
        this.watch.handleBlockSpread(new CancellableBlockEvent(event));
    }
    
    private class CancellableBlockEvent implements ICancellableBlockEvent {
        
        private BlockEvent event;

        public CancellableBlockEvent(BlockEvent event) {
            Preconditions.checkArgument((event instanceof Cancellable), "Event must be instance of " + (Cancellable.class.getName()));
            this.event = event;
        }

        @Override
        public String getWorld() {
            return event.getBlock().getWorld().getName();
        }
        
        

        @Override
        public BaseBlock getBlock() {
            Block b = event.getBlock();
            return new BaseBlock(b.getType().getId(), b.getData());
        }

        @Override
        public Vector getPosition() {
            Location l = event.getBlock().getLocation();
            return new BlockVector(l.getBlockX(), l.getBlockY(), l.getBlockZ());
        }

        @Override
        public void cancel() {
            ((Cancellable) event).setCancelled(true);
        }
        
    }
     
    private class CancellablePlayerBlockEvent extends CancellableBlockEvent {
        
        private boolean causedByPlayer;
        
        public CancellablePlayerBlockEvent(boolean causedByPlayer, BlockEvent event) {
            super(event);
            
            this.causedByPlayer = causedByPlayer;
        }

        public boolean isCausedByPlayer() {
            return causedByPlayer;
        }
        
        
        
        
        
    }
 }
