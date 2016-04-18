/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.worldguard.commands;

import com.chingo247.settlercraft.core.SettlerCraft;
import com.chingo247.settlercraft.core.commands.util.CommandExtras;
import com.chingo247.settlercraft.core.commands.util.CommandSenderType;
import com.chingo247.structureapi.StructureAPI;
import com.chingo247.structureapi.model.plot.PlotNode;
import com.chingo247.structureapi.model.structure.Structure;
import com.chingo247.structureapi.model.structure.StructureNode;
import com.chingo247.structureapi.worldguard.plugin.PermissionManager;
import com.chingo247.structureapi.worldguard.plugin.Permissions;
import com.chingo247.structureapi.worldguard.protection.StructureRegionRepository;
import com.chingo247.structureapi.worldguard.protection.StructureAPIWorldGuard;
import com.chingo247.structureapi.worldguard.protection.StructureAPIWorldGuardException;
import com.chingo247.structureapi.worldguard.protection.StructureAPIWorldGuardScheduler;
import com.chingo247.xplatform.core.IColors;
import com.chingo247.xplatform.core.ICommandSender;
import com.chingo247.xplatform.core.IPlayer;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.CommandUsageException;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

/**
 *
 * @author Chingo
 */
public class WGCommands {


    @CommandExtras(async = true, senderType = CommandSenderType.CONSOLE)
    @CommandPermissions(Permissions.EXPIRE_ALL)
    @Command(aliases = "sttwg:uninstall", desc = "Removes worldguard protection from all structures")
    public static void uninstall(final CommandContext args, ICommandSender sender) throws CommandException {
        StructureAPIWorldGuard.getInstance().uninstall(sender);
    }
    
    @CommandExtras(async = true, senderType = CommandSenderType.CONSOLE)
    @CommandPermissions(Permissions.PROTECT_ALL)
    @Command(aliases = "sttwg:install", desc = "Adds worldguard protection to all structures")
    public static void install(final CommandContext args, ICommandSender sender) throws CommandException {
        StructureAPIWorldGuard.getInstance().install(sender);
    }
    
    @CommandExtras(async = true)
    @Command(aliases = "sttwg:expire", usage = "sttwg:expire [world|structureid]" ,desc = "Expires protection for a structure or all structures of a world", min = 1, max = 1)
    public static void expire(final CommandContext args, ICommandSender sender) throws CommandException {
        if (!NumberUtils.isNumber(args.getString(0)))  {
            checkAllowed(sender, PermissionManager.Perms.STRUCTURE_WG_EXPIRE_WORLD);
            World w = Bukkit.getWorld(args.getString(1));
            if(w == null) {
                throw new CommandException("Couldn't find world with name '" + args.getString(1) + "'");
            }
            try {
                StructureAPIWorldGuard.getInstance().expireStructureProtection(w);
                sender.sendMessage("Expired protection for structures in world '" + w.getName() + "'");
            } catch (StorageException | StructureAPIWorldGuardException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            String arg = args.getString(1);
            Long id = getLong(arg);
            checkAllowed(sender, PermissionManager.Perms.STRUCTURE_WG_EXPIRE_SINGLE);
            expireSingle(id, sender);
        }
    }
    
    private static void expireSingle(long structureId, ICommandSender sender) throws CommandException {
        GraphDatabaseService graph = SettlerCraft.getInstance().getNeo4j();
        StructureRegionRepository repository = new StructureRegionRepository(graph);
        try (Transaction tx = graph.beginTx()) {
            StructureNode node = repository.findById(structureId);
            
            if(node != null) {
                try {
                    StructureAPIWorldGuard.getInstance().expire(new Structure(node));
                    sender.sendMessage("Expired protection for structure #" + structureId);
                } catch (StructureAPIWorldGuardException ex) {
                    throw new CommandException(ex);
                }
                tx.success();
            } else {
                tx.failure();
                throw new CommandException("Structure #" + structureId + " does not have a region");
            }
        } catch (StorageException ex) {
            sender.sendMessage("An error occured");
            Logger.getLogger(WGCommands.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    @CommandExtras(async = true)
    @Command(aliases = "sttwg:protect", usage = "sttwg:protect [world|structureid]" ,desc = "Adds protection to a structure or all structures of a world", min = 1, max = 1)
    public static void protect(final CommandContext args, ICommandSender sender) throws CommandException {
        if(!NumberUtils.isNumber(args.getString(1))) {
            checkAllowed(sender, PermissionManager.Perms.STRUCTURE_WG_PROTECT_WORLD);
            World w = Bukkit.getWorld(args.getString(1));
            if(w == null) {
                throw new CommandException("Couldn't find world with name '" + args.getString(1) + "'");
            }
            
            try {
                StructureAPIWorldGuard.getInstance().protectStructuresWorld(w);
                sender.sendMessage("Protected structures for world '" + w.getName() + "'");
            } catch (StorageException | StructureAPIWorldGuardException ex) {
                throw new RuntimeException(ex);
            }
           
        } else {
            checkAllowed(sender, PermissionManager.Perms.STRUCTURE_WG_PROTECT_SINGLE);
            long id = getLong(args.getString(1));
            GraphDatabaseService graph = SettlerCraft.getInstance().getNeo4j();
            
            try (Transaction tx = graph.beginTx()) {
                StructureRegionRepository regionRepository = new StructureRegionRepository(graph);
                StructureNode structureNode =  regionRepository.findById(id);
                if(structureNode == null) {
                    tx.success();
                    throw new CommandException("Couldn't find strucuture with id '" + id + "'");
                }
                
                try {
                    StructureAPIWorldGuard.getInstance().protect(new Structure(structureNode));
                    sender.sendMessage("Protected structure #" + id + " with worldguard");
                } catch (StorageException ex) {
                    tx.failure();
                    Logger.getLogger(WGCommands.class.getName()).log(Level.SEVERE, null, ex);
                    throw new CommandException("An error occured.. see console..");
                } catch (StructureAPIWorldGuardException ex) {
                    throw new CommandException(ex);
                }
                
                tx.success();
            }
        }
    }

    private static void checkAllowed(ICommandSender sender, PermissionManager.Perms perm) throws CommandException {
        if (sender instanceof IPlayer) {
            if (!PermissionManager.getInstance().isAllowed((IPlayer) sender, perm)) {
                throw new CommandException("You don't have permission to do this!");
            }
        }
    }
    
    private static long getLong(String arg) throws CommandException {
        try {
            return Long.parseLong(arg);
        } catch (NumberFormatException nfe) {
            throw new CommandException("Expected a number but got '" + arg + "'");
        }
    }


 

}
