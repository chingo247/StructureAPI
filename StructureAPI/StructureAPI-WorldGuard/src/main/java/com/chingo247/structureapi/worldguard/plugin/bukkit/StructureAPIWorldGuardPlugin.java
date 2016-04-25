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

import com.chingo247.structureapi.worldguard.protection.WorldGuardPlotListener;
import com.chingo247.settlercraft.core.SettlerCraft;
import com.chingo247.settlercraft.core.commands.util.PluginCommandManager;
import com.chingo247.settlercraft.core.exception.SettlerCraftException;
import com.chingo247.settlercraft.core.persistence.neo4j.Neo4jHelper;
import com.chingo247.settlercraft.core.util.JarUtil;
import com.chingo247.structureapi.StructureAPI;
import com.chingo247.structureapi.exeption.StructureAPIException;
import com.chingo247.structureapi.model.RelTypes;
import com.chingo247.structureapi.model.structure.ConstructionStatus;
import com.chingo247.structureapi.model.structure.Structure;
import com.chingo247.structureapi.model.structure.StructureNode;
import com.chingo247.structureapi.worldguard.commands.WGCommands;
import com.chingo247.structureapi.worldguard.plugin.ConfigProvider;
import com.chingo247.structureapi.worldguard.protection.ExpirationTimer;
import com.chingo247.structureapi.worldguard.protection.StructureAPIWorldGuard;
import com.chingo247.structureapi.worldguard.protection.StructureAPIWorldGuardScheduler;
import com.chingo247.structureapi.worldguard.protection.WorldGuardRegionNode;
import com.chingo247.structureapi.worldguard.restriction.WorldGuardRestriction;
import com.chingo247.xplatform.platforms.bukkit.BukkitConsoleSender;
import com.chingo247.xplatform.platforms.bukkit.BukkitPlayer;
import com.google.common.collect.Lists;
import com.sk89q.bukkit.util.CommandsManagerRegistration;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.minecraft.util.commands.CommandUsageException;
import com.sk89q.minecraft.util.commands.MissingNestedCommandException;
import com.sk89q.minecraft.util.commands.WrappedCommandException;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

/**
 *
 * @author Chingo
 */
public class StructureAPIWorldGuardPlugin extends JavaPlugin {

    private static final String RESOURCES_PATH = "com/chingo247/structureapi/worldguard/defaults/";
    private static final String CONFIG_FILE = "config.yml";
    
    private static final Label LABEL = DynamicLabel.label("WORLDGUARD_REGION");
    private static final String REGION_PROPERTY = "region";
    private static final String MSG_PREFIX = "[StructureAPI-WorldGuard]: ";
    private static final long ONE_MINUTE = 1000 * 60;
    
