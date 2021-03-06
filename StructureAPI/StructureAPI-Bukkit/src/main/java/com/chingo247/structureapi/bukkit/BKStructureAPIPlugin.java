package com.chingo247.structureapi.bukkit;

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
import com.chingo247.settlercraft.core.SettlerCraft;
import com.chingo247.settlercraft.core.exception.SettlerCraftException;
import com.chingo247.structureapi.bukkit.listener.PlanListener;
import com.chingo247.settlercraft.core.platforms.bukkit.BKPermissionRegistry;
import com.chingo247.settlercraft.core.platforms.services.IEconomyProvider;
import com.chingo247.settlercraft.core.commands.util.PluginCommandManager;
import com.chingo247.settlercraft.core.util.JarUtil;
import com.chingo247.structureapi.StructureAPI;
import com.chingo247.structureapi.StructureInvalidator;
import com.chingo247.structureapi.commands.ConstructionZoneCommands;
import com.chingo247.structureapi.commands.SchematicCommands;
import com.chingo247.structureapi.commands.SettlerCommands;
import com.chingo247.structureapi.commands.StructureCommands;
import com.chingo247.structureapi.commands.StructurePlanCommands;
import com.chingo247.structureapi.exeption.StructureAPIException;
import com.chingo247.structureapi.plan.PlanGenerator;
import com.chingo247.structureapi.platform.ConfigProvider;
import com.chingo247.structureapi.platform.permission.PermissionManager;
import com.chingo247.structureapi.platform.services.holograms.StructureHologramManager;
import com.chingo247.xplatform.core.APlatform;
import com.chingo247.xplatform.core.IPlugin;
import com.chingo247.xplatform.core.IScheduler;
import com.chingo247.xplatform.platforms.bukkit.BukkitConsoleSender;
import com.chingo247.xplatform.platforms.bukkit.BukkitPlatform;
import com.chingo247.xplatform.platforms.bukkit.BukkitPlayer;
import com.chingo247.xplatform.platforms.bukkit.BukkitPlugin;
import com.chingo247.xplatform.platforms.bukkit.BukkitServer;
import com.sk89q.bukkit.util.CommandsManagerRegistration;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.minecraft.util.commands.CommandUsageException;
import com.sk89q.minecraft.util.commands.MissingNestedCommandException;
import com.sk89q.minecraft.util.commands.WrappedCommandException;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.dom4j.DocumentException;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 *
 * @author Chingo
 */
public class BKStructureAPIPlugin extends JavaPlugin implements IPlugin {

    private static final String RESOURCES_PATH = "com/chingo247/resources/";
    public static final Level LOG_LEVEL = Level.SEVERE;
    public static final String MSG_PREFIX = "[SettlerCraft]: ";

    private IEconomyProvider economyProvider;
    private ConfigProvider configProvider;
    private static BKStructureAPIPlugin instance;
    private GraphDatabaseService graph;
    private PluginCommandManager commands;

