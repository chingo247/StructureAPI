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
package com.chingo247.structurecraft.commands;

import com.chingo247.menuapi.menu.util.ShopUtil;
import com.chingo247.settlercraft.core.commands.util.CommandExtras;
import com.chingo247.settlercraft.core.SettlerCraft;
import com.chingo247.settlercraft.core.commands.util.CommandSenderType;
import com.chingo247.settlercraft.core.model.settler.BaseSettlerNode;
import com.chingo247.structurecraft.event.structure.owner.StructureAddOwnerEvent;
import com.chingo247.structurecraft.event.structure.owner.StructureRemoveOwnerEvent;
import com.chingo247.structurecraft.model.owner.IOwnership;
import com.chingo247.structurecraft.model.owner.OwnerDomainNode;
import com.chingo247.structurecraft.model.owner.OwnerType;
import com.chingo247.structurecraft.model.settler.ISettlerRepository;
import com.chingo247.structurecraft.model.settler.SettlerRepositiory;
import com.chingo247.structurecraft.model.structure.ConstructionStatus;
import com.chingo247.structurecraft.model.structure.IStructureRepository;
import com.chingo247.structurecraft.model.structure.Structure;
import com.chingo247.structurecraft.model.structure.StructureNode;
import com.chingo247.structurecraft.model.structure.StructureRepository;
import com.chingo247.structurecraft.IStructureAPI;
import com.chingo247.structurecraft.construction.IContract;
import com.chingo247.structurecraft.construction.contract.BuildContract;
import com.chingo247.structurecraft.construction.contract.RollbackContract;
import com.chingo247.structurecraft.construction.contract.safe.SafeContract;
import com.chingo247.structurecraft.model.settler.SettlerNode;
import com.chingo247.structurecraft.platform.permission.Permissions;
import com.chingo247.xplatform.core.IColors;
import com.chingo247.xplatform.core.ICommandSender;
import com.chingo247.xplatform.core.ILocation;
import com.chingo247.xplatform.core.IPlayer;
import com.google.common.collect.Sets;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.CommandUsageException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.World;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.math.NumberUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

/**
 *
 * @author Chingo
 */
public class StructureCommands {

    private static final UUID CONSOLE = UUID.randomUUID();
    private static final Logger LOG = Logger.getLogger(StructureCommands.class.getSimpleName());
    private static final int MAX_LINES = 10;

    private static final Comparator<String> ALPHABETICAL_ORDER = new Comparator<String>() {

        @Override
        public int compare(String o1, String o2) {
            return o1.toLowerCase().compareTo(o2.toLowerCase());
        }
    };

    private static UUID getUUID(ICommandSender sender) {
        if (sender instanceof IPlayer) {
            return ((IPlayer) sender).getUniqueId();
        }
        return CONSOLE;
    }

    private static boolean isConsole(ICommandSender sender) {
        return !(isPlayer(sender));
    }

    private static boolean isPlayer(ICommandSender sender) {
        return (sender instanceof IPlayer);
    }

    private static boolean isOP(ICommandSender sender) {
        return isConsole(sender) || ((IPlayer) sender).isOP();
    }

    private static boolean isUniquePlayerName(String playerName, IStructureAPI structureAPI) {
        int count = 0;
        for (IPlayer player : structureAPI.getPlatform().getServer().getPlayers()) {
            if (player.getName().equals(playerName)) {
                count++;
                if (count > 1) {
                    return false;
                }
            }
        }
        return true;
    }

