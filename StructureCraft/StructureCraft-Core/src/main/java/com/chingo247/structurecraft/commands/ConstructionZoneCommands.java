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

import com.chingo247.settlercraft.core.SettlerCraft;
import com.chingo247.settlercraft.core.commands.util.CommandExtras;
import com.chingo247.settlercraft.core.commands.util.CommandSenderType;
import com.chingo247.settlercraft.core.event.EventManager;
import com.chingo247.settlercraft.core.event.async.AsyncEventManager;
import com.chingo247.settlercraft.core.model.interfaces.IBaseSettler;
import com.chingo247.structurecraft.event.zone.ConstructionZoneRemoveOwnerEvent;
import com.chingo247.structurecraft.event.zone.ConstructionZoneUpdateOwnerEvent;
import com.chingo247.structurecraft.event.zone.DeleteConstructionZoneEvent;
import com.chingo247.structurecraft.model.owner.IOwnership;
import com.chingo247.structurecraft.model.owner.OwnerDomainNode;
import com.chingo247.structurecraft.model.owner.OwnerType;
import com.chingo247.structurecraft.model.settler.ISettlerRepository;
import com.chingo247.structurecraft.model.settler.SettlerRepositiory;
import com.chingo247.structurecraft.model.zone.ConstructionZoneNode;
import com.chingo247.structurecraft.model.zone.ConstructionZoneRepository;
import com.chingo247.structurecraft.model.zone.IConstructionZone;
import com.chingo247.structurecraft.model.zone.IConstructionZoneRepository;
import com.chingo247.structurecraft.IStructureAPI;
import com.chingo247.structurecraft.model.settler.SettlerNode;
import com.chingo247.structurecraft.model.zone.ConstructionZone;
import com.chingo247.structurecraft.placing.constructionzone.IConstructionZonePlaceResult;
import com.chingo247.structurecraft.placing.constructionzone.IConstructionZonePlacer;
import com.chingo247.structurecraft.placing.constructionzone.IConstructionZonePlacerFactory;
import com.chingo247.structurecraft.platform.permission.Permissions;
import com.chingo247.xplatform.core.IColors;
import com.chingo247.xplatform.core.ICommandSender;
import com.chingo247.xplatform.core.IPlayer;
import com.google.common.collect.Sets;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.CommandUsageException;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;
import org.apache.commons.lang.math.NumberUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

/**
 *
 * @author Chingo
 */
public class ConstructionZoneCommands {
    
    private static final Comparator<String> ALPHABETICAL_ORDER = new Comparator<String>() {

        @Override
        public int compare(String o1, String o2) {
            return o1.toLowerCase().compareTo(o2.toLowerCase());
        }
    };
    
    
    
    
    @CommandPermissions(Permissions.CONSTRUCTIONZONE_CREATE)
    @CommandExtras(async = true, senderType = CommandSenderType.PLAYER)
    @Command(aliases = {"constructionzone:create", "cstz:create"}, usage = "/constructionzone:create", desc = "Create a construction zone", max = 6)
    public static void create(final CommandContext args, ICommandSender sender, IStructureAPI structureAPI) throws CommandException {
        IPlayer player = (IPlayer) sender;
        IConstructionZonePlacerFactory zonePlacerFactory = structureAPI.getConstructionZonePlacerFactory();
        IConstructionZonePlacer placer = zonePlacerFactory.createPlacer(player.getName());
        
        CuboidRegion region;
        if(args.argsLength() == 0) {
            Player worldEditPlayer = SettlerCraft.getInstance().getPlayer(player.getUniqueId());
            LocalSession session = WorldEdit.getInstance().getSessionManager().get(worldEditPlayer);
            
            RegionSelector rs = session.getRegionSelector(worldEditPlayer.getWorld());
            if(!rs.isDefined()) {
                throw new CommandException("No active selection or arguments provided");
            }
            
            try {
                Region r = rs.getRegion();
                if(!(r instanceof CuboidRegion)) {
                    throw new CommandException("Only cuboid-regions are supported for creating construction zones from selection");
                }
                region = (CuboidRegion) r;
            } catch (IncompleteRegionException ex) {
                throw new CommandException(ex);
            }
            
        } else {
            if(args.argsLength() < 6) {
                throw new CommandUsageException("Too few arguments", "usage: /constructionzone:create [minX][minY][minZ][maxX][maxY][maxZ]");
            } 
            String usage = "usage: /cstz:create [minX][minY][minZ][maxX][maxY][maxZ]";
            int minX = getInt(args, 0, "minX", usage);
            int minY = getInt(args, 1, "minY", usage);
            int minZ = getInt(args, 2, "minZ", usage);
            int maxX = getInt(args, 3, "maxX", usage);
            int maxY = getInt(args, 4, "maxY", usage);
            int maxZ = getInt(args, 5, "maxZ", usage);
            region = new CuboidRegion(new BlockVector(minX, minY, minZ), new BlockVector(maxX, maxY, maxZ));
        }
        
        IConstructionZonePlaceResult result = placer.place(region);
        if(result.succes()) {
            sender.sendMessage("Succesfully created construction zone");
        } else {
            throw new CommandException(result.error());
        }
    }
    
