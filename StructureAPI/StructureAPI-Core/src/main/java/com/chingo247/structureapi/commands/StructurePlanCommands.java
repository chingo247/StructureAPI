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
package com.chingo247.structureapi.commands;

import com.chingo247.settlercraft.core.commands.util.CommandExtras;
import com.chingo247.settlercraft.core.commands.util.CommandSenderType;
import com.chingo247.menuapi.menu.CategoryMenu;
import com.chingo247.settlercraft.core.SettlerCraft;
import com.chingo247.xplatform.core.ICommandSender;
import com.chingo247.xplatform.core.IPlayer;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandUsageException;
import com.chingo247.structureapi.IStructureAPI;
import com.chingo247.structureapi.StructureAPI;
import com.chingo247.structureapi.menu.StructurePlanItem;
import com.chingo247.structureapi.plan.PlanGenerator;
import com.chingo247.structureapi.platform.permission.Permissions;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import java.io.File;

/**
 *
 * @author Chingo
 */
public class StructurePlanCommands {

    @CommandExtras(senderType = CommandSenderType.CONSOLE)
    @Command(aliases = {"plans:generate"}, usage = "/plans:generate", desc = "Generates structure plans for schematics", max = 0)
    public static void generate(final CommandContext args, ICommandSender sender, IStructureAPI structureAPI) throws CommandUsageException, CommandException {
        File generationDirectory = StructureAPI.getInstance().getGenerationDirectory();
        PlanGenerator.generate(generationDirectory);
    }

    @CommandExtras(senderType = CommandSenderType.OP)
    @CommandPermissions({Permissions.RELOAD_PLANS})
    @Command(aliases = {"plans:reload"}, usage = "/plans:reload", desc = "Reloads structure plans", max = 0)
    public static void reload(final CommandContext args, ICommandSender sender, IStructureAPI structureAPI) throws CommandException {
        if (!structureAPI.isLoadingPlans()) {
            structureAPI.getStructurePlanManager().reload(false);
        } else {
            throw new CommandException("Already reloading plans!");
        }
    }

    @CommandExtras(senderType = CommandSenderType.PLAYER)
    @CommandPermissions({Permissions.OPEN_PLANMENU})
    @Command(aliases = {"plans:menu"}, usage = "/plans:menu", desc = "Opens the plan menu", max = 0)
    public static void openMenu(final CommandContext args, ICommandSender sender, IStructureAPI structureAPI) throws CommandException {
        if (!structureAPI.getConfig().isMenuEnabled()) {
            throw new CommandException("Plan menu is not enabled");
        }
        openMenu(structureAPI, (IPlayer) sender, true);
    }

    @CommandExtras(senderType = CommandSenderType.PLAYER)
    @CommandPermissions({Permissions.OPEN_PLANSHOP})
    @Command(aliases = {"plans:shop"}, usage = "/plans:shop", desc = "Opens the plan shop", max = 0)
    public static void openShop(final CommandContext args, ICommandSender sender, IStructureAPI structureAPI) throws CommandException {
        if (!structureAPI.getConfig().isShopEnabled()) {
            throw new CommandException("Plan shop is not enabled");
        }
        openMenu(structureAPI, (IPlayer) sender, false);
    }
    
    private static void openMenu(IStructureAPI structureAPI, IPlayer player, boolean isFree) throws CommandException {
        if (!isFree && SettlerCraft.getInstance().getEconomyProvider() == null) {
            throw new CommandException("Plan shop is not available (no economy plugin)");
        }

        if (structureAPI.isLoadingPlans()) {
            throw new CommandException("Plans are not loaded yet... please wait...");
        }

        CategoryMenu planmenu = StructureAPI.getInstance().createPlanMenu();
        if (planmenu == null) {
            throw new CommandException("Planmenu is initialized yet, please wait...");
        }

        if (!planmenu.isEnabled()) {
            throw new CommandException("Planmenu is not ready yet, please wait");
        }
        planmenu.setNoCosts(isFree);
        planmenu.openMenu(player);
    }

}
