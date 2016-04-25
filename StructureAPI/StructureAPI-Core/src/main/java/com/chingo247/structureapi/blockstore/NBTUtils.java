/*
 * Copyright (C) 2016 Chingo
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
package com.chingo247.structureapi.blockstore;

import com.sk89q.jnbt.Tag;
import java.util.Map;

/**
 *
 * @author Chingo
 */
public class NBTUtils {
    
    /**
     * Get child tag of a NBT structure.
     *
     * @param items The parent tag map
     * @param key The name of the tag to get
     * @param expected The expected type of the tag
     * @return child tag casted to the expected type
     * @throws DataException if the tag does not exist or the tag is not of the
     * expected type
     */
    public static <T extends Tag> T getChildTag(Map<String, Tag> items, String key,
            Class<T> expected) {

        if (!items.containsKey(key)) {
            throw new RuntimeException("Snapshot file is missing a \"" + key + "\" tag");
        }
        Tag tag = items.get(key);
        if (!expected.isInstance(tag)) {
            throw new RuntimeException(
                    key + " tag is not of tag type " + expected.getName());
        }
        return expected.cast(tag);
    }
    
}
