/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.towny.plugin;

import com.chingo247.settlercraft.core.SettlerCraft;
import com.chingo247.structureapi.towny.listener.TownListener;
import com.chingo247.structureapi.towny.restriction.TownyRestriction;
import com.chingo247.structureapi.StructureAPI;
import com.chingo247.structureapi.towny.listener.TownyDebugListener;
import com.palmergames.bukkit.config.ConfigNodes;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Coord;
import com.sk89q.worldedit.Vector2D;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Chingo
 */
public class StructureAPITowny extends JavaPlugin {

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("Towny") == null) {
            System.out.println("[SettlerCraft]: Couldn't find towny!");
            System.out.println("[SettlerCraft]: Disabling");
            return;
        }

        if (Bukkit.getPluginManager().getPlugin("SettlerCraft-StructureAPI") == null) {
            System.out.println("[SettlerCraft]: Couldn't find SettlerCraft-StructureAPI!");
            System.out.println("[SettlerCraft]: Disabling");
            
           
            
            
            return;
        }
        
        

        // Towny Restriction
        StructureAPI.getInstance().addRestriction(new TownyRestriction());
        SettlerCraft settlerCraft = SettlerCraft.getInstance();

//        Bukkit.getPluginManager().registerEvents(new TownyDebugListener(), this);
        Bukkit.getPluginManager().registerEvents(new TownListener(settlerCraft.getNeo4j(), settlerCraft.getExecutor()), this);

        if (TownySettings.getBoolean(ConfigNodes.NWS_PLOT_MANAGEMENT_REVERT_ENABLE)) {
            Bukkit.getConsoleSender().sendMessage(new String[]{
                ChatColor.RED + "[SettlerCraft]: **************************[WARNING]****************************************************",
                ChatColor.RED + "[SettlerCraft]: * The option of Towny 'revertOnUnclaim' was enabled, please disable this to prevent undesired behavior ",
                ChatColor.RED + "[SettlerCraft]: * Structures/Schematics would get reverted, but will still exist in SettlerCraft... ",
                ChatColor.RED + "[SettlerCraft]: * So please turn this off! ",
                ChatColor.RED + "[SettlerCraft]: **************************[WARNING]****************************************************"});
        }

    }

    public static Vector2D translate(Coord coord) {
        int blockSize = TownySettings.getTownBlockSize();
        int x = coord.getX() * blockSize;
        int z = coord.getZ() * blockSize;
        return new Vector2D(x, z);
    }
    
    public static Towny getTowny() {
        Towny towny = (Towny) Bukkit.getPluginManager().getPlugin("Towny");
        return  towny;
    }

}
