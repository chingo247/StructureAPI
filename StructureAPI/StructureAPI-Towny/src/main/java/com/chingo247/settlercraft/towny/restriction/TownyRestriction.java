/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.settlercraft.towny.restriction;

import com.chingo247.settlercraft.towny.plugin.SettlerCraftTowny;
import com.chingo247.structureapi.StructureRestriction;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.World;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;

/**
 *
 * @author Chingo
 */
public class TownyRestriction extends StructureRestriction {


    public TownyRestriction() {
        super("Towny", "towny.structure.restriction", null);
    }
    
    @Override
    public boolean evaluate(Player whoPlaces, World world, CuboidRegion affectedArea) {
        List<WorldCoord> coords = getCoords(Bukkit.getWorld(world.getName()), affectedArea);

        Resident whoPlacesResident = getResidentFor(whoPlaces);
        Iterator<WorldCoord> coordIt = coords.iterator();

        while (coordIt.hasNext()) {
            WorldCoord nextCoord = coordIt.next();
            TownBlock tb = getTownBlock(nextCoord);
            Resident resident = getResident(tb);

            if (tb != null) {
                Town t = getTown(tb);
                
                // Not a resident of this town! (not really needed as per case below, but improves feedback)
                if(t != null && !isResidentOf(t, whoPlacesResident)) {
                    setMessage("You are not a resident of '" + t.getName() + "'");
                    return false;
                }
                
                // Overlaps unowned! 
                if(resident != null && !isOwnedBy(tb, whoPlacesResident)) {
                    setMessage("Structure overlaps plots you don't own");
                    return false;
                } else if(resident == null && t != null && !isMayorOf(t, resident)) {
                    setMessage("Structure overlaps plots you don't own");
                    return false;
                }
               
            } else {

                // If not a mayor you are not allowed to place outside of towns
                if (whoPlacesResident == null || !whoPlacesResident.isMayor()) {
                    setMessage("You are not a mayor and are not allowed to place outside of towns");
                    return false;
                }
            }
        }

        return true;
    }
    
    private boolean isMayorOf(Town t, Resident r) {
        if(t == null) {
            return false;
        }
        
        if(r == null) {
            return false;
        }
        return t.getMayor().getName().equals(r.getName());
    } 
    
    private boolean isOwnedBy(TownBlock tb, Resident whoPlaces) {
        if(whoPlaces == null) {
            return false;
        }
        try {
            return tb.getResident().getName().equals(whoPlaces.getName());
        } catch (NotRegisteredException ex) {
            return false;
        }
    }
    
    private boolean isResidentOf(Town t, Resident resident) {
        if(resident == null) {
            return false;
        }
        return t.hasResident(resident);
    }
    
    private Town getTown(TownBlock townBlock) {
        if(townBlock == null) {
            return null;
        }
        
        try {
            return townBlock.getTown();
        } catch(NotRegisteredException ne) {
            return null;
        }
    }
    
    private Resident getResident(TownBlock townBlock) {
        if(townBlock == null) {
            return null;
        }
        try {
            return townBlock.getResident();
        } catch(NotRegisteredException ne) {
            return null;
        }
    }
    
    private TownBlock getTownBlock(WorldCoord coord) {
        try {
            return coord.getTownBlock();
        } catch (NotRegisteredException ex) {
            return null;
        }
    } 
    
    private Resident getResidentFor(Player player) {
        try {
            return TownyUniverse.getDataSource().getResident(player.getName());
        } catch (NotRegisteredException ex) {
            return null;
        }
    }
    
    

    public static List<WorldCoord> getCoords(org.bukkit.World w, CuboidRegion region) {

        Vector minPoint = region.getMinimumPoint();
        Vector maxPoint = region.getMaximumPoint();

//        System.out.println("Before between: " + minPoint + " and " + maxPoint);

        int blockSize = TownySettings.getTownBlockSize();
        WorldCoord minCoord = WorldCoord.parseWorldCoord(new Location(w, minPoint.getBlockX(), 0, minPoint.getBlockZ()));
//        WorldCoord maxCoord = WorldCoord.parseWorldCoord(new Location(w, maxPoint.getBlockX(), 0, maxPoint.getBlockZ()));

        Vector2D min = SettlerCraftTowny.translate(minCoord);
//        Vector2D max = SettlerCraftTowny.translate(maxCoord);

        List<WorldCoord> coords = new ArrayList<>();
        for (int x = min.getBlockX(); x < maxPoint.getBlockX(); x += blockSize) {
            for (int z = min.getBlockZ(); z < maxPoint.getBlockZ(); z += blockSize) {
                WorldCoord wc = WorldCoord.parseWorldCoord(new Location(w, x, 0, z));
                Vector2D pos = SettlerCraftTowny.translate(wc);
//                System.out.println("Added (" + wc.getX() + ", " + wc.getZ() + ") VECTOR(" + pos.getBlockX() + "," + pos.getBlockZ() + ")");
                coords.add(wc);
            }
        }

        return coords;
    }

}
