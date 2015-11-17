package com.chingo247.structurecraft.bukkit.selection;

///*
// * Copyright (C) 2015 Chingo
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program.  If not, see <http://www.gnu.org/licenses/>.
// */
//package com.chingo247.structurecraft.platform.bukkit.selection;
//
//import com.chingo247.settlercraft.core.Direction;
//import com.chingo247.settlercraft.core.SettlerCraft;
//import com.chingo247.structurecraft.platform.services.holograms.Hologram;
//import com.chingo247.structurecraft.select.ASelectionManager;
//import com.chingo247.structurecraft.select.Selection;
//import com.chingo247.structurecraft.platform.services.holograms.HologramsProvider;
//import com.chingo247.structurecraft.util.WorldUtil;
//import com.chingo247.xplatform.core.APlatform;
//import com.chingo247.xplatform.core.IColors;
//import com.chingo247.xplatform.core.IPlugin;
//import com.chingo247.xplatform.core.IScheduler;
//import com.sk89q.worldedit.Vector;
//import com.sk89q.worldedit.entity.Player;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Objects;
//import java.util.UUID;
//import org.bukkit.Bukkit;
//
///**
// *
// * @author Chingo
// */
//public class HologramSelectionManager extends ASelectionManager {
//    
//    private HologramsProvider hologramsProvider;
//    private static HologramSelectionManager instance;
//    private final Map<UUID, HoloSelection> selections = Collections.synchronizedMap(new HashMap<UUID, HoloSelection>());
//    
//    private HologramSelectionManager() {
//    }
//    
//    public static HologramSelectionManager getInstance() {
//        if(instance == null) {
//            instance = new HologramSelectionManager();
//        }
//        return instance;
//    }
//
//    public HologramsProvider getHologramsProvider() {
//        return hologramsProvider;
//    }
//    
//    public void setHologramsProvider(HologramsProvider hologramsProvider) {
//        this.hologramsProvider = hologramsProvider;
//    }
//    
//   
//    
//    public boolean hasHologramsProvider() {
//        return hologramsProvider != null;
//    }
//    
//
//    @Override
//    public void select(UUID player, Vector pos1, Vector pos2) {
//        org.bukkit.entity.Player ply = Bukkit.getPlayer(player);
//        Direction direction = WorldUtil.getDirection(ply.getLocation().getYaw());
//        Vector pos = WorldUtil.translateLocation(pos1, direction, pos2.getBlockX(), pos2.getBlockY(), pos2.getBlockZ());
//        HoloSelection selection = new HoloSelection(ply, pos1, pos2, direction, pos.getBlockX() < 0);
//        putSelection(selection);
//    }
//
//    @Override
//    public void deselect(UUID player) {
//        if(hasSelection(player)) {
//            HoloSelection selection = (HoloSelection) getSelection(player);
//            for(Hologram h : selection.holos) {
//                h.delete();
//            }
//            removeSelection(selection);
//        }
//    }
//
//   
//
//    private class HoloSelection extends Selection {
//
//
//        private final Hologram[] holos;
//
//        private static final int SELF = 0;
//        private static final int X_AXIS = 1;
//        private static final int Y_AXIS = 2;
//        private static final int Z_AXIS = 3;
//
//        public HoloSelection(final Player whoCanSee, final Vector pos1, final Vector pos2, Direction direction, boolean reverse) {
//            super(whoCanSee.getUniqueId(), pos1, pos2);
//            this.holos = new Hologram[4];
//
//            final Vector self;
//            switch (direction) {
//                case WEST:
//                    self = WorldUtil.translateLocation(pos1, direction, 0, 1.2, -0.5);
//                    break;
//                case SOUTH:
//                    self = WorldUtil.translateLocation(pos1, direction, -0.5, 1.2, 0.5);
//                    break;
//                case NORTH:
//                    self = WorldUtil.translateLocation(pos1, direction, 0.5, 1.2, -0.5);
//                    break;
//                case EAST:
//                    self = WorldUtil.translateLocation(pos1, direction, 0.5,  1.2, 0.5);
//                    break;
//                default:
//                    return;
//            }
//
//            final Vector xLoc;
//            if (reverse) {
//                xLoc = WorldUtil.translateLocation(self, direction, 2, 1, 0);
//            } else {
//                xLoc = WorldUtil.translateLocation(self, direction, -2, 1, 0);
//            }
//            final Vector yLoc = WorldUtil.translateLocation(self, direction, 0, 1, 0);
//            final Vector zLoc = WorldUtil.translateLocation(self, direction, 0, 1, 2);
//
//            final APlatform platform = SettlerCraft.getInstance().getPlatform();
//            final IPlugin plugin = SettlerCraft.getInstance().getPlugin();
//            final IScheduler scheduler = platform.getServer().getScheduler(plugin);
//            final IColors colors = platform.getChatColors();
//            
//            scheduler.runSync(new Runnable() {
//
//                @Override
//                public void run() {
//                    // BoxHolo
//                    Hologram boxHolo = hologramsProvider.createHologram(
//                            plugin.getName(), 
//                            whoCanSee.getWorld(), 
//                            new Vector(self.getX(), self.getY(), self.getZ())
//                    );
//                    boxHolo.addLine(colors.green() + "[X]");
//                    holos[SELF] = boxHolo;
//                    
//                    // X-AXIS Holo
//                    Hologram xAxisHolo = hologramsProvider.createHologram(
//                            plugin.getName(), 
//                            whoCanSee.getWorld(), 
//                            new Vector(xLoc.getX(), xLoc.getY(), xLoc.getZ())
//                    );
//                    xAxisHolo.addLine(colors.yellow() + "X-AXIS" + colors.reset() + "+" + Math.abs(pos2.subtract(pos1).getX()));
//                    holos[X_AXIS] = xAxisHolo;
////                           
//                    // Y-AXIS Holo
//                    Hologram yAxisHolo = hologramsProvider.createHologram(
//                            plugin.getName(), 
//                            whoCanSee.getWorld(), 
//                            new Vector(yLoc.getX(), yLoc.getY(), yLoc.getZ())
//                    );
//                    yAxisHolo.addLine(colors.yellow() + "Y-AXIS" + colors.reset() + "+" + Math.abs(pos2.subtract(pos1).getY()));
//                    holos[Y_AXIS] = yAxisHolo;
//                    
//                    // Z-AXIS Holo
//                    Hologram zAxisHolo = hologramsProvider.createHologram(
//                            plugin.getName(), 
//                            whoCanSee.getWorld(), 
//                            new Vector(zLoc.getX(), zLoc.getY(), zLoc.getZ())
//                    );
//                    zAxisHolo.addLine(colors.yellow() + "Z-AXIS" + colors.reset() + "+" + Math.abs(pos2.subtract(pos1).getZ()));
//                    holos[Z_AXIS] = zAxisHolo;
//                }
//            });
//            
//            
//
//        }
//
//             @Override
//    public int hashCode() {
//        int hash = 5;
//        hash = 47 * hash + Objects.hashCode(this.start);
//        hash = 47 * hash + Objects.hashCode(this.end);
//        return hash;
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        if (obj == null) {
//            return false;
//        }
//        if (getClass() != obj.getClass()) {
//            return false;
//        }
//        final Selection other = (Selection) obj;
//        if (!Objects.equals(this.start, other.getStart())) {
//            return false;
//        }
//        if (!Objects.equals(this.end, other.getEnd())) {
//            return false;
//        }
//        return true;
//    }
//        
//        
//
//        private void clear() {
//            final APlatform platform = SettlerCraft.getInstance().getPlatform();
//            final IPlugin plugin = SettlerCraft.getInstance().getPlugin();
//            final IScheduler scheduler = platform.getServer().getScheduler(plugin);
//            
//            scheduler.runSync(new Runnable() {
//
//                @Override
//                public void run() {
//                    for (Hologram holo : holos) {
//                        if(holo != null) {
//                            holo.delete();
//                        }
//                    }
//                }
//            });
//        }
//
//    }
//    
//    public void clearAll() {
//        for(HoloSelection s : selections.values()) {
//            for(Hologram h : s.holos) {
//                h.delete();
//            }
//        }
//    }
//    
//}