    private static String getInfo(StructureNode structure, IColors colors) {
        TreeSet<String> owners = Sets.newTreeSet(ALPHABETICAL_ORDER);

        List<SettlerNode> mastersNode = structure.getOwnerDomain().getOwners(OwnerType.MASTER);
        for (SettlerNode master : mastersNode) {
            owners.add(master.getName());
        }

        String ownershipString = "";
        int size = owners.size();
        int count = 0;

        for (String ownership : owners) {
            ownershipString += ownership;
            count++;
            if (count != size) {
                ownershipString += ", ";
            }

        }

        String line = "#" + colors.gold() + structure.getId() + " " + colors.blue() + structure.getName() + "\n"
                + colors.reset() + "World: " + colors.yellow() + structure.getWorldName() + "\n";

        Vector position = structure.getOrigin();
        line += colors.reset() + "Location: " + colors.yellow() + "X: " + colors.reset() + position.getX()
                + " " + colors.yellow() + "Y: " + colors.reset() + position.getY()
                + " " + colors.yellow() + "Z: " + colors.reset() + position.getZ() + "\n";
        
        CuboidRegion region = structure.getCuboidRegion();
        
        line += colors.reset() + "Width: " + colors.yellow() + region.getWidth() + colors.reset() 
                + " Height: " + colors.yellow() + region.getHeight() + colors.reset()
                + " Length: " + colors.yellow() + region.getLength() + colors.reset() + "\n";

        line += colors.reset() + "Status: " + colors.reset() + getStatusString(structure, colors) + "\n";

        if (structure.getPrice() > 0) {
            line += colors.reset() + "Value: " + colors.yellow() + structure.getPrice() + "\n";
        }

        if (!owners.isEmpty()) {
            if (owners.size() == 1) {
                line += colors.reset() + "Owners(MASTER): " + colors.yellow() + ownershipString + "\n";
            } else {
                line += colors.reset() + "Owners(MASTER): \n" + colors.yellow() + ownershipString + "\n";
            }
        }

        if (structure.getNode().hasProperty("WGRegion")) {
            line += colors.reset() + "WorldGuard-Region: " + colors.yellow() + structure.getNode().getProperty("WGRegion");
        }
        return line;

    }

    private static String getStatusString(StructureNode structure, IColors colors) {
        String statusString;
        ConstructionStatus status = structure.getStatus();
        switch (status) {
            case BUILDING:
                statusString = colors.yellow() + "BUILDING";
                break;
            case DEMOLISHING:
                statusString = colors.yellow() + "DEMOLISHING";
                break;
            case COMPLETED:
                statusString = colors.green() + "COMPLETE";
                break;
            case ON_HOLD:
                statusString = colors.red() + "ON HOLD";
                break;
            case QUEUED:
                statusString = colors.yellow() + "QUEUED";
                break;
            case REMOVED:
                statusString = colors.red() + "REMOVED";
                break;
            case STOPPED:
                statusString = colors.red() + "STOPPED";
                break;
            default:
                statusString = status.name();
        }
        return statusString;
    }

