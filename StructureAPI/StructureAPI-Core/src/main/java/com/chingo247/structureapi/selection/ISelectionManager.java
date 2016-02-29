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

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.entity.Player;
import java.util.UUID;

/**
 *
 * @author Chingo
 */
public interface ISelectionManager {

    void select(UUID player, Vector start, Vector end);

    void select(Player player, Vector start, Vector end);

    void deselect(UUID player);

    void deselect(Player player);

    boolean hasSelection(UUID player);

    boolean hasSelection(Player player);

    boolean matchesCurrentSelection(UUID player, Vector start, Vector end);

    boolean matchesCurrentSelection(Player player, Vector start, Vector end);

}
