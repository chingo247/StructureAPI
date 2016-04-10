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
package com.chingo247.structureapi.schematic;

import com.chingo247.settlercraft.core.SettlerCraft;
import com.chingo247.settlercraft.core.util.XXHasher;
import com.chingo247.structureapi.model.schematic.SchematicDataNode;
import com.chingo247.structureapi.model.schematic.SchematicRepository;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.google.common.collect.Maps;
import java.util.Collection;
import org.apache.commons.io.FileUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

/**
 *
 * @author Chingo
 */
public class SchematicManager {

//    private final SCLogger LOG = new SCLogger();
    private final Map<Long, Schematic> schematics;
    private static SchematicManager instance;
    private final SchematicRepository schematicRepository;
    private final GraphDatabaseService graph;
    private final long TWO_DAYS = 1000 * 60 * 60 * 24 * 2;

    private SchematicManager() {
        this.graph = SettlerCraft.getInstance().getNeo4j();
        this.schematicRepository = new SchematicRepository(graph);
        this.schematics = Collections.synchronizedMap(new HashMap<Long, Schematic>());
    }

    public static SchematicManager getInstance() {
        if (instance == null) {
            instance = new SchematicManager();
        }
        return instance;
    }

    /**
     * Will attempt to get the schematic from the cache. Otherwise the schematic
     * will be loaded and returned
     *
     * @param schematicFile The schematicFile
     * @return The schematic
     */
    public Schematic getOrLoadSchematic(File schematicFile) {
        try {
            XXHasher hasher = new XXHasher();
            long checksum = hasher.hash64(schematicFile);
            Schematic schematic = getSchematic(checksum);
            if (schematic == null) {
                FastClipboard clipboard = FastClipboard.read(schematicFile);
                schematic = new DefaultSchematic(schematicFile, clipboard.getWidth(), clipboard.getHeight(), clipboard.getLength(), clipboard.getyAxisOffset());
                schematics.put(checksum, schematic);
                clipboard = null;
            }
            return schematic;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

    }

    public synchronized Schematic getSchematic(Long checksum) {
        return schematics.get(checksum);
    }

    public synchronized void load(File directory) {
        Preconditions.checkArgument(directory.isDirectory());
        
        Iterator<File> fit = FileUtils.iterateFiles(directory, new String[]{"schematic"}, true);
        
        if (fit.hasNext()) {
            ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors()); // only create the pool if we have schematics
            Map<Long, SchematicDataNode> alreadyHere = Maps.newHashMap();
            Map<Long, SchematicDataNode> needsUpdating = Maps.newHashMap();
            
            List<SchematicProcessor> tasks = Lists.newArrayList();
            List<Schematic> alreadyDone = Lists.newArrayList();
            XXHasher hasher = new XXHasher();

            try (Transaction tx = graph.beginTx()) {
                Collection<SchematicDataNode> schematicNodes = schematicRepository.findAfterDate(System.currentTimeMillis() - TWO_DAYS);
                for (SchematicDataNode node : schematicNodes) {
                    if(!node.hasRotation()) {
                        needsUpdating.put(node.getXXHash64(), node);
                        continue;
                    }
                    alreadyHere.put(node.getXXHash64(), node);
                }

                // Process the schematics that need to be loaded
                while (fit.hasNext()) {
                    File schematicFile = fit.next();
                    try {
                        long checksum = hasher.hash64(schematicFile);
                        // Only load schematic data that wasn't yet loaded...
                        SchematicDataNode existingData = alreadyHere.get(checksum);
                        if (existingData != null) {
                            Schematic s = new DefaultSchematic(schematicFile, existingData.getWidth(), existingData.getHeight(), existingData.getLength(), existingData.getAxisOffset());
                            alreadyDone.add(s);
                        } else if (getSchematic(checksum) == null) {
                            SchematicProcessor processor = new SchematicProcessor(schematicFile);
                            tasks.add(processor);
                            pool.execute(processor);
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(SchematicManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                tx.success();
            }

            // Wait for the processes the finish and queue them for bulk insert
            List<Schematic> newSchematics = Lists.newArrayList();
            try {
                for (SchematicProcessor sp : tasks) {
                    Schematic schematic = sp.get();
                    if (schematic != null) {
                        newSchematics.add(schematic);
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(SchematicManager.class.getName()).log(Level.SEVERE, null, ex);
            }

            // Close the pool!
            pool.shutdown();

            int updated = 0;
            // Update the database
            try (Transaction tx = graph.beginTx()) {
                for (Schematic data : alreadyDone) {
                    SchematicDataNode sdn = schematicRepository.findByHash(data.getHash());
                    sdn.setLastImport(System.currentTimeMillis());
                }
                for (Schematic newData : newSchematics) {
                    if(needsUpdating.get(newData.getHash()) != null) {
                        SchematicDataNode dataNode = schematicRepository.findByHash(newData.getHash());
                        dataNode.setRotation(newData.getAxisOffset());
                        updated++;
                        continue;
                    } 
                    String name = newData.getFile().getName();
                    long xxhash = newData.getHash();
                    int width = newData.getWidth();
                    int height = newData.getHeight();
                    int length = newData.getLength();
                    int axisOffset = newData.getAxisOffset();
                    schematicRepository.addSchematic(name, xxhash, width, height, length, axisOffset, System.currentTimeMillis());
                }

                // Delete unused
                int removed = 0;
                for (SchematicDataNode sdn : schematicRepository.findBeforeDate(System.currentTimeMillis() - TWO_DAYS)) {
                    sdn.delete();
                    removed++;
                }
                if (removed > 0) {
                    System.out.println("[SettlerCraft]: Deleted " + removed + " schematic(s) from cache");
                }
                
                if (updated > 0) {
                    System.out.println("[SettlerCraft]: Updated " + updated + " schematic(s) from cache");
                }

                tx.success();
            }
            

            synchronized (schematics) {
                for (Schematic schematic : newSchematics) {
                    schematics.put(schematic.getHash(), schematic);
                }
                for (Schematic schematic : alreadyDone) {
                    schematics.put(schematic.getHash(), schematic);
                }
            }

        }

    }

    private class SchematicProcessor extends RecursiveTask<Schematic> {

        private final File schematicFile;

        public SchematicProcessor(File schematic) {
            Preconditions.checkNotNull(schematic);
            Preconditions.checkArgument(schematic.exists());
            this.schematicFile = schematic;
        }

        @Override
        protected Schematic compute() {
            try {
                FastClipboard clipboard = FastClipboard.read(schematicFile);
                DefaultSchematic schematic = new DefaultSchematic(schematicFile, clipboard.getWidth(), clipboard.getHeight(), clipboard.getLength(), clipboard.getyAxisOffset());
                return schematic;
            } catch (Exception ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
            return null;
        }

    }

}
