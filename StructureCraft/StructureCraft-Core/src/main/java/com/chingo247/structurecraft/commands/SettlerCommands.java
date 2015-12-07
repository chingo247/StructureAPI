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
import com.chingo247.settlercraft.core.model.settler.BaseSettlerNode;
import com.chingo247.settlercraft.core.model.settler.IBaseSettler;
import com.chingo247.xplatform.core.ICommandSender;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.chingo247.structurecraft.model.settler.ISettlerRepository;
import com.chingo247.structurecraft.model.settler.SettlerRepositiory;
import com.chingo247.structurecraft.IStructureAPI;
import com.chingo247.structurecraft.platform.permission.Permissions;
import com.chingo247.xplatform.core.IColors;
import com.chingo247.xplatform.core.ILocation;
import com.chingo247.xplatform.core.IPlayer;
import com.chingo247.xplatform.core.IWorld;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

/**
 *
 * @author Chingo
 */
public class SettlerCommands {

    @CommandPermissions(Permissions.SETTLER_ME)
    @CommandExtras(async = true, senderType = CommandSenderType.PLAYER)
    @Command(aliases = {"settler:me"}, usage = "/settler:me", desc = "Display your settler id", max = 0)
    public static void me(final CommandContext args, ICommandSender sender, IStructureAPI structureAPI) throws CommandException {
        final IPlayer player = (IPlayer) sender;
        final GraphDatabaseService graph = SettlerCraft.getInstance().getNeo4j();
        final ISettlerRepository settlerRepository = new SettlerRepositiory(graph);
        final IColors COLOR = structureAPI.getPlatform().getChatColors();
        
        try (Transaction tx = graph.beginTx()) {
            BaseSettlerNode node = settlerRepository.findByUUID(player.getUniqueId()); // NEVER NULL
            player.sendMessage("Your unique id is #" + COLOR.gold() + node.getId());
            tx.success();
        }
    }
    
    @CommandPermissions(Permissions.SETTLER_ME)
    @CommandExtras(async = false, senderType = CommandSenderType.PLAYER)
    @Command(aliases = {"settler:test"}, usage = "/settler:test [num]", desc = "Display your settler id", max = 1)
    public static void meTest(final CommandContext args, ICommandSender sender, IStructureAPI structureAPI) throws CommandException {
        
        IPlayer player = (IPlayer) sender;
        
        ILocation loc = player.getLocation();
        IWorld w = loc.getWorld();
        
        int x = (loc.getBlockX() >> 4) * 16;
        int z = (loc.getBlockX() >> 4) * 16;
        
        int distance = args.getInteger(0);
        
        World world = Bukkit.getWorld(w.getUUID());
        long time = 0;
        int count = 0;
        for(int xPos = x; xPos < x + (16 * distance); xPos++) {
            for(int zPos = z; zPos < z + (16 * distance); zPos++) {
                
                long start = System.currentTimeMillis();
                Chunk c = world.getChunkAt(xPos, zPos);
                c.getChunkSnapshot();
                long saveTime = (System.currentTimeMillis() - start);
                
//                System.out.println("Snapshot ("+xPos+"," + zPos + ") in " + saveTime + " ms");
                time += saveTime;
                count++;
                
            }
        }
        System.out.println("Total time = " + time + " ms");
        System.out.println("Total chunks = " + count);
        System.out.println("Average time = " + (time > 0 ? time / count : 0) + " ms");
        
        
        
    }

}
