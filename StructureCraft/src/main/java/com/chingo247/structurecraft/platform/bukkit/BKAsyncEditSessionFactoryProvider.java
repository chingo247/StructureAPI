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
package com.chingo247.structurecraft.platform.bukkit;

import com.chingo247.structurecraft.platform.services.AsyncEditSessionFactoryProvider;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import org.primesoft.asyncworldedit.AsyncWorldEditMain;
import org.primesoft.asyncworldedit.worldedit.AsyncEditSessionFactory;

/**
 *
 * @author Chingo
 */
class BKAsyncEditSessionFactoryProvider implements AsyncEditSessionFactoryProvider {
    
    private WorldEditPlugin plugin;
    private AsyncEditSessionFactory factory;

    public BKAsyncEditSessionFactoryProvider() {
        this.plugin = AsyncWorldEditMain.getWorldEdit(AsyncWorldEditMain.getInstance());
        this.factory = new AsyncEditSessionFactory(plugin, AsyncWorldEditMain.getInstance(), WorldEdit.getInstance().getEventBus());
    }
    
    

    @Override
    public AsyncEditSessionFactory getFactory() {
        return factory;
    }
    
    
    
}
