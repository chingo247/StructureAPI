/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.worldguard.commands;

/**
 *
 * @author Chingo
 */
public class WGCommands {

//    @CommandExtras(async = true)
//    @Command(aliases = {"stt:wg", "structure:worldguard"}, desc = "WorldGuard commands for structures", min = 1, max = 2)
//    public static void worldguard(final CommandContext args, final ICommandSender sender) throws Exception {
//
//        String subcommand = args.getString(0);
//
//        switch (subcommand) {
////            case "flag":
////                between(args, 2, -1);
////                flag(args, sender);
//            case "expire":
//                between(args, 2, 2);
//                expire(args, sender);
//                break;
//            case "protect":
//                between(args, 2, 2);
//                protect(args, sender);
//                break;
//            case "help":
//                help(args, sender);
//                break;
//            default:
//                throw new CommandUsageException("Invalid argument", "see stt:wg help");
//        }
//
//    }
//
//    private static void between(final CommandContext args, int min, int max) throws CommandException {
//        if (args.argsLength() < min) {
//            throw new CommandException("Too many arguments!");
//        }
//
//        if (max > 0 && args.argsLength() > max) {
//            throw new CommandException("Too many arguments!");
//        }
//    }
//
//    private static void expire(final CommandContext args, final ICommandSender sender) throws CommandException {
//        // TODO Check permission
//        // Check expire single | multi
//        if (args.getString(1).equalsIgnoreCase("ALL")) {
//            checkAllowed(sender, PermissionManager.Perms.STRUCTURE_WG_EXPIRE_ALL);
//        } else {
//            String arg = args.getString(1);
//            Long id;
//            try {
//                id = Long.parseLong(arg);
//            } catch (NumberFormatException nfe) {
//                throw new CommandException("Expected a number but got '" + arg + "'");
//            }
//            checkAllowed(sender, PermissionManager.Perms.STRUCTURE_WG_EXPIRE_SINGLE);
//        }
//
//        
//    }
//
//    private static void protect(final CommandContext args, final ICommandSender sender) {
//        // TODO Check permission
//        // Check expire single | multi
//    }
//
//    private static void checkAllowed(ICommandSender sender, PermissionManager.Perms perm) throws CommandException {
//        if (sender instanceof IPlayer) {
//            if (!PermissionManager.getInstance().isAllowed((IPlayer) sender, perm)) {
//                throw new CommandException("You don't have permission to do this!");
//            }
//        }
//    }
//
//    private static void help(final CommandContext args, final ICommandSender sender) {
//        IColors colors = StructureAPI.getInstance().getPlatform().getChatColors();
//
//        String[] subcommands = new String[]{
//            //            colors.blue() + "/stt:wg "+colors.yellow()+"flag <structureId> "+colors.reset()+"- Set a flag for a structure's region, will pass the arguments to worldguard",
//            colors.blue() + "/stt:wg " + colors.yellow() + "expire <structureId|ALL> " + colors.reset() + " - expire protection for a structure with given id or all structures when given ALL",
//            colors.blue() + "/stt:wg " + colors.yellow() + "protect <structureId|ALL> " + colors.reset() + "- protect structure with given id or all structures when given ALL",
//            colors.blue() + "/stt:wg " + colors.yellow() + "help " + colors.reset() + "- display all commands related to worldguard and structureapi"
//        };
//
//        String message = "StructureAPI-WorldGuard Commands \n";
//        for (String subcommand : subcommands) {
//            message += subcommand + "\n";
//        }
//        sender.sendMessage(message);
//    }
//
//    private static void flag(CommandContext args, ICommandSender sender) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }

}
