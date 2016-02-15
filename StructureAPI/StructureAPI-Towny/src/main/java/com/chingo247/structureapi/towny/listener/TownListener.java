/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.towny.listener;

import com.chingo247.structureapi.event.structure.StructureCreateEvent;
import com.chingo247.structureapi.model.structure.IStructure;
import com.chingo247.structureapi.towny.plugin.StructureAPITowny;
import com.chingo247.structureapi.model.structure.IStructureRepository;
import com.chingo247.structureapi.model.structure.Structure;
import com.chingo247.structureapi.model.structure.StructureNode;
import com.chingo247.structureapi.model.structure.StructureRepository;
import com.chingo247.structureapi.model.world.StructureWorldRepository;
import com.chingo247.structureapi.towny.restriction.TownyRestriction;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.event.NewTownEvent;
import com.palmergames.bukkit.towny.event.TownClaimEvent;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

/**
 *
 * @author Chingo
 */
public class TownListener implements Listener {

    private final GraphDatabaseService graph;
    private final ExecutorService executor;
    private final StructureWorldRepository worldRepository;
    private final IStructureRepository structureRepository;

    public TownListener(GraphDatabaseService graph, ExecutorService executor) {
        this.graph = graph;
        this.executor = executor;
        this.structureRepository = new StructureRepository(graph);
        this.worldRepository = new StructureWorldRepository(graph);
    }

    @Subscribe
    public void onStructureCreate(StructureCreateEvent createEvent) {
        IStructure structure = createEvent.getStructure();
        if(structure != null) {
            World w = Bukkit.getServer().getWorld(structure.getWorldUUID());
            List<WorldCoord> coords = TownyRestriction.getCoords(w, structure.getCuboidRegion());
            Town t = null;
            for(WorldCoord coord :coords) {
                TownBlock tb = null;
                try {
                    tb = coord.getTownBlock();
                } catch (NotRegisteredException ex) {
                }
                
                if(tb != null) {
                    try {
                        t = tb.getTown();
                    } catch (NotRegisteredException ex) {
                    }
                    if(t != null) {
                        break;
                    }
                }
            }
            
            if(t == null) {
                return;
            }
            
            try(Transaction tx = graph.beginTx()) {
                StructureNode node = new StructureNode(createEvent.getStructure().getUnderlyingNode());
                node.getNode().setProperty("TownyTown", t.getName());
                tx.success();
            }
        }
    }

    @EventHandler
    public void onTownCreate(NewTownEvent townEvent) {
        Town t = null;
        TownBlock tb = null;
        try {
            t = townEvent.getTown();
            tb = t.getHomeBlock();
        } catch (TownyException ex) {
            Logger.getLogger(TownListener.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Player player = null;
        
        boolean hasStructures = false;
        if(tb != null && t != null) {
            player = Bukkit.getPlayer(t.getMayor().getName());
            int blockSize = TownySettings.getTownBlockSize();
            World w = Bukkit.getWorld(t.getWorld().getName());
            
            try (Transaction tx = graph.beginTx()){
                WorldCoord coord = tb.getWorldCoord();

                Vector2D pos = StructureAPITowny.translate(coord);

                Vector min = new BlockVector(pos.getX(), 0, pos.getZ());
                Vector max = new BlockVector(pos.getX() + blockSize, 128, pos.getZ() + blockSize);

                hasStructures = structureRepository.hasStructuresWithin(w.getUID(), new CuboidRegion(min, max));
                tx.success();
            }
            
            if(hasStructures) {
                    TownyUniverse.getDataSource().removeTown(t);
                if(player != null) {
                    player.sendMessage(ChatColor.RED + "Removed town '" + t.getName() + "' because it was placed inside a structure");
                }
            }
        }
        
    }
    
    @EventHandler
    public void onTownClaim(TownClaimEvent claimEvent) {
        Town t = null;
        try {
            t = claimEvent.getTownBlock().getTown();
        } catch(NotRegisteredException nre) {
        }
        
        Player player = null;
        
        boolean hasStructures = false;
        if(t != null) {
            player = Bukkit.getPlayer(t.getMayor().getName());
            int blockSize = TownySettings.getTownBlockSize();
            World w = Bukkit.getWorld(t.getWorld().getName());
            
            try (Transaction tx = graph.beginTx()){
                TownBlock tb = claimEvent.getTownBlock();
                WorldCoord coord = tb.getWorldCoord();

                Vector2D pos = StructureAPITowny.translate(coord);

                Vector min = new BlockVector(pos.getX(), 0, pos.getZ());
                Vector max = new BlockVector(pos.getX() + blockSize, 128, pos.getZ() + blockSize);
            
                hasStructures = structureRepository.hasStructuresWithin(w.getUID(), new CuboidRegion(min, max));
                tx.success();
            }
            
            if(hasStructures) {
                TownyWorld tw = claimEvent.getTownBlock().getWorld();
                tw.removeTownBlock(claimEvent.getTownBlock());
                if(player != null) {
                    player.sendMessage(ChatColor.RED + "Plot has been removed, plots may not overlap structures...");
                }
            }
            
        }
        
        
        
        
        
                
            
    }
  
//    @EventHandler
//    public void onTownDelete(TownUnclaimEvent unclaimEvent) {
//        removeStructuresFromCell(unclaimEvent.getWorldCoord() , ", because plot has been unclaimed!");
//    }
//
//    private void removeStructuresFromCell(WorldCoord t, final String reason) {
//        int blockSize = TownySettings.getTownBlockSize();
//        Vector2D vector = SettlerCraftTowny.translate(t);
//
//        Vector min = new Vector(vector.getBlockX(), 0, vector.getBlockZ());
//        Vector max = min.add(blockSize, 256, blockSize);
//
//        final CuboidRegion region = new CuboidRegion(min, max);
//        final World w = Bukkit.getWorld(t.getWorldName());
//
//        executor.execute(new Runnable() {
//
//            @Override
//            public void run() {
//                try (Transaction tx = graph.beginTx()) {
//                    IStructureWorld sw = worldRepository.findByUUID(w.getUID());
//                    List<StructureNode> sns = sw.getStructuresWithin(region, -1);
//
//
//                    for (StructureNode sn : sns) {
//                        if (sn.getStatus() != StructureStatus.REMOVED) {
//                            sn.setStatus(StructureStatus.REMOVED);
//                            for (StructureOwnerNode owner : sn.getOwners()) {
//                                Player player = Bukkit.getPlayer(owner.getUUID());
//                                player.sendMessage("Structure " + sn.getName() + " has been removed, " + reason);
//                            }
//                        }
//                    }
//
//                    tx.success();
//                }
//            }
//        });
//    }

}
