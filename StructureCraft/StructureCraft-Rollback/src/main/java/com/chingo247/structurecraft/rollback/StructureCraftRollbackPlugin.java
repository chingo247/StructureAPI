/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.rollback;

import com.chingo247.settlercraft.core.SettlerCraft;
import com.chingo247.settlercraft.core.commands.util.PluginCommandManager;
import com.chingo247.settlercraft.core.concurrent.ThreadPoolFactory;
import com.chingo247.structurecraft.StructureAPI;
import com.chingo247.structurecraft.rollback.logging.impl.StructureBlockLogger;
import com.chingo247.structurecraft.rollback.platform.IRollbackConfig;
import com.chingo247.structurecraft.rollback.platform.IRollbackPlugin;
import com.chingo247.xplatform.platforms.bukkit.BukkitConsoleSender;
import com.chingo247.xplatform.platforms.bukkit.BukkitPlayer;
import com.sk89q.bukkit.util.CommandsManagerRegistration;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.minecraft.util.commands.CommandUsageException;
import com.sk89q.minecraft.util.commands.MissingNestedCommandException;
import com.sk89q.minecraft.util.commands.WrappedCommandException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.PrimeSoft.blocksHub.BlocksHub;
import org.PrimeSoft.blocksHub.IBlocksHubApi;
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 *
 * @author Chingo
 */
public class StructureCraftRollbackPlugin extends JavaPlugin implements IRollbackPlugin {

    private PluginCommandManager commands;
    public static final String MSG_PREFIX = "[StructureCraft-Rollback]: ";
    private static final String BLOCKS_HUB = "BlocksHub";
    private static StructureCraftRollbackPlugin instance;

    public static StructureCraftRollbackPlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin(BLOCKS_HUB) == null) {
            System.out.println(MSG_PREFIX + "Unable to find BlocksHub!!");
            return;
        }
        instance = this;

        BlocksHub blocksHub = (BlocksHub) Bukkit.getPluginManager().getPlugin(BLOCKS_HUB);
        final IBlocksHubApi blocksHubApi = blocksHub.getApi();
        StructureCraftRollback.getInstance().registerRollbackPlugin(this);
        StructureCraftRollback.getInstance().registerBlocksHubAPI(blocksHubApi);
        StructureCraftRollback.getInstance().initialize();

        // Register Commands
        registerCommands();
    }

    private void registerCommands() {
        this.commands = new PluginCommandManager(SettlerCraft.getInstance().getExecutor(), SettlerCraft.getInstance().getPlatform());

        CommandsManagerRegistration cmdRegister = new CommandsManagerRegistration(this, commands);
//        cmdRegister.register(LogCommands.class);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            if (sender instanceof Player) {
                this.commands.execute(command.getName(), args, new BukkitPlayer((Player) sender), new BukkitPlayer((Player) sender), StructureAPI.getInstance(), commands);
            } else {
                this.commands.execute(command.getName(), args, new BukkitConsoleSender(sender), new BukkitConsoleSender(sender), StructureAPI.getInstance(), commands);
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

    public File getConfigFile() {
        return new File(getDataFolder(), "config.yml");
    }

    @Override
    public File getWorkingDirectory() {
        return getDataFolder();
    }


}
