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
package com.chingo247.settlercraft.holographicdisplays.bukkit.plugin;

import com.chingo247.settlercraft.core.SettlerCraft;
import com.chingo247.structurecraft.platform.services.holograms.Hologram;
import com.chingo247.structurecraft.platform.services.holograms.HologramsProvider;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.world.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author Chingo
 */
class HolographicDisplaysHologramProvider implements HologramsProvider {

    @Override
    public String getName() {
        return "HolographicDisplays";
    }

    @Override
    public Hologram createHologram(String plugin, String world, Vector position) {
        Plugin p = Bukkit.getPluginManager().getPlugin(plugin);
        if (p == null) {
            throw new RuntimeException("Can't find plugin '" + plugin + "'");
        }

        org.bukkit.World w = Bukkit.getWorld(world);
        com.gmail.filoghost.holographicdisplays.api.Hologram holo = HologramsAPI.createHologram(p, new Location(w, position.getX(), position.getY(), position.getZ()));
        return new HolographicDisplaysHologram(holo);
    }

    private class HolographicDisplaysHologram implements Hologram {

        private final com.gmail.filoghost.holographicdisplays.api.Hologram holo;

        public HolographicDisplaysHologram(com.gmail.filoghost.holographicdisplays.api.Hologram holo) {
            this.holo = holo;
        }

        @Override
        public void insertLine(int i, String s) {
            holo.insertTextLine(i, s);
        }

        @Override
        public void addLine(String s) {
            holo.appendTextLine(s);
        }

        @Override
        public void removeLine(int i) {
            holo.removeLine(i);
        }

        @Override
        public Vector getPosition() {
            Location location = holo.getLocation();
            return new Vector(location.getX(), location.getY(), location.getZ());
        }

        @Override
        public World getWorld() {
            return SettlerCraft.getInstance().getWorld(holo.getWorld().getName());
        }

        @Override
        public void delete() {
            if(!holo.isDeleted()) {
                holo.delete();
            }
        }

    }

}
