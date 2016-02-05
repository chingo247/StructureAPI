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

import com.chingo247.settlercraft.core.SettlerCraft;
import com.chingo247.settlercraft.core.commands.util.CommandExtras;
import com.chingo247.settlercraft.core.commands.util.CommandSenderType;
import com.chingo247.structureapi.model.structure.IStructureRepository;
import com.chingo247.structureapi.model.structure.Structure;
import com.chingo247.structureapi.model.structure.StructureNode;
import com.chingo247.structureapi.model.structure.StructureRepository;
import com.chingo247.structureapi.plan.IStructurePlan;
import com.chingo247.structureapi.plan.StructurePlanManager;
import com.chingo247.structureapi.IStructureAPI;
import com.chingo247.structureapi.exeption.StructureException;
import com.chingo247.structureapi.exeption.StructurePlanException;
import com.chingo247.structureapi.placement.IPlacement;
import com.chingo247.structureapi.placement.block.SchematicPlacement;
import com.chingo247.structureapi.schematic.FastClipboard;
import com.chingo247.structureapi.platform.permission.Permissions;
import com.chingo247.xplatform.core.IColors;
import com.chingo247.xplatform.core.ICommandSender;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.CommandUsageException;
import com.sk89q.worldedit.entity.Player;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

/**
 *
 * @author Chingo
 */
public class SchematicCommands {
    
    public static final String SCHEMATICS_UPDATE_DIR = "update/schematics";
    
    @CommandPermissions({Permissions.CONTENT_ROTATE_PLACEMENT})
    @CommandExtras(async = true)
    @Command(aliases = {"schematic:rotate"}, usage = "/schematic:rotate [structure-id][degrees]",desc = "Rotates a schematic of a structure, only affects future structures with the same schematic", min = 2, max = 2)
    public static void schematicRotate(final CommandContext args, ICommandSender sender, IStructureAPI structureAPI) throws CommandException {
        final GraphDatabaseService graph = SettlerCraft.getInstance().getNeo4j();
        final IStructureRepository structureRepository = new StructureRepository(graph);
        final IColors COLOR = SettlerCraft.getInstance().getPlatform().getChatColors();
        final String usage = "/schematic:rotate [structure-id][degrees]";
        
        String idArg = args.getString(0);
        String degreeArg = args.getString(1);
        
        long structureId;
        try {
            structureId = Long.parseLong(idArg);
        } catch (NumberFormatException nfe) {
            throw new CommandUsageException("Expected a number for [structure-id] but got '" + idArg + "'", usage);
        }
        
        int degrees;
        try {
            degrees = Integer.parseInt(degreeArg);
        } catch (NumberFormatException nfe) {
            throw new CommandUsageException("Expected a number for [degrees] but got '" + degreeArg + "'", usage);
        }
        
        if(degrees % 90 != 0) {
            throw new CommandException( "Argument [degrees] must be a multiple of 90");
        }

        Structure structure = null;
        try (Transaction tx = graph.beginTx()) {
            StructureNode n = structureRepository.findById(structureId);
            if (n == null) {
                tx.success();
                throw new CommandException("unable to find structure with id #" + structureId);
            }
            structure = new Structure(n);
            tx.success();
        }

        IStructurePlan plan = null;
        try {
            plan = structure.getStructurePlan();
        } catch (StructurePlanException ex) {
            throw new CommandException(ex);
        }
        IPlacement placement = plan.getPlacement();

        if(!(placement instanceof SchematicPlacement)) {
            throw new CommandException("Placement type of structure #" + structureId + " is not a schematic");
        }

        SchematicPlacement schematicPlacement = (SchematicPlacement) placement;
        StructurePlanManager spm = StructurePlanManager.getInstance();
        List<IStructurePlan> plans = spm.getPlans();
        List<File> matching = Lists.newArrayList();
        Set<String> done = Sets.newHashSet();
        long hash = schematicPlacement.getSchematic().getHash();
        
        for (IStructurePlan p : plans) {
            if (p.getPlacement() instanceof SchematicPlacement) {
                SchematicPlacement sp = (SchematicPlacement) p.getPlacement();
                File nextSchematicFile = sp.getSchematic().getFile();
                if (sp.getSchematic().getHash() == hash) {
                    if (!done.contains(nextSchematicFile.getAbsolutePath())) {
                        matching.add(nextSchematicFile);
                        try {
                            FastClipboard.rotateAndWrite(nextSchematicFile, degrees);
                        } catch (IOException ex) {
                            if (sender instanceof Player) {
                                sender.sendMessage("Something went wrong during rotation... See console (if possible)");
                            }
                            Logger.getLogger(SchematicCommands.class.getName()).log(Level.SEVERE, null, ex);
                            return;
                        }
                    }
                    spm.reload(p.getId()); // Reload placement
                }

            }
        }

        if (matching.isEmpty()) {
            throw new CommandException("Couldn't find plan for structure #" + structureId);
        } else if (matching.size() == 1) {
            sender.sendMessage(COLOR.white() + "Rotated '" + COLOR.blue() + matching.get(0).getName() + COLOR.reset() + "' by " + degrees + " degrees");
        } else {
            String[] rotatedPlans = new String[matching.size() + 1];
            for (int i = 0; i < rotatedPlans.length; i++) {
                if (i == 0) {
                    rotatedPlans[i] = "The schematics of the following plans have been rotated:";
                } else {
                    rotatedPlans[i] = COLOR.blue() + matching.get(i - 1).getName() + COLOR.reset();
                }
            }
            sender.sendMessage(rotatedPlans);
        }
        
    }
    
}
