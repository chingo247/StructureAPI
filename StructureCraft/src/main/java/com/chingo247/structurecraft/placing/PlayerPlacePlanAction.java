/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.placing;

import com.chingo247.xplatform.core.AItemStack;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.entity.Player;
import java.util.UUID;

/**
 *
 * @author Chingo
 */
public class PlayerPlacePlanAction {
    
    private String world;
    private boolean clickedAir;
    private AItemStack planItem;
    private UUID player;
    private Vector position;

    public PlayerPlacePlanAction(UUID player, String world, Vector position, boolean clickedAir, AItemStack planItem) {
        this.world = world;
        this.clickedAir = clickedAir;
        this.planItem = planItem;
        this.position = position;
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
