/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.worldguard.commands;

import com.chingo247.settlercraft.core.SettlerCraft;
import com.chingo247.settlercraft.core.commands.util.CommandExtras;
import com.chingo247.structureapi.StructureAPI;
import com.chingo247.structureapi.model.plot.PlotNode;
import com.chingo247.structureapi.model.structure.Structure;
import com.chingo247.structureapi.model.structure.StructureNode;
import com.chingo247.structureapi.worldguard.plugin.PermissionManager;
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

    @CommandExtras(async = true)
    @Command(aliases = {"stt:wg", "structure:worldguard"}, desc = "WorldGuard commands for structures", min = 1, max = 2)
    public static void worldguard(final CommandContext args, final ICommandSender sender) throws Exception {

        String subcommand = args.getString(0);

        switch (subcommand) {
            case "uninstall":
                StructureAPIWorldGuard.getInstance().uninstall();
                break;
            case "expire":
                between(args, 2, 2);
                expire(args, sender);
                break;
            case "protect":
                between(args, 2, 2);
                protect(args, sender);
                break;
            case "help":
                help(args, sender);
                break;
            default:
                throw new CommandUsageException("Invalid argument", "see stt:wg help");
        }
    }

    private static void between(final CommandContext args, int min, int max) throws CommandException {
        if (args.argsLength() < min) {
            throw new CommandException("Too many arguments!");
        }

        if (max > 0 && args.argsLength() > max) {
            throw new CommandException("Too many arguments!");
        }
    }
    

    private static void expire(final CommandContext args, final ICommandSender sender) throws CommandException {
        // TODO Check permission
        // Check expire single | multi
        if (!NumberUtils.isNumber(args.getString(1)))  {   
            World w = Bukkit.getWorld(args.getString(1));
            if(w == null) {
                throw new CommandException("Couldn't find world with name '" + args.getString(1) + "'");
            }
            try {
                StructureAPIWorldGuard.getInstance().expireStructureProtection(w);
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

    private static void protect(final CommandContext args, final ICommandSender sender) throws CommandException {
        if(!NumberUtils.isNumber(args.getString(1))) {
            World w = Bukkit.getWorld(args.getString(1));
            if(w == null) {
                throw new CommandException("Couldn't find world with name '" + args.getString(1) + "'");
            }
            
            try {
                StructureAPIWorldGuard.getInstance().protectStructuresWorld(w);
            } catch (StorageException | StructureAPIWorldGuardException ex) {
                throw new RuntimeException(ex);
            }
            
        } else {
           
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

    private static void help(final CommandContext args, final ICommandSender sender) {
        IColors colors = StructureAPI.getInstance().getPlatform().getChatColors();

        String[] subcommands = new String[]{
            //            colors.blue() + "/stt:wg "+colors.yellow()+"flag <structureId> "+colors.reset()+"- Set a flag for a structure's region, will pass the arguments to worldguard",
            colors.blue() + "/stt:wg " + colors.yellow() + "expire <structureId|ALL> " + colors.reset() + " - expire protection for a structure with given id or all structures when given ALL",
            colors.blue() + "/stt:wg " + colors.yellow() + "protect <structureId|ALL> " + colors.reset() + "- protect structure with given id or all structures when given ALL",
            colors.blue() + "/stt:wg " + colors.yellow() + "help " + colors.reset() + "- display all commands related to worldguard and structureapi"
        };

        String message = "StructureAPI-WorldGuard Commands \n";
        for (String subcommand : subcommands) {
            message += subcommand + "\n";
        }
        sender.sendMessage(message);
    }

 

}