    @CommandExtras(async = true)
    @Command(aliases = {"constructionzone:info", "cstz:info"}, usage = "/constructionzone:info", desc = "Show info about the zone with given id or the zone you are in if no id is given", max = 1)
    public static void info(final CommandContext args, ICommandSender sender, IStructureAPI structureAPI) throws CommandException {
        GraphDatabaseService graph = SettlerCraft.getInstance().getNeo4j();
        IConstructionZoneRepository zoneRepository = new ConstructionZoneRepository(graph);
        IConstructionZone zone = null;
        IColors colors = structureAPI.getPlatform().getChatColors();
        
        if(args.argsLength() == 0 && (sender instanceof IPlayer)) {
            IPlayer ply = (IPlayer) sender;
            
            try(Transaction tx = graph.beginTx()) {
                ConstructionZoneNode z = zoneRepository.findOnPosition(ply.getLocation());
                if(z == null) {
                    tx.success();
                    throw new CommandException("Not within a construction zone");
                }
                
                zone = new ConstructionZone(z);
                
                String line = getInfo(zone, colors);
                sender.sendMessage(line);
                tx.success();
            }
            
        } else if (args.argsLength() == 1) {
            long id = getLong(args, 0, "zone-id", "Usage: constructionzone:info [zone-id] \n"
                    + "alias: /cstz:info [zone-id]");
            
            try(Transaction tx = graph.beginTx()) {
                ConstructionZoneNode z = zoneRepository.findById(id);
                if(z == null) {
                    tx.success();
                    throw new CommandException("No construction zone for id #" + id);
                }
                zone = new ConstructionZone(z);
                
                String line = getInfo(zone, colors);
                sender.sendMessage(line);
                tx.success();
            }
            
            
        } else {
            throw new CommandUsageException("Too few arguments", "Usage: constructionzone:info [zone-id] \n"
                    + "alias: /cstz:info [zone-id]");
        }
    }
    
    private static String getInfo(IConstructionZone zone, IColors colors) {
        TreeSet<String> owners = Sets.newTreeSet(ALPHABETICAL_ORDER);

        List<SettlerNode> mastersNode = zone.getOwnerDomain().getOwners(OwnerType.MASTER);
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
        
        String line = "#" + colors.gold() + zone.getId() + "\n"
                + colors.reset() + "World: " + colors.yellow() + zone.getWorldName() + "\n";

        Vector position = zone.getCuboidRegion().getCenter();
        line += colors.reset() + "Location: " + colors.yellow() + "X: " + colors.reset() + position.getX()
                + " " + colors.yellow() + "Y: " + colors.reset() + position.getY()
                + " " + colors.yellow() + "Z: " + colors.reset() + position.getZ() + "\n";

        if (!owners.isEmpty()) {
            if (owners.size() == 1) {
                line += colors.reset() + "Owners(MASTER): " + colors.yellow() + ownershipString + "\n";
            } else {
                line += colors.reset() + "Owners(MASTER): \n" + colors.yellow() + ownershipString + "\n";
            }
        } else {
            line += colors.reset() + "Owners(MASTER): " +colors.red() + "NONE";
        }

        if (zone.hasWorldGuardRegion()) {
            line += colors.reset() + "WorldGuard-Region: " + colors.yellow() + zone.getWorldGuardRegion();
        }
        
        return line;
    }
    
