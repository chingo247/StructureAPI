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

import com.chingo247.settlercraft.core.exception.SettlerCraftException;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author Chingo
 */
public class ConfigProvider {

    private HashMap<Flag, Object> defaultFlags;
    private final File configFile;
    private int expirationTime;
    private boolean broadcastExpiration;

    ConfigProvider(File f) throws SettlerCraftException {
        this.configFile = f;
        this.expirationTime = -1;
        this.broadcastExpiration = false;
        reload();
    }
    
    public final void reload() throws SettlerCraftException {
        final FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        this.defaultFlags = getDefaultFlags(config);
        this.expirationTime = config.getInt("protection.expiration-time");
        this.broadcastExpiration = config.getBoolean("protection.broadcast-expiration-to-owners");
    }

    public boolean isBroadcastingExpiration() {
        return broadcastExpiration;
    }
    
    public int getExpirationTime() {
        return expirationTime;
    }
    
    public HashMap<Flag, Object> getDefaultRegionFlags() {
        return defaultFlags;
    }

    private HashMap<Flag, Object> getDefaultFlags(FileConfiguration config) throws SettlerCraftException {
        HashMap<Flag, Object> df = new HashMap<>();
        if (config.getConfigurationSection("structure.default-flags") != null) {
            Map<String, Object> flags = config.getConfigurationSection("structure.default-flags").getValues(false);
            for (Map.Entry<String, Object> entry : flags.entrySet()) {
                Flag<?> foundFlag = DefaultFlag.fuzzyMatchFlag(entry.getKey());
                if (foundFlag == null) {
                    throw new SettlerCraftException("Error in StructureAPI-WorldGuard config.yml: Flag '" + entry.getKey() + "' is not a valid worldguard flag");
                } else {
                    try {
                        df.put(foundFlag, foundFlag.parseInput(WorldGuardPlugin.inst(), Bukkit.getConsoleSender(), String.valueOf(entry.getValue())));
                    } catch (InvalidFlagFormat ex) {
                        Bukkit.getConsoleSender().sendMessage("Error in: " + configFile.getAbsolutePath());
                        Logger.getLogger(ConfigProvider.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            }
        }

        return df;
    }

    public static ConfigProvider load(File f) throws SettlerCraftException {
        return new ConfigProvider(f);
    }

}
