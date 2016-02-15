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
package com.chingo247.structureapi.worldguard.plugin.bukkit;

import com.chingo247.structureapi.worldguard.protection.WorldGuardPlotProtector;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Chingo
 */
public class StructureAPIWorldGuard extends JavaPlugin {

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
        if(Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            WorldGuardPlotProtector.getInstance().initialize();
        } else {
            System.out.println("[SettlerCraft-WorldGuard]: Couldn't find WorldGuard! Disabling SettlerCraft-WorldGuard");
            this.setEnabled(false);
        }
    }
    
    
    
    
}
