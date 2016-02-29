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
package com.chingo247.structureapi.model.structure;

import java.io.File;

/**
 *
 * @author Chingo
 */
public class RollbackData {
    
    public static final String ROLLBACK_DIRECTORY = "rollback";
    
    private Structure structure;

    RollbackData(Structure structure) {
        this.structure = structure;
    }

    public File getBlockStoreDirectory() {
        File rollbackDirectory = new File(structure.getDirectory(), ROLLBACK_DIRECTORY);
        rollbackDirectory.mkdirs();
        return rollbackDirectory;
    }
    
    public boolean hasBlockStore() {
        return new File(structure.getDirectory(), ROLLBACK_DIRECTORY).exists();
    }

   
    
}
