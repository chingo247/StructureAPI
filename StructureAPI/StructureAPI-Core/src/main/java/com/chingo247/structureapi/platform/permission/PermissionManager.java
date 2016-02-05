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
package com.chingo247.structureapi.platform.permission;

import com.chingo247.settlercraft.core.SettlerCraft;
import com.chingo247.settlercraft.core.platforms.services.IPermissionRegistry;
import com.chingo247.settlercraft.core.platforms.services.permission.Permission;
import com.chingo247.settlercraft.core.platforms.services.permission.PermissionDefault;
import com.chingo247.xplatform.core.IPlayer;
import com.sk89q.worldedit.entity.Player;
import java.util.UUID;
import com.google.common.base.Preconditions;

/**
 *
 * @author Chingo
 */
public class PermissionManager {
    
    private static final String PREFIX = "settlercraft.";
    private static PermissionManager instance;
    private IPermissionRegistry permissionRegistry;

    private PermissionManager() {
    }
    
    public static PermissionManager getInstance() {
        if(instance == null) {
            instance = new PermissionManager();
        }
        return instance;
    }
    

    /**
     * Will register permissions at the IPermissionRegistry
     * @param registry The registry to register the permissions to
     */
    public void registerPermissionRegistry(IPermissionRegistry registry) {
        Preconditions.checkNotNull(registry, "IPermissionRegistry may not be null");
        for(Perms p : Perms.values()) {
            registry.registerPermission(p.permission);
        }
        this.permissionRegistry = registry;
    }

    public IPermissionRegistry getPermissionRegistry() {
        return permissionRegistry;
    }
    
    public enum Perms {
//        CONTENT_GENERATE_PLANS(new Permission(Permissions.CONTENT_GENERATE_PLANS, PermissionDefault.OP, "Allows to generate plans from schematics on the server - is CONSOLE only")),
//        CONTENT_GENERATE_PLANS(new Permission(Permissions.CONTENT_GENERATE_PLANS, PermissionDefault.OP, "Allows to generate plans from schematics on the server - is CONSOLE only")),
        CONTENT_RELOAD_PLANS(new Permission(Permissions.CONTENT_RELOAD_PLANS, PermissionDefault.OP, "Allows a player to reload plans on the server")),
        CONTENT_ROTATE_PLACEMENT(new Permission(Permissions.CONTENT_ROTATE_PLACEMENT, PermissionDefault.OP, "Allows a player to rotate the original schematic for all future placing")),
        
        SETTLER_OPEN_PLAN_MENU(new Permission(Permissions.SETTLER_OPEN_PLANMENU, PermissionDefault.OP, "Allows a player to use the plan menu (contains plans for FREE)")),
        SETTLER_OPEN_SHOP_MENU(new Permission(Permissions.SETTLER_OPEN_PLANSHOP, PermissionDefault.TRUE, "Allows a player to use the plan shop")),
        
        SETTLER_STRUCTURE_CREATE(new Permission(Permissions.STRUCTURE_CREATE, PermissionDefault.OP, "Allows a player to create structures from selections")),
        SETTLER_STRUCTURE_PLACE(new Permission(Permissions.STRUCTURE_PLACE, PermissionDefault.TRUE, "Allows the player to place structures")),
        SETTLER_STRUCTURE_CONSTRUCTION(new Permission(Permissions.STRUCTURE_CONSTRUCTION, PermissionDefault.TRUE, "Allows the player to start construction.demolition of a structure")),
        SETTLER_STRUCTURE_INFO(new Permission(Permissions.STRUCTURE_INFO, PermissionDefault.TRUE, "Allows the player to show information about a structure")),
        SETTLER_STRUCTURE_LIST(new Permission(Permissions.STRUCTURE_LIST, PermissionDefault.TRUE, "Allows the player to show a list structures he or another player owns")),
        SETTLER_STRUCTURE_LOCATION(new Permission(Permissions.STRUCTURE_LOCATION, PermissionDefault.TRUE, "Allows the player to show his relative position to a structure")),
        
        SETTLER_CONSTRUCTION_ZONE_CREATE(new Permission(Permissions.CONSTRUCTIONZONE_CREATE, PermissionDefault.OP, "Allows a player to create construction zones")),
        SETTLER_CONSTRUCTION_ZONE_DELETE(new Permission(Permissions.CONSTRUCTIONZONE_DELETE, PermissionDefault.OP, "Allows a player to delete construction zones"));
        private Permission permission;

        private Perms(Permission permission) {
            this.permission = permission;
        }

        public Permission getPermission() {
            return permission;
        }
        
    }
    
    public boolean isAllowed(Player player, Perms permission) {
        return isAllowed(player.getUniqueId(), permission);
    }
    
    public boolean isAllowed(UUID player, Perms permission) {
        return isAllowed(SettlerCraft.getInstance().getPlatform().getPlayer(player), permission);
    }

    public boolean isAllowed(IPlayer player, Perms permission) {
        if(player == null) return false;
        if(player.isOP() && permission.permission.getDefault() != PermissionDefault.FALSE) return true;
        return player.hasPermission(permission.permission.getName());
    }
    
    
}
