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
import com.chingo247.settlercraft.core.persistence.neo4j.Neo4jHelper;
import com.chingo247.structureapi.StructureAPI;
import com.chingo247.structureapi.model.RelTypes;
import com.chingo247.structureapi.model.structure.ConstructionStatus;
import com.chingo247.structureapi.model.structure.Structure;
import com.chingo247.structureapi.model.structure.StructureNode;
import com.chingo247.structureapi.worldguard.commands.WGCommands;
import com.chingo247.structureapi.worldguard.protection.StructureRegionRepository;
import com.chingo247.structureapi.worldguard.protection.WorldGuardProtection;
import com.chingo247.structureapi.worldguard.protection.WorldGuardProtectionOld;
import com.chingo247.structureapi.worldguard.protection.WorldGuardScheduler;
import com.chingo247.structureapi.worldguard.restriction.WorldGuardRestriction;
import com.chingo247.xplatform.platforms.bukkit.BukkitConsoleSender;
import com.chingo247.xplatform.platforms.bukkit.BukkitPlayer;
import com.google.common.collect.Lists;
import com.sk89q.bukkit.util.CommandsManagerRegistration;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.minecraft.util.commands.CommandUsageException;
import com.sk89q.minecraft.util.commands.MissingNestedCommandException;
import com.sk89q.minecraft.util.commands.WrappedCommandException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
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
public class StructureAPIWorldGuard extends JavaPlugin {

    private static final Label LABEL = DynamicLabel.label("WORLDGUARD_REGION");
    private static final String REGION_PROPERTY = "region";

    private PluginCommandManager commands;

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("SettlerCraft-Core") == null) {
            System.out.println("[SettlerCraft-WorldGuard]: SettlerCraft-Core NOT FOUND!!! Disabling...");
            this.setEnabled(false);
            return;
        }

        if (Bukkit.getPluginManager().getPlugin("SettlerCraft-StructureAPI") == null
                || !Bukkit.getPluginManager().getPlugin("SettlerCraft-StructureAPI").isEnabled()) {
            System.out.println("[SettlerCraft-WorldGuard]: SettlerCraft-StructureAPI NOT FOUND!!! Disabling...");
            this.setEnabled(false);
            return;
        }

        // Enable WorldGuard
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            WorldGuardProtectionOld.getInstance().initialize();
        } else {
            System.out.println("[SettlerCraft-WorldGuard]: Couldn't find WorldGuard! Disabling SettlerCraft-WorldGuard");
            this.setEnabled(false);
        }

        ExecutorService executor = StructureAPI.getInstance().getExecutor();
        WorldGuardScheduler.getInstance().setExecutor(executor);
        StructureAPI.getInstance().getAsyncEventBus().register(new WorldGuardPlotListener());
        StructureAPI.getInstance().addRestriction(new WorldGuardRestriction());

        // Set index if not exist
        GraphDatabaseService graph = SettlerCraft.getInstance().getNeo4j();
        try (Transaction tx = graph.beginTx()) {
            Neo4jHelper.createUniqueIndexIfNotExist(graph, LABEL, REGION_PROPERTY);
            tx.success();
        }
        
        
        
        processRegionsWithoutStructure();

        registerCommands();
    }

    private void registerCommands() {
        this.commands = new PluginCommandManager(StructureAPI.getInstance().getExecutor(), SettlerCraft.getInstance().getPlatform());
        CommandsManagerRegistration cmdRegister = new CommandsManagerRegistration(this, commands);
        cmdRegister.register(WGCommands.class);
    }

    private void processRegionsWithoutStructure() {
        GraphDatabaseService graph = SettlerCraft.getInstance().getNeo4j();
        StructureRegionRepository regionRepository = new StructureRegionRepository(graph);

        long total = regionRepository.countRegionsWithoutStructure();
        long count = 0;
        
        long batchSize = 1000;
        
        WorldGuardProtection protection = new WorldGuardProtection();
        
        while(count < total) {
            try(Transaction tx = graph.beginTx()) {
            
                List<Node> nodes = regionRepository.findRegionsWithoutStructure(count, count + batchSize);
                
                for (Iterator<Node> iterator = nodes.iterator(); iterator.hasNext();) {
                    Node next = iterator.next();
                    Structure structure = new Structure(next);
                }
                
                
                
                
                count++;
                tx.success();
            }
            
            
        }
        
        
        try (Transaction tx = graph.beginTx()) {
            Result r = graph.execute(query);

            while (r.hasNext()) {
                Node n = (Node) r.next().get("structure");
                Structure structure = new Structure(n);
                removeProtection(structure, true, false);
                System.out.println("[SettlerCraft-WorldGuard]: Removed protection from structure #" + structure.getId() + " because it was removed");
            }

            tx.success();
        }
    }

    private void processStructuresWithoutRegion() {
        final List<Structure> structures = Lists.newArrayList();
        GraphDatabaseService graph = SettlerCraft.getInstance().getNeo4j();
        try (Transaction tx = graph.beginTx()) {
            // (a)-[:PROTECTED_BY]->(w:WORLDGUARD_REGION {region: a.WGRegion})

            String query = "MATCH(s:" + StructureNode.LABEL + ")-[r:" + RelTypes.PROTECTED_BY.name() + "]->(:WORLDGUARD_REGION) "
                    + "WHERE r IS NULL "
                    + "AND NOT s.protectionExpired = true "
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
        }

        SettlerCraft.getInstance().getExecutor().submit(new Runnable() {

            @Override
            public void run() {
                for (Iterator<Structure> sit = structures.iterator(); sit.hasNext();) {
                    Structure s = sit.next();
                    protect(s);
                    System.out.println(MSG_PREFIX + "Protected structure #" + s.getId() + " with 'WorldGuard'");
                    sit.remove();
                }
            }
        });

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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().startsWith("stt:wg") && args.length == 0) {
            return Arrays.asList(new String[]{"expire", "protect", "help"});
        }
        return null;
    }

}