    @CommandPermissions(Permissions.CONSTRUCTIONZONE_DELETE)
    @CommandExtras(async = true, senderType = CommandSenderType.PLAYER)
    @Command(aliases = {"constructionzone:delete", "cstz:delete"}, usage = "/constructionzone:delete [zone-id]", desc = "Delete a construction zone", min = 1, max = 1)
    public static void delete(final CommandContext args, ICommandSender sender, IStructureAPI structureAPI) throws CommandException {
        final GraphDatabaseService graph = SettlerCraft.getInstance().getNeo4j();
        final IConstructionZoneRepository zoneRepository = new ConstructionZoneRepository(graph);
       
        Long id = getLong(args, 0, "zone-id", 
                      "Usage: /constructionzone:delete [zone-id] \n"
                    + "Alias: /cstz:delete [zone-id]");
        
        try(Transaction tx = graph.beginTx()) {
            ConstructionZoneNode zone = zoneRepository.findById(id);
            if(zone == null) {
                tx.success();
                throw new CommandException("No construction zone found for id #" + id);
            }
            
            Node n = zone.getNode();
            for(Relationship rel : n.getRelationships()) {
                rel.delete();
            }
            tx.success();
            sender.sendMessage("Deleted construction-zone #" + id);
            AsyncEventManager.getInstance().post(new DeleteConstructionZoneEvent(new ConstructionZone(zone)));
        }
        
        
    }
    
    @CommandExtras(async = true)
    @Command(aliases = {"constructionzone:masters", "cstz:masters"}, desc = "")
    public static void masters(final CommandContext args, ICommandSender sender, IStructureAPI structureAPI) throws Exception {
        ownerships(sender, args, structureAPI, OwnerType.MASTER);
    }

    @CommandExtras(async = true)
    @Command(aliases = {"constructionzone:owners", "cstz:owners"}, desc = "")
    public static void owners(final CommandContext args, ICommandSender sender, IStructureAPI structureAPI) throws Exception {
        ownerships(sender, args, structureAPI, OwnerType.OWNER);
    }

    @CommandExtras(async = true)
    @Command(aliases = {"constructionzone:members", "cstz:members"}, desc = "")
    public static void members(final CommandContext args, ICommandSender sender, IStructureAPI structureAPI) throws Exception {
        ownerships(sender, args, structureAPI, OwnerType.MEMBER);
    }
    
    private static ConstructionZoneNode getConstructionZone(CommandContext args, Transaction activeTransaction) throws CommandException {
        final GraphDatabaseService graph = SettlerCraft.getInstance().getNeo4j();
        final IConstructionZoneRepository constructionRepository = new ConstructionZoneRepository(graph);
        Long zoneId = null;
        String zoneIdArg = args.getString(0);
        if (!NumberUtils.isNumber(zoneIdArg)) {
            activeTransaction.success();
            throw new CommandException("Expected a number but got '" + zoneIdArg + "'");
        }
        zoneId = Long.parseLong(zoneIdArg);
        ConstructionZoneNode zone = constructionRepository.findById(zoneId);
        if (zone == null) {
            activeTransaction.success();
            throw new CommandException("Couldn't find structure for id #" + zoneId);
        }
        return zone;
    }

