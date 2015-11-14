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
package com.chingo247.structurecraft.construction;

import com.chingo247.structurecraft.model.structure.Structure;
import com.chingo247.structurecraft.construction.options.Options;
import com.chingo247.structurecraft.exeption.StructureException;
import com.sk89q.worldedit.EditSession;
import java.io.IOException;
import java.util.UUID;

/**
 *
 * @author Chingo
 */
public class DefaultDemolitionTaskAssigner implements ITaskAssigner {
    
    private DefaultStructureTaskFactory taskfactory;

    public DefaultDemolitionTaskAssigner() {
        this.taskfactory = new DefaultStructureTaskFactory();
    }

    @Override
    public void assignTasks(EditSession session, UUID player, ConstructionEntry entry, Options options) throws StructureException, IOException {
        Structure structure = entry.getStructure();
//        if(taskfactory.hasBackup(structure, "restore.snapshot")) {
//            System.out.println("[DemolitionTaskAssigner]: HAS BACKUP");
//            StructureTask task = taskfactory.restore(session, player, structure);
//            entry.addTask(task);
//        } else {
//            System.out.println("[DemolitionTaskAssigner]: NO BACKUP");
            entry.addTask(taskfactory.demolish(session, player, entry.getStructure(), options));
//        }
        
    }
    
}
