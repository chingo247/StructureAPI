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
package com.chingo247.structurecraft.plan.io;

import com.chingo247.settlercraft.core.util.LogLevel;
import com.chingo247.settlercraft.core.util.SCLogger;
import com.chingo247.structurecraft.plan.io.exception.ElementValueException;
import com.chingo247.structurecraft.placement.interfaces.IPlacement;
import com.chingo247.structurecraft.plan.io.document.PlacementElement;
import com.chingo247.structurecraft.plan.io.document.LineElement;
import com.chingo247.structurecraft.plan.io.document.StructurePlanDocument;
import com.chingo247.structurecraft.plan.io.document.SubStructureElement;
import com.chingo247.structurecraft.plan.io.exception.PlanException;
import com.chingo247.structurecraft.schematic.SchematicManager;
import com.chingo247.structurecraft.plan.DefaultStructurePlan;
import com.chingo247.structurecraft.plan.DefaultSubstructuresPlan;
import com.chingo247.structurecraft.plan.interfaces.IStructurePlan;
import com.chingo247.structurecraft.plan.PlacementAPI;
import com.google.common.base.Preconditions;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import org.apache.commons.io.FileUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

/**
 *
 * @author Chingo
 */
public class StructurePlanReader {

    public List<IStructurePlan> readDirectory(File structurePlanDirectory) {
        return readDirectory(structurePlanDirectory, false, new ForkJoinPool());
    }

    public List<IStructurePlan> readDirectory(File structurePlanDirectory, boolean printstuff, ForkJoinPool pool) {
        Iterator<File> fit = FileUtils.iterateFiles(structurePlanDirectory, new String[]{"xml"}, true);
        SchematicManager sdm = SchematicManager.getInstance();
        sdm.load(structurePlanDirectory);


        Map<String, StructurePlanProcessor> processors = new HashMap<>();

        while (fit.hasNext()) {
            File structurePlanFile = fit.next();
            StructurePlanProcessor spp = new StructurePlanProcessor(structurePlanFile);
            processors.put(structurePlanFile.getAbsolutePath(), spp);
            pool.execute(spp);
        }

        List<IStructurePlan> plans = new ArrayList<>();
        try {

            for (StructurePlanProcessor spp : processors.values()) {
                IStructurePlan plan = spp.get();
                if (plan != null) {
                    plans.add(plan);
                }
            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.SEVERE, ex.getMessage(), ex);
        }

        
        return plans;
    }

    public IStructurePlan readFile(File structurePlanFile) {
        SchematicManager sdm = SchematicManager.getInstance();

        sdm.load(structurePlanFile.getParentFile());
        StructurePlanProcessor spp = new StructurePlanProcessor(structurePlanFile);
        return spp.compute();
    }

    /**
     *
     * @author Chingo
     */
    private class StructurePlanProcessor extends RecursiveTask<IStructurePlan> {

        private File structurePlanFile;
        private DefaultSubstructuresPlan parent;
        private final SCLogger LOG = SCLogger.getLogger();

        StructurePlanProcessor(File structurePlan) {
            this(structurePlan, null);
        }

        StructurePlanProcessor(File structurePlan, DefaultSubstructuresPlan parent) {
            Preconditions.checkNotNull(structurePlan);
            Preconditions.checkArgument(structurePlan.exists());
            this.structurePlanFile = structurePlan;
            this.parent = parent;
        }

