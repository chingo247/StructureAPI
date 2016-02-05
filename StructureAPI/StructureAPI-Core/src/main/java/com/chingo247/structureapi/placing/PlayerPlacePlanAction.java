/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.placing;

import com.chingo247.xplatform.core.AItemStack;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.Vector;
import java.util.UUID;

/**
 *
 * @author Chingo
 */
public class PlayerPlacePlanAction {
    
    private final String world;
    private final boolean clickedAir;
    private final AItemStack planItem;
    private final UUID player;
    private final Vector position;

    public PlayerPlacePlanAction(UUID player, String world, Vector position, boolean clickedAir, AItemStack planItem) {
        Preconditions.checkNotNull(player, "player was null");
        Preconditions.checkNotNull(world, "world was null");
        Preconditions.checkNotNull(planItem, "planItem was null");
        
        this.world = world;
        this.clickedAir = clickedAir;
        this.planItem = planItem;
        this.position = position;
        this.player = player;
        
        assert this.player.equals(player) : "player not set";
        assert this.world.equals(world) : "world not set";
        assert this.clickedAir == clickedAir;
        assert this.planItem != null : "planItem not set";
    }

    public Vector getPosition() {
        return position;
    }

    public AItemStack getPlanItem() {
        return planItem;
    }

    public String getWorld() {
        return world;
    }

    public boolean isClickedAir() {
        return clickedAir;
    }

    public UUID getPlayer() {
        return player;
    }
    
    
}