    @Override
    public void onEnable() {
        instance = this;
        if (Bukkit.getPluginManager().getPlugin("SettlerCraft") != null) {
            Bukkit.getConsoleSender().sendMessage(new String[]{
                ChatColor.RED + "[SettlerCraft]: Please remove the old jar of SettlerCraft!",
                ChatColor.RED + "[SettlerCraft]: This should be named something like 'SettlerCraft-1.0-RC3.jar'",
                ChatColor.RED + "[SettlerCraft]: Or something like 'SettlerCraft-1.0-RC4-1.jar'",
                ChatColor.RED + "[SettlerCraft]: and needs to be removed!",}
            );
            return;
        }

        if (Bukkit.getPluginManager().getPlugin("SettlerCraft-Core") == null) {
            System.out.println(MSG_PREFIX + " SettlerCraft-Core NOT FOUND!!! Disabling...");
            this.setEnabled(false);
            return;
        }
        if (Bukkit.getPluginManager().getPlugin("SettlerCraft-MenuAPI") == null) {
            System.out.println(MSG_PREFIX + " SettlerCraft-MenuAPI NOT FOUND!!! Disabling...");
            this.setEnabled(false);
            return;
        }
        if (Bukkit.getPluginManager().getPlugin("WorldEdit") == null) {
            System.out.println(MSG_PREFIX + " WorldEdit NOT FOUND!!! Disabling...");
            this.setEnabled(false);
            return;
        }
        if (Bukkit.getPluginManager().getPlugin("AsyncWorldEdit") == null) {
            System.out.println(MSG_PREFIX + " AsyncWorldEdit NOT FOUND!!! Disabling...");
            this.setEnabled(false);
            return;
        }

        try {
            createDefaults();
        } catch (Exception ex) {
            Bukkit.getConsoleSender().sendMessage(new String[]{
                ChatColor.RED + ex.getMessage(), ChatColor.RED + "Disabling SettlerCraft-StructureAPI"
            });
//            Logger.getLogger(BKStructureAPIPlugin.class.getName()).log(Level.SEVERE, null, ex);
            this.setEnabled(false);
            return;
        }

        // Get GraphDatabase
        graph = SettlerCraft.getInstance().getNeo4j();

        try {
            try {
                // Initialize Config
                configProvider = ConfigProvider.load(new File(getDataFolder(), "config.yml"));
            } catch (StructureAPIException ex) {
                System.out.println(ChatColor.RED + "[StructureAPI] " + ex.getMessage());
                this.setEnabled(false);
                System.out.println(ChatColor.RED + "[StructureAPI]: Disabling SettlerCraft-StructureAPI");
                return;
            }
        } catch (IOException ex) {
            Logger.getLogger(BKStructureAPIPlugin.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Register plugin & ConfigProvider
        StructureAPI structureAPI = (StructureAPI) StructureAPI.getInstance();
        try {
            structureAPI.registerStructureAPIPlugin(this);
        } catch (StructureAPIException ex) {
            this.setEnabled(false);
            
            Logger.getLogger(BKStructureAPIPlugin.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        structureAPI.registerConfigProvider(configProvider);
        structureAPI.registerAWE(new BKAsyncWorldEditIntegration());

        economyProvider = SettlerCraft.getInstance().getEconomyProvider();

        // Run invalidation!
        StructureInvalidator invalidator = new StructureInvalidator(new BukkitServer(Bukkit.getServer()), SettlerCraft.getInstance().getExecutor(), graph, economyProvider);
        invalidator.invalidate();

        // Initialize the StructureAPI
        try {
            structureAPI.initialize();
        } catch (DocumentException | SettlerCraftException ex) {
            java.util.logging.Logger.getLogger(BKStructureAPIPlugin.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(MSG_PREFIX + "Disabling SettlerCraft-StructureAPI");
            return;
        }

        structureAPI.registerAsyncEditSesionFactoryProvider(new BKAsyncEditSessionFactoryProvider());

        // Register Listeners
        Bukkit.getPluginManager().registerEvents(new PlanListener(structureAPI, economyProvider), this);
//        Bukkit.getPluginManager().registerEvents(new PhysicsListener(StructureAPI.getInstance().getPhysicsWatcher()), this);

        // Generate Plans 
        File generationDirectory = StructureAPI.getInstance().getGenerationDirectory();
        generationDirectory.mkdirs();
        PlanGenerator.generate(generationDirectory);

//        // Setup HolographicDisplays (if available)
        if (configProvider.isUseHolograms()) {
            StructureHologramManager.getInstance().inititialize(new BukkitPlugin(this));
        }

        // Register permissions
        PermissionManager.getInstance().registerPermissionRegistry(new BKPermissionRegistry());

        // Setup Commands
        registerCommands();
        
    }

    private void registerCommands() {
        this.commands = new PluginCommandManager(StructureAPI.getInstance().getExecutor(), SettlerCraft.getInstance().getPlatform());
        CommandsManagerRegistration cmdRegister = new CommandsManagerRegistration(this, commands);
        cmdRegister.register(SchematicCommands.class);
        cmdRegister.register(StructureCommands.class);
        cmdRegister.register(SettlerCommands.class);
        cmdRegister.register(ConstructionZoneCommands.class);
        cmdRegister.register(StructurePlanCommands.class);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            if (sender instanceof Player) {
                this.commands.execute(command.getName(), args, new BukkitPlayer((Player) sender), new BukkitPlayer((Player) sender), StructureAPI.getInstance());
            } else {
                this.commands.execute(command.getName(), args, new BukkitConsoleSender(sender), new BukkitConsoleSender(sender), StructureAPI.getInstance());
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
    public void onDisable() {
        if(StructureAPI.getInstance().getExecutor() != null) {
            System.out.println("[SettlerCraft-StructureAPI]: Stopping running threads...");
            StructureAPI.getInstance().getExecutor().shutdown();
        }
    }


    private void createDefaults() throws Exception {
        try {
            checkConfigUpdate();
            JarUtil.createDefault(new File(getDataFolder(), "menu.xml"), getFile(), RESOURCES_PATH + "menu.xml");
        } catch (IOException ex) {
            Logger.getLogger(BKStructureAPIPlugin.class.getName()).log(Level.SEVERE, null, ex);
        }
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
        BKStructureAPIConfigUpdater updater = new BKStructureAPIConfigUpdater(configFile, newConfigFile);
        try {
            updater.checkAndUpdate();
        } catch (Exception ex) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + ex.getMessage());
            throw ex;
        }
        // Delete the temp directory
        FileUtils.deleteDirectory(temp);
    }

    public static BKStructureAPIPlugin getInstance() {
        return instance;
    }

    public ConfigProvider getConfigProvider() {
        return configProvider;
    }

    @Override
    public IScheduler getScheduler() {
        return getPlatform().getServer().getScheduler(new BukkitPlugin(this));
    }

    @Override
    public APlatform getPlatform() {
        return new BukkitPlatform(Bukkit.getServer());
    }

}