    @CommandPermissions(Permissions.STRUCTURE_INFO)
    @CommandExtras(async = true)
    @Command(aliases = {"structure:info", "stt:info"}, desc = "Display info about the structure you are in or with the given id", max = 1)
    public static void info(final CommandContext args, final ICommandSender sender, IStructureAPI structureAPI) throws Exception {
        final GraphDatabaseService graph = SettlerCraft.getInstance().getNeo4j();
        final IColors color = structureAPI.getPlatform().getChatColors();
        final StructureRepository structureRepository = new StructureRepository(graph);
        final StructureNode structure;

        if (args.argsLength() == 1) {
            // Find by ID
            Long id;
            try {
                id = Long.parseLong(args.getString(0));
            } catch (NumberFormatException nfe) {
                throw new CommandException("Expected a number but got '" + args.getString(0) + "'");
            }
            long start = System.currentTimeMillis();

            try (Transaction tx = graph.beginTx()) {
                structure = structureRepository.findById(id);

                if (structure == null) {
                    tx.success();
                    throw new CommandException("Couldn't find structure for id #" + id);
                }
                String info = getInfo(structure, color);
                sender.sendMessage(info);
                tx.success();
            }

            LOG.log(Level.INFO, "info in {0} ms", (System.currentTimeMillis() - start));
        } else if (sender instanceof IPlayer) {
            // Find by position
            IPlayer ply = (IPlayer) sender;
            ILocation loc = ply.getLocation();
            Vector pos = new Vector(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            long start = System.currentTimeMillis();

            try (Transaction tx = graph.beginTx()) {
                structure = structureRepository.findStructureOnPosition(ply.getWorld().getUUID(), pos);

                if (structure == null) {
                    tx.success();
                    throw new CommandException(" Not within a structure...");
                }

                String info = getInfo(structure, color);
                sender.sendMessage(info);
                tx.success();
            }
            LOG.log(Level.INFO, "info in {0} ms", (System.currentTimeMillis() - start));
        } else {
            throw new CommandException("Too few arguments \n" + "/structure:info [id]");
        }
    }

    @CommandPermissions(Permissions.STRUCTURE_CONSTRUCTION)
    @CommandExtras(async = true)
    @Command(aliases = {"structure:build", "stt:build"}, desc = "Builds a structure", min = 1, max = 1, flags = "f")
    public static void build(final CommandContext args, final ICommandSender sender, final IStructureAPI structureAPI) throws Exception {
        final UUID uuid = getUUID(sender);
        final GraphDatabaseService graph = SettlerCraft.getInstance().getNeo4j();
        final StructureRepository structureRepository = new StructureRepository(graph);

        final Structure structure;
        String structureIdArg = args.getString(0);
        if (!NumberUtils.isNumber(structureIdArg)) {
            throw new CommandException("Expected a number but got '" + structureIdArg + "' \n" + "/structure:build [id]");
        }
        long id = Long.parseLong(structureIdArg);
        long start = System.currentTimeMillis();
        try (Transaction tx = graph.beginTx()) {
            StructureNode sn = structureRepository.findById(id);

            if (sn == null) {
                tx.success();
                throw new CommandException("Couldn't find a structure for #" + structureIdArg);
            }

            if (isPlayer(sender) && !isOP(sender) && sender instanceof IPlayer && !sn.getOwnerDomain().isOwnerOfType(uuid, OwnerType.MASTER)) {
                tx.success();
                throw new CommandException("You are not the 'MASTER' owner of this structure...");
            }
            structure = new Structure(sn);
            tx.success();
        }
        LOG.log(Level.INFO, "build in {0} ms", (System.currentTimeMillis() - start));

        String force = args.hasFlag('f') ? args.getFlag('f') : null;
        final boolean useForce = force != null && (force.equals("t") || force.equals("true"));
        
        BuildContract buildContract = new BuildContract();
        SafeContract safeContract = new SafeContract(buildContract);
        safeContract.setRecursive(true)
                .setRestrictive(true)
                .setForced(useForce)
                .setPlayer(uuid);
        structureAPI.getConstructionExecutor().submit(structure, safeContract);
    }
    
    @CommandPermissions(Permissions.STRUCTURE_CONSTRUCTION)
    @CommandExtras(async = true)
    @Command(aliases = {"structure:rollback", "stt:rollback"}, desc = "Restores the area back to before the structure was placed", min = 1, max = 1, flags = "f")
    public static void rollback(final CommandContext args, final ICommandSender sender, final IStructureAPI structureAPI) throws Exception {
        final UUID uuid = getUUID(sender);
        final GraphDatabaseService graph = SettlerCraft.getInstance().getNeo4j();
        final StructureRepository structureRepository = new StructureRepository(graph);

        final Structure structure;
        String structureIdArg = args.getString(0);
        if (!NumberUtils.isNumber(structureIdArg)) {
            throw new CommandException("Expected a number but got '" + structureIdArg + "' \n" + "/structure:build [id]");
        }
        long id = Long.parseLong(structureIdArg);
        long start = System.currentTimeMillis();
        try (Transaction tx = graph.beginTx()) {
            StructureNode sn = structureRepository.findById(id);

            if (sn == null) {
                tx.success();
                throw new CommandException("Couldn't find a structure for #" + structureIdArg);
            }

            if (isPlayer(sender) && !isOP(sender) && sender instanceof IPlayer && !sn.getOwnerDomain().isOwnerOfType(uuid, OwnerType.MASTER)) {
                tx.success();
                throw new CommandException("You are not the 'MASTER' owner of this structure...");
            }
            structure = new Structure(sn);
            tx.success();
        }
        LOG.log(Level.INFO, "rollback in {0} ms", (System.currentTimeMillis() - start));

        if(!structure.getRollbackData().hasBlockStore()) {
            throw new CommandException("Rollback not available for this structure");
        }
        
        String force = args.hasFlag('f') ? args.getFlag('f') : null;
        final boolean useForce = force != null && (force.equals("t") || force.equals("true"));
        
        IContract rollbackContract =  new RollbackContract()
                .setRecursive(true)
                .setRestrictive(true)
                .setForced(useForce)
                .setReversedOrder(true)
                .setPlayer(uuid);
        structureAPI.getConstructionExecutor().submit(structure, rollbackContract);
        
    }

    @CommandPermissions(Permissions.STRUCTURE_CONSTRUCTION)
    @CommandExtras(async = true)
    @Command(aliases = {"structure:demolish", "stt:demolish"}, desc = "Demolishes a structure", min = 1, max = 1, flags = "f")
    public static void demolish(final CommandContext args, ICommandSender sender, IStructureAPI structureAPI) throws Exception {
        final GraphDatabaseService graph = SettlerCraft.getInstance().getNeo4j();
        final StructureRepository structureRepository = new StructureRepository(graph);
        final Structure structure;
        final UUID uuid = getUUID(sender);

        String structureIdArg = args.getString(0);
        if (!NumberUtils.isNumber(structureIdArg)) {
            throw new CommandException("Expected a number but got '" + structureIdArg + "' \n" + "/structure:demolish [id]");
        }

        // Check structure
        long id = Long.parseLong(structureIdArg);
        long start = System.currentTimeMillis();
        try (Transaction tx = graph.beginTx()) {
            StructureNode sn = structureRepository.findById(id);

            // Structure not found!
            if (sn == null) {
                tx.success();
                throw new CommandException("Couldn't find a structure for #" + structureIdArg);
            }

            // Player is not the owner!
            if (isPlayer(sender) && !isOP(sender) && !sn.getOwnerDomain().isOwnerOfType(uuid, OwnerType.MASTER)) {
                tx.success();
                throw new CommandException("You are not the 'MASTER' owner of this structure...");
            }
            structure = new Structure(sn);

            tx.success();
        }
        LOG.log(Level.INFO, "demolish in {0} ms", (System.currentTimeMillis() - start));

        // Use force?
        String force = args.hasFlag('f') ? args.getFlag('f') : null;
        final boolean useForce = force != null && (force.equals("t") || force.equals("true"));

        RollbackContract rollbackContract =  new RollbackContract();
        SafeContract safeContract = new SafeContract(rollbackContract);
        safeContract.setRecursive(true)
                .setRestrictive(true)
                .setForced(useForce)
                .setReversedOrder(true)
                .setPlayer(uuid);
        structureAPI.getConstructionExecutor().submit(structure, safeContract);

    }
    
    

    @CommandPermissions(Permissions.STRUCTURE_CONSTRUCTION)
    @CommandExtras(async = true)
    @Command(aliases = {"structure:halt", "stt:halt"}, desc = "Stop building or demolishing of a structure", min = 1, max = 1, flags = "f")
    public static void halt(final CommandContext args, ICommandSender sender, IStructureAPI structureAPI) throws Exception {
        final GraphDatabaseService graph = SettlerCraft.getInstance().getNeo4j();
        final StructureRepository structureRepository = new StructureRepository(graph);
        final Structure structure;
        final UUID uuid = getUUID(sender);
        final IColors colors = structureAPI.getPlatform().getChatColors();

        String structureIdArg = args.getString(0);
        if (!NumberUtils.isNumber(structureIdArg)) {
            throw new CommandException("Expected a number but got '" + structureIdArg + "'");
        }

        // Retrieve structure and perform checks
        long id = Long.parseLong(structureIdArg);
        long start = System.currentTimeMillis();
        try (Transaction tx = graph.beginTx()) {
            StructureNode sn = structureRepository.findById(id);

            if (sn == null) {
                tx.success();
                throw new CommandException("Couldn't find a structure for #" + structureIdArg);
            }

            if (isPlayer(sender) && !isOP(sender) && !sn.getOwnerDomain().isOwner(uuid)) {
                tx.success();
                throw new CommandException("You don't own this structure...");
            }
            structure = new Structure(sn);

            tx.success();
        }
        LOG.log(Level.INFO, "stop in {0} ms", (System.currentTimeMillis() - start));

        // Use force?
        String force = args.hasFlag('f') ? args.getFlag('f') : null;
        final boolean useForce = force != null && (force.equals("t") || force.equals("true"));

        // Stop current action
        String structureInfo = colors.reset() + ": #" + colors.gold() + structure.getId() + colors.blue() + " " + structure.getName();
        sender.sendMessage(colors.red() + "STOPPING" + structureInfo);
        
        structureAPI.getConstructionExecutor().purge(structure);

    }

    @CommandExtras(async = true)
    @Command(aliases = {"structure:masters", "stt:masters"}, desc = "")
    public static void masters(final CommandContext args, ICommandSender sender, IStructureAPI structureAPI) throws Exception {
        ownerships(sender, args, structureAPI, OwnerType.MASTER);
    }

    @CommandExtras(async = true)
    @Command(aliases = {"structure:owners", "stt:owners"}, desc = "")
    public static void owners(final CommandContext args, ICommandSender sender, IStructureAPI structureAPI) throws Exception {
        ownerships(sender, args, structureAPI, OwnerType.OWNER);
    }

    @CommandExtras(async = true)
    @Command(aliases = {"structure:members", "stt:members"}, desc = "")
    public static void members(final CommandContext args, ICommandSender sender, IStructureAPI structureAPI) throws Exception {
        ownerships(sender, args, structureAPI, OwnerType.MEMBER);
    }

    private static StructureNode getStructure(CommandContext args, Transaction activeTransaction) throws CommandException {
        final GraphDatabaseService graph = SettlerCraft.getInstance().getNeo4j();
        final IStructureRepository structureRepository = new StructureRepository(graph);
        Long structureId = null;
        String structureIdArg = args.getString(0);
        if (!NumberUtils.isNumber(structureIdArg)) {
            activeTransaction.success();
            throw new CommandException("Expected a number but got '" + structureIdArg + "'");
        }
        structureId = Long.parseLong(structureIdArg);
        StructureNode structure = structureRepository.findById(structureId);
        if (structure == null) {
            activeTransaction.success();
            throw new CommandException("Couldn't find structure for id #" + structureId);
        }
        return structure;
    }

    private static void showOwnerships(ICommandSender sender, CommandContext args, IStructureAPI structureAPI, OwnerType type) throws CommandException {
        IColors COLOR = structureAPI.getPlatform().getChatColors();
        GraphDatabaseService graph = SettlerCraft.getInstance().getNeo4j();

        TreeSet<String> ownerships = Sets.newTreeSet(ALPHABETICAL_ORDER);
        String structureName = null;
        Long structureId;
        try (Transaction tx = graph.beginTx()) {
            StructureNode structure = getStructure(args, tx);
            structureName = structure.getName();
            structureId = structure.getId();
            for (SettlerNode member : structure.getOwnerDomain().getOwners(type)) {
                ownerships.add(member.getName());
            }

            tx.success();
        }
        String ownershipString = "";
        int size = ownerships.size();

        if (size != 0) {
            int count = 0;

            for (String ownership : ownerships) {
                ownershipString += ownership;
                count++;
                if (count != size) {
                    ownershipString += ", ";
                }

            }
        } else {
            ownershipString = "None";
        }

        String ownersString;
        if (type == OwnerType.MASTER) {
            ownersString = "Masters: ";
        } else if (type == OwnerType.OWNER) {
            ownersString = "Owners: ";
        } else {
            ownersString = "Members: ";
        }

        if (size == 0) {
            sender.sendMessage("#" + COLOR.gold() + structureId + " - " + COLOR.blue() + structureName, COLOR.reset() + ownersString + COLOR.red() + ownershipString);
        } else {
            sender.sendMessage("#" + COLOR.gold() + structureId + " - " + COLOR.blue() + structureName, COLOR.reset() + ownersString, ownershipString);
        }

    }

    private static void updateOwnership(ICommandSender sender, CommandContext args, IStructureAPI structureAPI, OwnerType type) throws CommandException {
        GraphDatabaseService graph = SettlerCraft.getInstance().getNeo4j();
        IColors colors = structureAPI.getPlatform().getChatColors();
        ISettlerRepository settlerRepository = new SettlerRepositiory(graph);
        
        // Set help message
        String help;
        if (type == OwnerType.MASTER) {
            help = "/structure:masters [structureId] <add|remove> [playerName| #settler-id]";
        } else if (type == OwnerType.OWNER) {
            help = "/structure:owners [structureId] <add|remove> [playerName| #settler-id]";
        } else {
            help = "/structure:members [structureId] <add|remove> [playerName| #settler-id]";
        }
        
        // Check argument lengths
        if (args.argsLength() < 3) {
            throw new CommandException("Too few arguments" + "\n" + help);
        } else if (args.argsLength() > 3) {
            throw new CommandException("Too many arguments" + "\n" + help);
        }

        String method = args.getString(1);
        String playerArg = args.getString(2);

        if (!method.equalsIgnoreCase("add") && !method.equalsIgnoreCase("remove")) {
            throw new CommandException("Unknown method '" + method + "', expected 'add' or 'remove'" + "\n" + help);
        }

        try (Transaction tx = graph.beginTx()) {
            StructureNode structureNode = getStructure(args, tx);
            if (!isOP(sender)) {
                IPlayer player = (IPlayer) sender;
                IOwnership ownership = structureNode.getOwnerDomain().getOwnership(player.getUniqueId());

                if (ownership == null) {
                    tx.success();
                    throw new CommandException("You don't own this structure");
                }

                if (ownership.getOwnerType().getTypeId() < type.getTypeId()) {
                    tx.success();
                    throw new CommandException("You don't have enough privileges to " + method + " players of type '" + type.name() + "'");
                }

                if (type == OwnerType.MASTER && ownership.getOwnerType() == type && method.equalsIgnoreCase("remove")) {
                    tx.success();
                    throw new CommandException("Players of type '" + OwnerType.MASTER + "' can't remove each other");
                }

            }

            IPlayer ply;
            if (playerArg.startsWith("#")) {
                if (!isUniquePlayerName(playerArg, structureAPI)) {
                    throw new CommandException("Player name '" + playerArg + "' is not unique \n"
                            + "Use player id instead of name \n"
                            + "Usage: /structure:owners [structureId] " + method + " -# [playerId]"
                            + "The other player can get it's player id by using the '/settler:me' command"
                    );
                }

                ply = structureAPI.getPlatform().getPlayer(playerArg);
                if (ply == null) {
                    tx.success();
                    throw new CommandException("Couldn't find a player for '" + playerArg + "'");
                }
            } else {
                Long id = null;
                try {
                    id = Long.parseLong(playerArg);
                    BaseSettlerNode sn = settlerRepository.findById(id);
                    if (sn == null) {
                        tx.success();
                        throw new CommandException("Couldn't find a player for id'" + playerArg + "'");
                    }
                    ply = structureAPI.getPlatform().getPlayer(sn.getUniqueId());

                } catch (NumberFormatException nfe) {
                    tx.success();
                    String error = "Expected ";
                    if(type == OwnerType.MEMBER) {
                        error += "/stt:members ";
                    } else if(type == OwnerType.OWNER) {
                        error += "/stt:owners ";
                    } else {
                        error += "/stt:masters ";
                    }
                    error += method + "#[settler-id] \nbut got '" + playerArg + "' after #";
                    throw new CommandException(error);
                }
            }

            UUID uuid = ply.getUniqueId();
            if (method.equalsIgnoreCase("add")) {
                BaseSettlerNode settler = settlerRepository.findByUUID(ply.getUniqueId());
                OwnerDomainNode ownerDomain = structureNode.getOwnerDomain();
                IOwnership ownershipToAdd = ownerDomain.getOwnership(settler.getUniqueId());

                if (ownershipToAdd == null) {
                    ownerDomain.setOwnership(settler, type);
                    structureAPI.getEventDispatcher().dispatchEvent(new StructureAddOwnerEvent(uuid, new Structure(structureNode), type));
                    sender.sendMessage("Successfully added '" + colors.green() + ply.getName() + colors.reset() + "' to #" + colors.gold() + structureNode.getId() + " " + colors.blue() + structureNode.getName() + colors.reset() + " as " + colors.yellow() + type.name());
                } else {
                    ownerDomain.setOwnership(settler, type);
                    structureAPI.getEventDispatcher().dispatchEvent(new StructureAddOwnerEvent(uuid, new Structure(structureNode), type));
                    sender.sendMessage("Updated ownership of '" + colors.green() + ply.getName() + colors.reset() + "' to " + colors.yellow() + type.name() + colors.reset() + " for structure ",
                            "#" + colors.gold() + structureNode.getId() + " " + colors.blue() + structureNode.getName());
                }
            } else { // remove
                OwnerDomainNode ownerDomain = structureNode.getOwnerDomain();
                if (!ownerDomain.removeOwnership(uuid)) {
                    throw new CommandException(ply.getName() + " does not own this structure...");
                }
                structureAPI.getEventDispatcher().dispatchEvent(new StructureRemoveOwnerEvent(uuid, new Structure(structureNode), type));
                sender.sendMessage("Successfully removed '" + colors.green() + ply.getName() + colors.reset() + "' from #" + colors.gold() + structureNode.getId() + " " + colors.blue() + structureNode.getName() + " as " + colors.yellow() + type.name());
            }
            tx.success();
        }
    }
    
    private static void ownerships(ICommandSender sender, CommandContext args, IStructureAPI structureAPI, OwnerType requestedType) throws CommandException {
        if (args.argsLength() == 1) {
            showOwnerships(sender, args, structureAPI, requestedType);
        } else {
            updateOwnership(sender, args, structureAPI, requestedType);
        }
        
    }

    @CommandPermissions(Permissions.STRUCTURE_LIST)
    @CommandExtras(async = true)
    @Command(aliases = {"structure:list", "stt:list"}, desc = "")
    public static void list(final CommandContext args, ICommandSender sender, IStructureAPI structureAPI) throws Exception {
        final GraphDatabaseService graph = SettlerCraft.getInstance().getNeo4j();
        final ISettlerRepository structureOwnerRepository = new SettlerRepositiory(graph);
        final IColors colors = structureAPI.getPlatform().getChatColors();

        int page = 0;
        final UUID playerId;
        if (args.argsLength() == 0) {
            if (!isPlayer(sender)) {
                throw new CommandUsageException("Too few arguments!", "/structure:list - Is for players only");
            }

            playerId = getUUID(sender);

        } else if (args.argsLength() == 1) {
            if (!isPlayer(sender)) {
                throw new CommandUsageException("Too few arguments!", "/structure:list [page] - Is for players only");
            }

            playerId = getUUID(sender);
            String pageArg = args.getString(0);
            if (NumberUtils.isNumber(pageArg)) {
                page = Integer.parseInt(pageArg);
            } else {
                throw new CommandException("Expected a number but got '" + pageArg + "'");
            }
        } else { // 2 arguments

            String plyName = args.getString(0);
            IPlayer ply = SettlerCraft.getInstance().getPlatform().getServer().getPlayer(plyName);
            if (ply == null) {
                throw new CommandException("Player '" + plyName + "' not found");
            }
            playerId = ply.getUniqueId();
            String pageString = args.getString(1);
            if (NumberUtils.isNumber(pageString)) {
                page = Integer.parseInt(pageString);
            } else {
                throw new CommandException("Expected a number but got '" + pageString + "'");
            }
        }

        final int p = page;

        String[] message = new String[MAX_LINES];
        int skip = p * (MAX_LINES - 1);
        int limit = (MAX_LINES - 1);

        long start = System.currentTimeMillis();
        try (Transaction tx = graph.beginTx()) {
            SettlerNode structureOwner = structureOwnerRepository.findByUUID(playerId);

            long countStart = System.currentTimeMillis();
            long totalStructures = structureOwner.getStructureCount();
            LOG.log(Level.INFO, "list count in {0} ms", (System.currentTimeMillis() - countStart));
            long totalPages = Math.round(Math.ceil(totalStructures / (MAX_LINES - 1)));
            List<StructureNode> structures = structureOwner.getStructures(skip, limit);
            if (p > totalPages || p < 0) {
                tx.success();
                throw new CommandException("Page " + p + " out of " + totalPages + "...");
            }

            int lineNumber = 0;
            message[0] = "-----------(Page: " + p + "/" + totalPages + ", Structures: " + totalStructures + ")---------------";
            lineNumber++;
            for (StructureNode structureNode : structures) {
//                

                String line;
                double price = structureNode.getPrice();
                if (price > 0.0d) {
                    line = String.format("#%-1s%-10d%-3s%-40s%-15s%-1s%-5s", colors.gold(), structureNode.getId(), colors.blue(), structureNode.getName(), getStatusString(structureNode, colors), colors.yellow(), ShopUtil.valueString(price));
                } else {
                    line = String.format("#%-1s%-10d%-3s%-40s%-15s", colors.gold(), structureNode.getId(), colors.blue(), structureNode.getName(), getStatusString(structureNode, colors));
                }

                message[lineNumber] = line;
                lineNumber++;
            }
            tx.success();
        }
        LOG.log(Level.INFO, "list structures in {0} ms", (System.currentTimeMillis() - start));
        sender.sendMessage(message);
    }

    @CommandPermissions(Permissions.STRUCTURE_LOCATION)
    @CommandExtras(async = true, senderType = CommandSenderType.PLAYER)
    @Command(aliases = {"structure:location", "stt:location"}, desc = "")
    public static void location(final CommandContext args, ICommandSender sender, IStructureAPI structureAPI) throws Exception {
        final GraphDatabaseService graph = SettlerCraft.getInstance().getNeo4j();
        final IStructureRepository structureRepository = new StructureRepository(graph);
        final IColors colors = structureAPI.getPlatform().getChatColors();
        final IPlayer player = (IPlayer) sender;

        if (args.argsLength() == 1) {
            Long id;
            String idArg = args.getString(0);
            try {
                id = Long.parseLong(idArg);
            } catch (NumberFormatException nfe) {
                throw new CommandException("Expected a number but got '" + idArg + "'");
            }

            ILocation loc = player.getLocation();
            long start = System.currentTimeMillis();
            try (Transaction tx = graph.beginTx()) {
                StructureNode structure = structureRepository.findById(id);
                if (structure == null) {
                    tx.success();
                    throw new CommandException("Couldn't find structure for id #" + id);
                }
                if (structure.getStatus() == ConstructionStatus.REMOVED) {
                    tx.success();
                    throw new CommandException("Can't get relative location of a removed structure");
                }

                World w = SettlerCraft.getInstance().getWorld(structure.getWorldUUID());
                if (!w.getName().equals(player.getWorld().getName())) {
                    tx.success();
                    throw new CommandException("Structure must be in the same world...");
                }
                Vector rel = structure.getRelativePosition(new Vector(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
                player.sendMessage("Your relative position is " + colors.yellow() + "x: " + colors.reset() + rel.getBlockX() + colors.yellow() + " y: " + colors.reset() + rel.getBlockY() + colors.yellow() + " z: " + colors.reset() + rel.getBlockZ());

                tx.success();
            }
            LOG.log(Level.INFO, "relative location in {0} ms", (System.currentTimeMillis() - start));

        } else {
            IPlayer ply = (IPlayer) player;
            ILocation loc = ply.getLocation();

            Vector pos = new Vector(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            long start = System.currentTimeMillis();
            try (Transaction tx = graph.beginTx()) {
                StructureNode structure = structureRepository.findStructureOnPosition(ply.getWorld().getUUID(), pos);

                if (structure == null) {
                    throw new CommandException("Not within a structure...");
                }

                String worldName = structure.getWorldName();
                if (!worldName.equals(player.getWorld().getName())) {
                    throw new CommandException("Structure must be in the same world...");
                }

                Vector rel = structure.getRelativePosition(new Vector(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));

                player.sendMessage("Your relative position is " + colors.yellow() + "x: " + colors.reset() + rel.getBlockX() + colors.yellow() + " y: " + colors.reset() + rel.getBlockY() + colors.yellow() + " z: " + colors.reset() + rel.getBlockZ());
                tx.success();
            }
            LOG.log(Level.INFO, "relative location in {0} ms", (System.currentTimeMillis() - start));
        }

    }

    @CommandPermissions(Permissions.STRUCTURE_CREATE)
    @CommandExtras(async = true, senderType = CommandSenderType.PLAYER)
    @Command(aliases = {"structure:create", "/stt:create"}, desc = "Create a structure from selection or given arguments")
    public static void create(final CommandContext args, ICommandSender sender, IStructureAPI structureAPI) throws Exception {
        if (args.argsLength() == 0) { // Create from selection
            IPlayer plySender = (IPlayer) sender;
            Player player = SettlerCraft.getInstance().getPlayer(plySender.getUniqueId());

        }
    }
}