    private static void showOwnerships(ICommandSender sender, CommandContext args, IStructureAPI structureAPI, OwnerType type) throws CommandException {
        IColors COLOR = structureAPI.getPlatform().getChatColors();
        GraphDatabaseService graph = SettlerCraft.getInstance().getNeo4j();

        TreeSet<String> ownerships = Sets.newTreeSet(ALPHABETICAL_ORDER);
        Long zoneId;
        try (Transaction tx = graph.beginTx()) {
            ConstructionZoneNode zone = getConstructionZone(args, tx);
            zoneId = zone.getId();
            for (SettlerNode member : zone.getOwnerDomain().getOwners(type)) {
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
            sender.sendMessage("ConstructionZone #" + COLOR.gold() + zoneId + COLOR.reset() + " - " , COLOR.reset() + ownersString + COLOR.red() + ownershipString);
        } else {
            sender.sendMessage("ConstructionZone #" + COLOR.gold() + zoneId + COLOR.reset() + " - " , COLOR.reset() + ownersString, ownershipString);
        }

    }

    private static void updateOwnership(ICommandSender sender, CommandContext args, IStructureAPI structureAPI, OwnerType type) throws CommandException {
        GraphDatabaseService graph = SettlerCraft.getInstance().getNeo4j();
        IColors colors = structureAPI.getPlatform().getChatColors();
        ISettlerRepository settlerRepository = new SettlerRepositiory(graph);
        
        // Set help message
        String help= "/cstz:"+type.plural().toLowerCase()+" [zone-id] <add|remove> [playerName| #settler-id]";
        
        
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
            ConstructionZoneNode zone = getConstructionZone(args, tx);
            if (!isOP(sender)) {
                IPlayer player = (IPlayer) sender;
                IOwnership ownership = zone.getOwnerDomain().getOwnership(player.getUniqueId());

                if (ownership == null) {
                    tx.success();
                    throw new CommandException("You don't own this zone");
                }
                
                if(ownership.getOwnerType() == OwnerType.MEMBER) {
                    tx.success();
                    throw new CommandException("You need to be owner at or a higher role to add players to this construction zone");
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
            if (!playerArg.startsWith("#")) {
                if (!isUniquePlayerName(playerArg, structureAPI)) {
                    
                    
                    
                    throw new CommandException("Player name '" + playerArg + "' is not unique \n"
                            + "Use player id instead of name \n"
                            + "Usage: /cstz:"+type.plural().toLowerCase()+" [zone-id] " + method + " # [playerId]"
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
                    IBaseSettler sn = settlerRepository.findById(id);
                    if (sn == null) {
                        tx.success();
                        throw new CommandException("Couldn't find a player for id'" + playerArg + "'");
                    }
                    ply = structureAPI.getPlatform().getPlayer(sn.getUniqueId());

                } catch (NumberFormatException nfe) {
                    tx.success();
                    String error = "Expected /cstz:" + type.plural().toLowerCase();
                    error += method + "#[settler-id] \nbut got '" + playerArg + "' after #";
                    throw new CommandException(error);
                }
            }

            UUID uuid = ply.getUniqueId();
            if (method.equalsIgnoreCase("add")) {
                IBaseSettler settler = settlerRepository.findByUUID(ply.getUniqueId());
                OwnerDomainNode ownerDomain = zone.getOwnerDomain();
                IOwnership ownershipToAdd = ownerDomain.getOwnership(settler.getUniqueId());

                if (ownershipToAdd == null) {
                    ownerDomain.setOwnership(settler, type);
                    EventManager.getInstance().getEventBus().post(new ConstructionZoneUpdateOwnerEvent(new ConstructionZone(zone), uuid, type));
                    sender.sendMessage("Successfully added '" + colors.green() + ply.getName() + colors.reset() + "' to #" + colors.gold() + zone.getId() + colors.reset() + " as " + colors.yellow() + type.name());
                } else {
                    ownerDomain.setOwnership(settler, type);
                    EventManager.getInstance().getEventBus().post(new ConstructionZoneUpdateOwnerEvent(new ConstructionZone(zone), uuid, type));
                    sender.sendMessage("Updated ownership of '" + colors.green() + ply.getName() + colors.reset() + "' to " + colors.yellow() + type.name() + colors.reset() + " for structure ",
                            "#" + colors.gold() + zone.getId());
                }
            } else { // remove
                OwnerDomainNode ownerDomain = zone.getOwnerDomain();
                if (!ownerDomain.removeOwnership(uuid)) {
                    throw new CommandException(ply.getName() + " does not own this construction zone...");
                }
                EventManager.getInstance().getEventBus().post(new ConstructionZoneRemoveOwnerEvent(new ConstructionZone(zone), uuid, type));
                sender.sendMessage("Successfully removed '" + colors.green() + ply.getName() + colors.reset() + "' from #" + colors.gold() + zone.getId() + " as " + colors.yellow() + type.name());
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
    
    private static boolean isPlayer(ICommandSender sender) {
        return (sender instanceof IPlayer);
    }
    
    private static boolean isConsole(ICommandSender sender) {
        return !(isPlayer(sender));
    }
    
     private static boolean isOP(ICommandSender sender) {
        return isConsole(sender) || ((IPlayer) sender).isOP();
    }
    
    private static long getLong(CommandContext args, int index, String value, String usage) throws CommandUsageException {
        long number;
        try {
            String arg = args.getString(index);
            number = Integer.parseInt(arg);
        } catch (NumberFormatException nfe) {
            throw new CommandUsageException("Expected a number for '" + value + "' but got " + args.getString(index), usage);
        }
        return number;
    }
    
    private static int getInt(CommandContext args, int index, String value, String usage) throws CommandUsageException {
        int number;
        try {
            String arg = args.getString(index);
            number = Integer.parseInt(arg);
        } catch (NumberFormatException nfe) {
            throw new CommandUsageException("Expected a number for '" + value + "' but got " + args.getString(index), usage);
        }
        return number;
    }
    
}
