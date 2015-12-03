package com.chingo247.structurecraft.rollback.commands;

import com.chingo247.settlercraft.core.SettlerCraft;
import com.chingo247.settlercraft.core.commands.util.CommandExtras;
import com.chingo247.settlercraft.core.commands.util.PluginCommandManager;
import com.chingo247.structurecraft.IStructureAPI;
import com.chingo247.structurecraft.model.owner.OwnerType;
import com.chingo247.structurecraft.model.structure.Structure;
import com.chingo247.structurecraft.model.structure.StructureNode;
import com.chingo247.structurecraft.model.structure.StructureRepository;
import com.chingo247.structurecraft.platform.permission.Permissions;
import com.chingo247.structurecraft.rollback.StructureCraftRollback;
import com.chingo247.xplatform.core.ICommandSender;
import com.chingo247.xplatform.core.IPlayer;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
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


public class LogCommands {

    private static final UUID CONSOLE = UUID.randomUUID();
    private static final Logger LOG = Logger.getLogger(LogCommands.class.getName());
    private static final int MAX_LINES = 10;

    /**
     * Display all commands
     *
     * @param args
     * @param sender
     * @param structureAPI
     */
    public static void help(final CommandContext args, ICommandSender sender, IStructureAPI structureAPI, PluginCommandManager commandManager) {
    }

    @CommandPermissions(Permissions.STRUCTURE_CONSTRUCTION)
    @CommandExtras(async = true)
    @Command(aliases = {"structure:rollback", "stt:rollback"}, desc = "Rollback a structure", min = 1, max = 1, flags = "f")
    public static void restore(final CommandContext args, ICommandSender sender, IStructureAPI structureAPI) throws Exception {
        final GraphDatabaseService graph = SettlerCraft.getInstance().getNeo4j();
        final StructureRepository structureRepository = new StructureRepository(graph);
        final Structure structure;
        final UUID uuid = getUUID(sender);

        String structureIdArg = args.getString(0);
        if (!NumberUtils.isNumber(structureIdArg)) {
            throw new CommandException("Expected a number but got '" + structureIdArg + "' \n" + "/structure:rollback [id]");
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
        LOG.log(Level.INFO, "rollback in {0} ms", (System.currentTimeMillis() - start));

        // Use force?
        String force = args.hasFlag('f') ? args.getFlag('f') : null;
        final boolean useForce = force != null && (force.equals("t") || force.equals("true"));

        // Start demolition
        StructureCraftRollback.getInstance().getRestorePlanFactory()
                .newRestorePlan(structure)
                .setRestrictive(true)
                .setForced(useForce)
                .setPlayer(uuid)
                .execute();

    }

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

}
