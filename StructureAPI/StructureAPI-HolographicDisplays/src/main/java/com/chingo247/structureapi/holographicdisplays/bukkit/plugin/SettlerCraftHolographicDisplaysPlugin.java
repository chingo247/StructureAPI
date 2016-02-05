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
package com.chingo247.structureapi.holographicdisplays.bukkit.plugin;

import com.chingo247.structurecraft.platform.services.holograms.StructureHologramManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Chingo
 */
public class SettlerCraftHolographicDisplaysPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("HolographicDisplays") != null) {
            StructureHologramManager.getInstance().setHologramProvider(new HolographicDisplaysHologramProvider());
//            HologramSelectionManager.getInstance().setHologramsProvider(new HolographicDisplaysHologramProvider());
            Bukkit.getPluginManager().registerEvents(new shutdownListener(), this);
        } else {
            System.out.println("[SettlerCraft-HolographicDisplays]: Couldn't find HolographicDisplays, Disabling SettlerCraft-HolographicDisplays");
            this.setEnabled(false);
        }
    }

    private class shutdownListener implements Listener {

        @EventHandler(priority = EventPriority.HIGHEST) // Critical as it needs to be executed on shutdown
        public void onShutdown(PluginDisableEvent disableEvent) {
            if (disableEvent.getPlugin().getName().equals("HolographicDisplays")) {
                if (StructureHologramManager.getInstance().getHologramsProvider() != null
                        && StructureHologramManager.getInstance().getHologramsProvider().getName().equals("HolographicDisplays")) {
                    StructureHologramManager.getInstance().shutdown();
                }

//                if (HologramSelectionManager.getInstance().getHologramsProvider() != null
//                        && HologramSelectionManager.getInstance().getHologramsProvider().getName().equals("HolographicDisplays")) {
//                    HologramSelectionManager.getInstance().clearAll();
//                }
            }
        }

    }
}
