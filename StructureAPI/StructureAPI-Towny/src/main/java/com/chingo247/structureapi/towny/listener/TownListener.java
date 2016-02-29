/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.towny.listener;

import com.chingo247.structureapi.towny.plugin.StructureAPITowny;
import com.chingo247.structureapi.model.structure.StructureRepository;
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
    private final StructureRepository structureRepository;

    public TownListener(GraphDatabaseService graph, ExecutorService executor) {
        this.graph = graph;
        this.structureRepository = new StructureRepository(graph);
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
}
