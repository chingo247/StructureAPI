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
package com.chingo247.structurecraft.plan;

import com.chingo247.structurecraft.plan.io.StructurePlanReader;
import com.chingo247.structurecraft.StructureAPI;
import com.chingo247.structurecraft.event.structure.plan.StructurePlansLoadedEvent;
import com.chingo247.structurecraft.event.structure.plan.StructurePlansReloadEvent;
import com.chingo247.structurecraft.plan.io.exception.PlanException;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * StructurePlan manager is a storage class for StructurePlans. All methods
 * within this class are synchronised and can thus be called from multiple
 * threads
 *
 * @author Chingo
 */
public class StructurePlanManager implements IStructurePlanProvider{

    
    private final Map<String, IStructurePlan> plans;
    private final String planDirectoryPath;
    private ForkJoinPool forkJoinPool;
    private final int parallelism;
    private final File planDirectory;
    private static StructurePlanManager instance;
    private Logger LOG = Logger.getLogger(StructurePlanManager.class.getName());

    private StructurePlanManager() {
        this.planDirectory = StructureAPI.getInstance().getPlanDirectory();
        this.plans = Maps.newHashMap();
        this.planDirectoryPath = planDirectory.getAbsolutePath();
        this.parallelism = Math.max(1, Runtime.getRuntime().availableProcessors() - 2); // Dont lag server on reload...
    }

    public static StructurePlanManager getInstance() {
        if (instance == null) {
            instance = new StructurePlanManager();
        }
        return instance;
    }
    
    @Override
    public void reload(String planId) {
        IStructurePlan plan = plans.get(planId);
        if(plan == null) {
            return;
        }
        
        StructurePlanReader reader = new StructurePlanReader();
        IStructurePlan newPlan = reader.readFile(plan.getFile());
        
        synchronized(plans) {
            plans.remove(plan.getId());
            plans.put(newPlan.getId(), newPlan);
        }
        
    }

    @Override
    public IStructurePlan getPlan(String planId) {
        synchronized(plans) {
            return plans.get(planId);
        }
    }

    public void putPlan(IStructurePlan plan) {
        synchronized (plans) {
            plans.put(plan.getId(), plan);
        }
    }

    @Override
    public void reload() {
        loadPlans(true);
    }

    public void loadPlans(final boolean verbose) {
        synchronized (plans) {
            plans.clear();
            
        }

        // Make dirs if not exist!
        planDirectory.mkdirs();

        StructureAPI.getInstance().getEventDispatcher().dispatchEvent(new StructurePlansReloadEvent());

        // If it isn't null and there are still processes running... terminate them
        if (forkJoinPool != null && !forkJoinPool.isShutdown()) {
            forkJoinPool.shutdown();
        }
        
        forkJoinPool = new ForkJoinPool(parallelism);
        StructureAPI.getInstance().getExecutor().submit(new Runnable() {

            @Override
            public void run() {
                try {
                    StructurePlanReader reader = new StructurePlanReader();
                    List<IStructurePlan> plansList = reader.readDirectory(planDirectory, verbose, forkJoinPool);
                    for (IStructurePlan plan : plansList) {
                        boolean exists = getPlan(plan.getId()) != null;
                        if (exists) {
                            continue; // it's exact the same plan...
                        }
                        putPlan(plan);
                    }
                    if (!forkJoinPool.isShutdown()) {
                        forkJoinPool.shutdown();
                    }
                    StructureAPI.getInstance().getEventDispatcher().dispatchEvent(new StructurePlansLoadedEvent());
                } catch (Exception ex) {
                    LOG.log(Level.SEVERE, ex.getMessage(), ex);
                }
            }
        });

    }

    protected File getPlanDirectory() {
        return new File(planDirectoryPath);
    }

    @Override
    public List<IStructurePlan> getPlans() {
        synchronized (plans) {
            return new ArrayList<>(plans.values());
        }
    }


    @Override
    public List<IStructurePlan> getPlans(UUID player) {
        return getPlans(); // Not relevant for this implementation
    }

    private class SearchReferencesTask extends RecursiveTask<Map<String, String>> {

        private final File searchDirectory;
        private final SAXReader reader;

        public SearchReferencesTask(File directory) {
            Preconditions.checkArgument(directory.isDirectory());
            this.searchDirectory = directory;
            this.reader = new SAXReader();
        }

        @Override
        protected Map<String, String> compute() {

            Map<String, String> result = Maps.newHashMap();

            try {

                List<SearchReferencesTask> tasks = new ArrayList<>();
                for (File f : searchDirectory.listFiles()) {

                    if (f.isDirectory()) {
                        SearchReferencesTask task = new SearchReferencesTask(f);
                        task.fork();
                        tasks.add(task);
                    } else {
                        if (!FilenameUtils.getExtension(f.getName()).equals("xml")) {
                            continue;
                        }
                        try {
                            if (!isStructurePlan(f)) {
                                continue;
                            }
                        } catch (DocumentException ex) {
                            Logger.getLogger(StructurePlanManager.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        Map.Entry<String, String> entry = getEntry(f);
                        if (entry != null) {
                            result.put(entry.getKey(), entry.getValue());
                        }
                    }
                }

                for (SearchReferencesTask task : tasks) {
                    Map<String, String> childReferences = task.get();
                    for (Map.Entry<String, String> s : childReferences.entrySet()) {
                        if (result.get(s.getKey()) == null) {
                            result.put(s.getKey(), s.getValue());
                        } else {
                            throw new PlanException("Duplicate id references! StructurePlanFile: " + s.getValue() + " and file " + result.get(s.getKey()) + " have the same id!");
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(StructurePlanManager.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }

            return result;
        }

        private Map.Entry<String, String> getEntry(File f) {
            try {
                Document d = reader.read(f);
                Element e = (Element) d.getRootElement().selectSingleNode("Id");

                if (e != null) {
                    String id = e.getText().trim();
                    if (id.isEmpty()) {
                        throw new PlanException("The <Id> tag of " + f.getAbsolutePath() + " is empty!");
                    }

                    return Maps.immutableEntry(id, f.getAbsolutePath());
                }
            } catch (DocumentException ex) {
                Logger.getLogger(StructurePlanManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        }

    }

    private boolean isStructurePlan(File file) throws DocumentException {
        SAXReader reader = new SAXReader();
        Document d = reader.read(file);
        return d.getRootElement().getName().equals("StructurePlan");
    }

}
