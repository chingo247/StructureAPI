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
package com.chingo247.structurecraft.model.structure;

import com.chingo247.settlercraft.core.Direction;
import com.chingo247.structurecraft.model.plot.Plot;
import com.chingo247.structurecraft.StructureAPI;
import com.chingo247.structurecraft.exeption.StructureException;
import com.chingo247.structurecraft.plan.IStructurePlan;
import com.chingo247.structurecraft.plan.io.StructurePlanReader;
import com.chingo247.structurecraft.util.WorldUtil;
import com.chingo247.xplatform.core.IWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import java.io.File;
import java.util.Date;
import org.neo4j.graphdb.Node;

/**
 * As opposed to the {@link StructureNode} this unmodifable structure has all it's properties loaded. 
 * None of the opertions of this class have to be executed within a transaction
 * @author Chingo
 */
public class Structure extends Plot implements IStructure {
    
    private static final String BACKUPS_DIR = "backups";
    
    private Long id;
    private String name;
    private Vector origin;
    private ConstructionStatus status;
    private double price;
    private Direction direction;
    
    private CuboidRegion cuboidRegion;
    private Date completedAt;
    private Date createdAt;
    private Date deletedAt;

    public Structure(Node structureNode) {
        this(new StructureNode(structureNode));
    }
    
    public Structure(StructureNode structure) {
        super(structure.getNode());
        this.id = structure.getId();
        this.name = structure.getName();
        this.origin = structure.getOrigin();
        this.status = structure.getStatus();
        this.price = structure.getPrice();
        this.cuboidRegion = structure.getCuboidRegion();
        this.direction = structure.getDirection();
        this.deletedAt = structure.getDeletedAt();
        this.createdAt = structure.getCreatedAt();
        this.completedAt = structure.getCompletedAt();
    }

  
    
    /**
     * Gets the id of the structure
     *
     * @return The id of the structure
     */
    @Override
    public Long getId() {
        return id;
    }

    /**
     * Gets the name of the structure
     *
     * @return
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Gets the origin of this Structure
     *
     * @return The position
     */
    @Override
    public Vector getOrigin() {
        return origin;
    }

    /**
     * The region this structure overlaps
     * @return The region
     */
    @Override
    public CuboidRegion getCuboidRegion() {
        return cuboidRegion;
    }

    /**
     * Gets the value/price of this structure
     *
     * @return The value/price of this structure
     */
    @Override
    public double getPrice() {
        return price;
    }

    /**
     * Gets the direction in which this structure is oriented
     * @return the direction
     */
    @Override
    public Direction getDirection() {
        return direction;
    }

    /**
     * The current construction status of this structure
     *
     * @return The construction status
     */
    public ConstructionStatus getConstructionStatus() {
        return status;
    }

    /**
     * Gets when this structure was completed, may return null
     *
     * @return The date of completion
     */
    @Override
    public Date getCompletedAt() {
        return completedAt;
    }
    
    public Vector getSize() {
        return cuboidRegion.getMaximumPoint().subtract(cuboidRegion.getMinimumPoint()).add(Vector.ONE);
    }

    /**
     * Gets when this structure was created
     *
     * @return The date this structure was created
     */
    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * Gets the date when this structure was removed. may return null
     *
     * @return The date of removal
     */
    @Override
    public Date getDeletedAt() {
        return deletedAt;
    }

    @Override
    public ConstructionStatus getStatus() {
        return status;
    }
    
    
    
    /**
     * Will add the offset to the structure's origin, which is always the front
     * left corner of a structure.
     *
     * @param offset The offset
     * @return the location
     */
    @Override
    public Vector translateRelativeLocation(Vector offset) {
        Vector p = WorldUtil.translateLocation(getOrigin(), getDirection(), offset.getX(), offset.getY(), offset.getZ());
        return new Vector(p.getBlockX(), p.getBlockY(), p.getBlockZ());
    }

    /**
     * Gets the relative position
     * @param worldPosition The worldposition
     * @return The relative position
     */
    @Override
    public Vector getRelativePosition(Vector worldPosition) {
        switch (getDirection()) {
            case NORTH:
                return new Vector(
                        worldPosition.getBlockX() - this.getOrigin().getX(),
                        worldPosition.getBlockY() - this.getOrigin().getY(),
                        this.getOrigin().getZ() - worldPosition.getBlockZ()
                );
            case SOUTH:
                return new Vector(
                        this.getOrigin().getX() - worldPosition.getBlockX(),
                        worldPosition.getBlockY() - this.getOrigin().getY(),
                        worldPosition.getBlockZ() - this.getOrigin().getZ()
                );
            case EAST:
                return new Vector(
                        worldPosition.getBlockZ() - this.getOrigin().getZ(),
                        worldPosition.getBlockY() - this.getOrigin().getY(),
                        worldPosition.getBlockX() - this.getOrigin().getX()
                );
            case WEST:
                return new Vector(
                        this.getOrigin().getZ() - worldPosition.getBlockZ(),
                        worldPosition.getBlockY() - this.getOrigin().getY(),
                        this.getOrigin().getX() - worldPosition.getBlockX()
                );
            default:
                throw new AssertionError("Unreachable");
        }
    }

    /**
     * Returns the directory for this structure
     *
     * @return The directory
     */
    @Override
    public final File getDirectory() {
        IWorld world = StructureAPI.getInstance().getPlatform().getServer().getWorld(getWorldUUID());
        
        File worldStructureFolder = StructureAPI.getInstance().getStructuresDirectory(world.getName());
        return new File(worldStructureFolder, String.valueOf(getId()));
    }

    @Override
    public IStructurePlan getStructurePlan() throws StructureException {
        File planFile = new File(getDirectory(), "structureplan.xml");
        if(!planFile.exists()) {
            throw new StructureException("Structure #" + getId() + " doesn't have a plan!");
        }

        StructurePlanReader reader = new StructurePlanReader();
        IStructurePlan plan = reader.readFile(planFile);

        return plan;
    }

    @Override
    public File getBackupDirectory() {
        return new File(getDirectory(), BACKUPS_DIR);
    }

    

}