        @Override
        protected IStructurePlan compute() {
            long start = System.currentTimeMillis();

            
            StructurePlanDocument planDocument = null;
            try {
                planDocument = StructurePlanDocument.read(structurePlanFile);
                planDocument.checkNotNull(StructurePlanXMLConstants.STRUCTURE_PLAN_PLACEMENT);

                String name = planDocument.getName();
                String id = planDocument.getReferenceId();
                double price = planDocument.getPrice();
                String description = planDocument.getDescription();
                String category = planDocument.getCategory();

                PlacementElement placementElement = planDocument.getPlacementElement();
                PlacementProcessor placementProcessor = new PlacementProcessor(structurePlanFile.getParentFile(), placementElement);
                placementProcessor.fork();

                if (planDocument.hasSubStructureElements()) {

                    DefaultSubstructuresPlan plan = new DefaultSubstructuresPlan(id, structurePlanFile, parent, placementProcessor.get());
                    plan.setName(name);
                    plan.setPrice(price);
                    plan.setDescription(description);
                    plan.setCategory(category);

                    // Get and Set subplaceables
                    List<SubStructureElement> substructureElements = planDocument.getSubStructureElements();
                    List<StructurePlanProcessor> spps = null;
                    List<PlacementProcessor> pps = null;
                    if (!substructureElements.isEmpty()) {
                        spps = new ArrayList<>();
                        pps = new ArrayList<>();

                        for (SubStructureElement subStructureElement : substructureElements) {
                            String t = subStructureElement.getType();

                            if (t.trim().toLowerCase().equals("embedded")) {
                                // Perform recursion check here!
                                // Fully check branch for matchin types!
                                File f = handleEmbeddedPlan(structurePlanFile, subStructureElement);
                                if (plan.matchesParentRecursively(f)) {
                                    throw new PlanException("Element <" + subStructureElement.getElementName() + "> on line " + subStructureElement.getLine()
                                            + " matches a plan in his branch!");
                                }
                                spps.add(new StructurePlanProcessor(f, parent));
                            } else {
                                pps.add(new PlacementProcessor(structurePlanFile.getParentFile(), placementElement));
                            }
                        }
                    }

                    // Fork the processes
                    if (pps != null) {
                        for (PlacementProcessor pp : pps) {
                            pp.fork();
                        }
                    }
                    if (spps != null) {
                        for (StructurePlanProcessor spp : spps) {
                            spp.fork();
                        }
                    }

                    // Collect the data
                    if (pps != null) {
                        for (PlacementProcessor pp : pps) {
                            plan.addPlacement(pp.get());
                        }
                    }
                    if (spps != null) {
                        for (StructurePlanProcessor spp : spps) {
                            plan.addStructurePlan(spp.get());
                        }
                    }

                    // Recursive process SubStructurePlans
                    if (parent == null) {
//                StructurePlanUtil.validate(plan);
                    }

//                    LOG.print(LogLevel.INFO, structurePlanFile, "StructurePlan", System.currentTimeMillis() - start);
                    return plan;

                } else {
                    DefaultStructurePlan plan = new DefaultStructurePlan(id, structurePlanFile, placementProcessor.get());
                    plan.setName(name);
                    plan.setPrice(price);
                    plan.setDescription(description);
                    plan.setCategory(category);
//                    LOG.print(LogLevel.INFO, structurePlanFile, "StructurePlan", System.currentTimeMillis() - start);
                    return plan;
                }

            } catch (ElementValueException ex) {
                LOG.print(LogLevel.ERROR, "Error in '" + structurePlanFile.getAbsolutePath() + "' >> " + ex.getMessage(), "StructurePlan", null);
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.SEVERE, ex.getMessage(), ex);
            }

            return null;
        }

        private File handleEmbeddedPlan(File structurePlan, SubStructureElement element) throws DocumentException, ElementValueException {
            element.checkNotNull("File");

            LineElement simpleElement = element.selectSingleElement("File");
            simpleElement.checkNotEmpty();

            File f = new File(structurePlan.getParent(), element.getTextValue());
            if (!f.exists()) {
                throw new PlanException("File reference '" + f.getAbsolutePath() + "' defined on line " + simpleElement.getLine() + " does not exist!");
            }
            if (!isStructurePlan(f)) {
                throw new PlanException("File is not a valid StructurePlan file");
            }
            return f;
        }

        private boolean isStructurePlan(File file) throws DocumentException {
            SAXReader reader = new SAXReader();
            Document d = reader.read(file);
            return d.getRootElement().getName().equals("StructurePlan");
        }

    }

    private class PlacementProcessor extends RecursiveTask<IPlacement> {

        private final PlacementElement placeableNode;
        private final File sourceDirectory;

        /**
         * Processes a Placeable
         *
         * @param sourceDirectory
         * @param placementNode
         */
        public PlacementProcessor(File sourceDirectory, PlacementElement placementNode) {
            this.placeableNode = placementNode;
            this.sourceDirectory = sourceDirectory;
        }

        @Override
        protected IPlacement compute() {
            IPlacement placement = PlacementAPI.getInstance().parse(placeableNode.getFile(), placeableNode.getElement().getDocument(), placeableNode);
            return placement;
        }

    }

}
