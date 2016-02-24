/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.worldguard.commands;

import com.chingo247.settlercraft.core.commands.util.CommandExtras;
import com.chingo247.structureapi.worldguard.protection.WorldGuardProtection;
import com.chingo247.xplatform.core.ICommandSender;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandUsageException;

/**
 *
 * @author Chingo
 */
public class WGCommands {

//    @CommandPermissions(Permissions.STRUCTURE_INFO)
    private static final String[] subcommands = new String[]{
        "/stt:wg flag <structureId> - Set a flag for a structure's region, will pass the arguments to worldguard",
        "/stt:wg expire <structureId|ALL> - expire protection for a structure with given id or all structures when given ALL",
        "/stt:wg protect <structureId|ALL> - protect structure with given id or all structures when given ALL",
        "/stt:wg help - display all commands related to worldguard and structureapi"
    };

    @CommandExtras(async = true)
    @Command(aliases = {"stt:wg", "structure:worldguard"}, desc = "WorldGuard commands for structures", min = 2, max = 2)
    public static void worldguard(final CommandContext args, final ICommandSender sender) throws Exception {

        String subcommand = args.getString(0);

        switch (subcommand) {
            case "flag":
                between(args, 2, -1);
                flag(args, sender);
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

    private static void expire(final CommandContext args, final ICommandSender sender) {
        // TODO Check permission
        // Check expire single | multi
        WorldGuardProtection.getInstance().removeProtection(null, false, true);
    }

    private static void protect(final CommandContext args, final ICommandSender sender) {
        // TODO Check permission
        // Check expire single | multi
    }

    private static void help(final CommandContext args, final ICommandSender sender) {
        String message = "StructureAPI-WorldGuard Commands \n";
        for (String subcommand : subcommands) {
            message += subcommand + "\n";
        }
        sender.sendMessage(message);
    }

    private static void flag(CommandContext args, ICommandSender sender) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