    private PluginCommandManager commands;
    private ExpirationTimer timer;
    private ConfigProvider config;

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("SettlerCraft-Core") == null) {
            System.out.println("[StructureAPI-WorldGuard]: SettlerCraft-Core NOT FOUND!!! Disabling...");
            this.setEnabled(false);
            return;
        }

        if (Bukkit.getPluginManager().getPlugin("SettlerCraft-StructureAPI") == null
                || !Bukkit.getPluginManager().getPlugin("SettlerCraft-StructureAPI").isEnabled()) {
            System.out.println("[StructureAPI-WorldGuard]: SettlerCraft-StructureAPI NOT FOUND!!! Disabling...");
            this.setEnabled(false);
            return;
        }

        // Enable WorldGuard
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") == null) {
            System.out.println("[StructureAPI-WorldGuard]: Couldn't find WorldGuard! Disabling SettlerCraft-WorldGuard");
            this.setEnabled(false);
            return;
        }
        
        try {
            checkConfigUpdate();
        } catch (Exception ex) {
            Logger.getLogger(StructureAPIWorldGuardPlugin.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        try {
            this.config = ConfigProvider.load(new File(getDataFolder(), CONFIG_FILE));
        } catch (SettlerCraftException ex) {
            Logger.getLogger(StructureAPIWorldGuardPlugin.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            System.out.println("Disabling StructureAPI-WorldGuard");
            this.setEnabled(false);
            return;
        } 
        

        ExecutorService executor = StructureAPI.getInstance().getExecutor();
        StructureAPIWorldGuardScheduler.getInstance().setExecutor(executor);
        StructureAPI.getInstance().getAsyncEventBus().register(new WorldGuardPlotListener());
        StructureAPI.getInstance().addRestriction(new WorldGuardRestriction());

        // Set index if not exist
        GraphDatabaseService graph = SettlerCraft.getInstance().getNeo4j();
        try (Transaction tx = graph.beginTx()) {
            Neo4jHelper.createUniqueIndexIfNotExist(graph, LABEL, REGION_PROPERTY);
            tx.success();
        }

        if (StructureAPI.getInstance().getConfig().isProtectStructures()) {
            processStructuresWithoutRegion();
        }
        
        int worldguardExpire = config.getExpirationTime() * 1000 * 60;
        if (worldguardExpire > 0) {
            timer = new ExpirationTimer(graph, ONE_MINUTE, worldguardExpire);
            timer.start();
        }
        
        registerCommands();
    }

    private void registerCommands() {
        this.commands = new PluginCommandManager(StructureAPI.getInstance().getExecutor(), SettlerCraft.getInstance().getPlatform());
        CommandsManagerRegistration cmdRegister = new CommandsManagerRegistration(this, commands);
        cmdRegister.register(WGCommands.class);
    }
    
    private void checkConfigUpdate() throws Exception {
        // Get current Config
        File configFile = new File(getDataFolder(), "config.yml");
        
        // Get temp config
        File temp = new File(getDataFolder(), "temp");
        temp.mkdirs();
        File newConfigFile = new File(temp, "config.yml");
        newConfigFile.delete();
        newConfigFile = new File(temp, "config.yml");
            JarUtil.createDefault(newConfigFile, getFile(), RESOURCES_PATH + "config.yml");

        // Perform update if necessary
        StructureAPIWGConfigUpdater updater = new StructureAPIWGConfigUpdater(configFile, newConfigFile);
        try {
            updater.checkAndUpdate();
        } catch (Exception ex) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + ex.getMessage());
            throw ex;
        }
        // Delete the temp directory
        FileUtils.deleteDirectory(temp);
    }

    private void processStructuresWithoutRegion() {
        final List<Structure> structures = Lists.newArrayList();
        GraphDatabaseService graph = SettlerCraft.getInstance().getNeo4j();
        try (Transaction tx = graph.beginTx()) {
            // (a)-[:PROTECTED_BY]->(w:WORLDGUARD_REGION {region: a.WGRegion})

            String query = "MATCH(s:" + StructureNode.LABEL + ")-[r:" + RelTypes.PROTECTED_BY.name() + "]->(region:WORLDGUARD_REGION) "
                    + "WHERE r IS NULL "
                    + "AND NOT s." + StructureNode.CONSTRUCTION_STATUS_PROPERTY + " = " + ConstructionStatus.REMOVED.getStatusId() + " "
                    + "RETURN s";

            Result r = graph.execute(query);
            while (r.hasNext()) {
                Map<String, Object> map = r.next();
                for (Object o : map.values()) { // all structures
                    Node n = (Node) o;
                    Structure structure = new Structure(n);
                    structures.add(structure);
                }
            }
            tx.success();
        }

        if (!structures.isEmpty()) {
            System.out.println(MSG_PREFIX + "Processing " + structures.size() + " structures without a worldguard region");
            SettlerCraft.getInstance().getExecutor().submit(new Runnable() {

                @Override
                public void run() {
                    
                    Transaction tx = null;
                    GraphDatabaseService graph = SettlerCraft.getInstance().getNeo4j();
                    int count = 0;
                    try {
                        tx = graph.beginTx();
                        for (Iterator<Structure> sit = structures.iterator(); sit.hasNext();) {
                            Structure s = sit.next();
                            StructureAPIWorldGuard.getInstance().protect(s);
                            count++;
                            sit.remove();
                        }
                        tx.success();
                    } catch (Exception ex) {
                        Logger.getLogger(StructureAPIWorldGuardPlugin.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                        if (tx != null) {
                            tx.failure();
                        }
                    } finally {
                        if (tx != null) {
                            tx.close();
                        }
                    }
                    
                    if(count > 1) {
                        System.out.println(MSG_PREFIX + "Protected " + count + " structures with 'WorldGuard'");
                    } else {
                        System.out.println(MSG_PREFIX + "Protected " + count + " structure with 'WorldGuard'");
                    }
                    
                    
                }
            });
        }

    }

    @Override
    public void onDisable() {
        if(timer != null) {
            timer.stop();
        }
    }
    
    

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            if (sender instanceof Player) {
                this.commands.execute(command.getName(), args, new BukkitPlayer((Player) sender), new BukkitPlayer((Player) sender));
            } else {
                this.commands.execute(command.getName(), args, new BukkitConsoleSender(sender), new BukkitConsoleSender(sender));
            }
        } catch (CommandPermissionsException e) {
            sender.sendMessage(ChatColor.RED + "You don't have permission.");
        } catch (MissingNestedCommandException e) {
            sender.sendMessage(ChatColor.RED + e.getUsage());
        } catch (CommandUsageException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
            sender.sendMessage(ChatColor.RED + e.getUsage());
        } catch (WrappedCommandException e) {
            if (e.getCause() instanceof NumberFormatException) {
                sender.sendMessage(ChatColor.RED + "Number expected, string received instead.");
            } else {
                sender.sendMessage(ChatColor.RED + "An error has occurred. See console.");
                e.printStackTrace();
            }
        } catch (com.sk89q.minecraft.util.commands.CommandException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
        }

        return true;
    }

}
