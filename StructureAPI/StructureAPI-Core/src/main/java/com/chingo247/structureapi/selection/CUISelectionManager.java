
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

package com.chingo247.structureapi.selection;

import com.chingo247.settlercraft.core.SettlerCraft;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.world.World;
import java.util.UUID;
import java.util.logging.Logger;

/**
 *
 * @author Chingo
 */
public class CUISelectionManager extends ASelectionManager {

    private final Logger log = Logger.getLogger(CUISelectionManager.class.getName());
    private static CUISelectionManager instance;
    
    private CUISelectionManager() {}
    
    public static CUISelectionManager getInstance() {
        if(instance == null) {
            instance = new CUISelectionManager();
        }
        return instance;
    }

    @Override
    public void select(Player player, Vector start, Vector end) {
        LocalSession session = WorldEdit.getInstance().getSession(player);
        
        Selection selection = new Selection(player.getUniqueId(), start, end);
        World world = SettlerCraft.getInstance().getWorld(player.getWorld().getName());
        putSelection(selection);
        
        session.getRegionSelector(world).selectPrimary(start, null);
        session.getRegionSelector(world).selectSecondary(end, null);
        session.dispatchCUISelection(player);
    }

    

    @Override
    public void select(UUID player, Vector start, Vector end) {
        select(SettlerCraft.getInstance().getPlayer(player), start, end);
    }

    @Override
    public void deselect(UUID ply) {
        Player player = SettlerCraft.getInstance().getPlayer(ply);
        LocalSession session = WorldEdit.getInstance().getSession(player);
        World world = SettlerCraft.getInstance().getWorld(player.getWorld().getName());
        if (session.getRegionSelector(world).isDefined()) {
            session.getRegionSelector(world).clear();
            session.dispatchCUISelection(player);
        }
        removeSelection(getSelection(player.getUniqueId()));
    }

    

    
    
}
