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
package com.chingo247.structureapi.worldguard.plugin;

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
        STRUCTURE_WG_EXPIRE_SINGLE(new Permission(Permissions.EXPIRE_SINGLE, PermissionDefault.OP, "Allows a player to expire a single protected structure")),
        STRUCTURE_WG_EXPIRE_WORLD(new Permission(Permissions.EXPIRE_WORLD, PermissionDefault.OP, "Allows a player to expire all protected structures in a world")),
        STRUCTURE_WG_EXPIRE_ALL(new Permission(Permissions.EXPIRE_ALL, PermissionDefault.OP, "Allows a player to expire all protected structures")),
        STRUCTURE_WG_PROTECT_SINGLE(new Permission(Permissions.PROTECT_SINGLE, PermissionDefault.OP, "Allows a player to protect a single structure")),
        STRUCTURE_WG_PROTECT_WORLD(new Permission(Permissions.PROTECT_WORLD, PermissionDefault.OP, "Allows a player to protect all (unprotected) structure")),
        STRUCTURE_WG_PROTECT_ALL(new Permission(Permissions.PROTECT_ALL, PermissionDefault.OP, "Allows a player to protect all (unprotected) structure"));
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
