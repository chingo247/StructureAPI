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
package com.chingo247.settlercraft.worldguard.plugin.bukkit;

import com.chingo247.settlercraft.core.SettlerCraft;
import com.chingo247.settlercraft.worldguard.protecttion.SettlerCraftWGService;
import com.chingo247.structureapi.IStructureAPI;
import com.chingo247.structureapi.StructureAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 *
 * @author Chingo
 */
public class SettlerCraftWorldGuarldPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        if(Bukkit.getPluginManager().getPlugin("SettlerCraft-Core") == null) {
           System.out.println("[SettlerCraft-WorldGuard]: SettlerCraft-Core NOT FOUND!!! Disabling...");
            this.setEnabled(false);
            return; 
        }
        
        if(Bukkit.getPluginManager().getPlugin("SettlerCraft-StructureAPI") == null) {
           System.out.println("[SettlerCraft-WorldGuard]: SettlerCraft-StructureAPI NOT FOUND!!! Disabling...");
            this.setEnabled(false);
            return; 
        }
        
        
        // Enable WorldGuard
        GraphDatabaseService graph = SettlerCraft.getInstance().getNeo4j();
        IStructureAPI structureAPI = StructureAPI.getInstance();
        
        
        
        if(Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            SettlerCraftWGService worldGuardHelper = new SettlerCraftWGService(graph, structureAPI);
            worldGuardHelper.initialize();
        } else {
            System.out.println("[SettlerCraft-WorldGuard]: Couldn't find WorldGuard! Disabling SettlerCraft-WorldGuard");
            this.setEnabled(false);
        }
    }
    
    
    
    
}
