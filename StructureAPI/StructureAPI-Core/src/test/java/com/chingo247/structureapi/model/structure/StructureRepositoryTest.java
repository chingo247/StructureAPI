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
package com.chingo247.structureapi.model.structure;

import com.chingo247.settlercraft.core.Direction;
import com.chingo247.settlercraft.core.model.settler.SettlerNode;
import com.chingo247.settlercraft.core.model.settler.SettlerRepository;
import com.chingo247.structureapi.model.owner.OwnerType;
import com.chingo247.structureapi.model.owner.StructureOwnership;
import com.chingo247.structureapi.model.world.StructureWorldNode;
import com.chingo247.structureapi.model.world.StructureWorldRepository;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import java.util.Collection;
import java.util.UUID;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;

/**
 *
 * @author Chingo
 */
public class StructureRepositoryTest {

    private static final GraphDatabaseService graph = new TestGraphDatabaseFactory().newImpermanentDatabase();;

    public StructureRepositoryTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        
    }

    @AfterClass
    public static void tearDownClass() {
        graph.shutdown();
    }

    @Before
    public void setUp() {
        setupIdGenerator("SETTLER_ID");
        setupIdGenerator("STRUCTURE_ID");
    }

    @After
    public void tearDown() {
        String query = "MATCH (n) DETACH DELETE n";
        graph.execute(query);
    }

    /**
     * Test of findById method, of class StructureRepository.
     */
    @Test
    public void testFindById() {
        try (Transaction tx = graph.beginTx()) {
            System.out.println("TEST StructureRepository findById");
            Long id = 1L;
            StructureRepository repository = new StructureRepository(graph);
            StructureNode expResult = repository.addStructure("test", Vector.ZERO, new CuboidRegion(Vector.ZERO, new BlockVector(10, 10, 10)), Direction.NORTH, 100);;
            StructureNode result = repository.findById(id);
            assertEquals(expResult.getId(), result.getId());
            tx.success();
        }
    }

    /**
     * Test of addStructure method, of class StructureRepository.
     */
    @Test
    public void testAddStructure() {
        try (Transaction tx = graph.beginTx()) {
            System.out.println("TEST StructureRepository addStructure");
            String name = "test";
            Vector position = Vector.ZERO;
            CuboidRegion region = new CuboidRegion(Vector.ZERO, new BlockVector(10, 10, 10));
            Direction direction = Direction.WEST;
            double price = 1337.0;
            StructureRepository instance = new StructureRepository(graph);
            StructureNode result = instance.addStructure(name, position, region, direction, price);
            assertEquals(result.getCuboidRegion().getMaximumPoint(), region.getMaximumPoint());
            assertEquals(result.getCuboidRegion().getMinimumPoint(), region.getMinimumPoint());
            assertTrue(result.getPrice() == price);
            assertTrue(result.getDirection() == direction);
            assertEquals(result.getName(), name);
            assertEquals(result.getOrigin(), position);
            tx.success();
        }
    }

    /**
     * Test of findByWorld method, of class StructureRepository.
     */
    @Test
    public void testFindByWorld() {
        System.out.println("TEST StructureRepository findByWorld");
        UUID worldUUID = UUID.randomUUID();
        UUID otherUUID = UUID.randomUUID();
        StructureWorldRepository worldRepository = new StructureWorldRepository(graph);
        StructureRepository structureRepository = new StructureRepository(graph);
        try (Transaction tx = graph.beginTx()) {
            // add World
            StructureWorldNode worldNode = worldRepository.addOrGet("TestWorld", worldUUID);
            StructureWorldNode anotherWorld = worldRepository.addOrGet("TestWorld-2", otherUUID);
            assertNotNull(anotherWorld);
            assertNotNull(worldNode);
            // Test Add world works
            StructureWorldNode existingWorld = worldRepository.findByUUID(worldUUID);
            assertTrue(existingWorld.getUUID().equals(worldNode.getUUID()));
            StructureWorldNode anotherWorldExists = worldRepository.findByUUID(otherUUID);
            assertTrue(anotherWorldExists.getUUID().equals(anotherWorld.getUUID()));

            // Add current to world
            StructureNode snThisWorld = structureRepository.addStructure("test", Vector.ZERO, new CuboidRegion(Vector.ZERO, new Vector(10, 10, 10)), Direction.NORTH, 10.0);
            worldNode.addStructure(snThisWorld);

            // Add current to other world so we guarantee there are others!
            StructureNode snAnotherWorld = structureRepository.addStructure("test", Vector.ZERO, new CuboidRegion(Vector.ZERO, new Vector(10, 10, 10)), Direction.NORTH, 10.0);
            anotherWorld.addStructure(snAnotherWorld);

            // Check exists in current
            Collection<StructureNode> current = structureRepository.findByWorld(worldUUID);
            assertTrue(current.size() == 1);
            for (StructureNode structure : current) {
                assertTrue(structure.getWorldUUID().equals(worldUUID));
            }

            // Check exists in other
            Collection<StructureNode> other = structureRepository.findByWorld(otherUUID);
            assertTrue(other.size() == 1);
            for (StructureNode structure : other) {
                assertTrue(structure.getWorldUUID().equals(otherUUID));
            }

            assertTrue(structureRepository.hasStructuresWithin(worldUUID, new CuboidRegion(Vector.ZERO, new BlockVector(10, 10, 10))));
            assertFalse(structureRepository.hasStructuresWithin(worldUUID, new CuboidRegion(new BlockVector(11, 11, 11), new BlockVector(22, 22, 22))));
            assertFalse(structureRepository.hasStructuresWithin(worldUUID, new CuboidRegion(new BlockVector(-1, 0, -1), new BlockVector(-22, 22, -22))));

            assertTrue(structureRepository.hasStructuresWithin(otherUUID, new CuboidRegion(Vector.ZERO, new BlockVector(10, 10, 10))));
            assertFalse(structureRepository.hasStructuresWithin(otherUUID, new CuboidRegion(new BlockVector(11, 11, 11), new BlockVector(22, 22, 22))));
            assertFalse(structureRepository.hasStructuresWithin(otherUUID, new CuboidRegion(new BlockVector(-1, 0, -1), new BlockVector(-22, 22, -22))));

            tx.success();
        }
    }

    /**
     * Test of findByOwner method, of class StructureRepository.
     */
    @Test
    public void testFindByOwner() {
        System.out.println("TEST StructureRepository findByOwner");
        UUID playerUUID = UUID.randomUUID();
        UUID otherUUID = UUID.randomUUID();
        SettlerRepository repository = new SettlerRepository(graph);
        StructureRepository instance = new StructureRepository(graph);
        try (Transaction tx = graph.beginTx()) {
            // add World
            SettlerNode player = repository.addSettler(playerUUID, "TestWorld");
            SettlerNode anotherPlayer = repository.addSettler(otherUUID, "TestWorld");
            assertNotNull(anotherPlayer);
            assertNotNull(player);
            // Test Add world works
            SettlerNode playerExists = repository.findByUUID(playerUUID);
            assertTrue(playerExists.getUniqueId().equals(player.getUniqueId()));
            SettlerNode anotherPlayerExists = repository.findByUUID(otherUUID);
            assertTrue(anotherPlayerExists.getUniqueId().equals(anotherPlayer.getUniqueId()));

            StructureNode snPlayer = instance.addStructure("test", Vector.ZERO, new CuboidRegion(Vector.ZERO, new Vector(10, 10, 10)), Direction.NORTH, 10.0);
            snPlayer.getOwnerDomain().setOwnership(player, OwnerType.MASTER);
            assertTrue(snPlayer.getOwnerDomain().isOwner(player.getUniqueId()));

            StructureNode snOther = instance.addStructure("test", Vector.ZERO, new CuboidRegion(Vector.ZERO, new Vector(10, 10, 10)), Direction.NORTH, 10.0);
            snOther.getOwnerDomain().setOwnership(anotherPlayer, OwnerType.MASTER);
            assertTrue(snOther.getOwnerDomain().isOwner(anotherPlayer.getUniqueId()));

            tx.success();
        }
        
        try(Transaction tx = graph.beginTx()) {
            Collection<StructureOwnership> current = instance.findByOwner(playerUUID);
            
            assertTrue(current.size() == 1);
            for (StructureOwnership structure : current) {
                assertTrue(structure.getOwner().getUniqueId().equals(playerUUID));
            }

            Collection<StructureOwnership> other = instance.findByOwner(otherUUID);
            assertTrue(current.size() == 1);
            for (StructureOwnership structure : other) {
                assertTrue(structure.getOwner().getUniqueId().equals(otherUUID));
            }
            tx.success();
        }
        
    }
    
     private static void setupIdGenerator(String generatorName) {
        try (Transaction tx = graph.beginTx()) {
            Result r = graph.execute("MATCH (sid: ID_GENERATOR {name:'" + generatorName + "'}) "
                    + "RETURN sid "
                    + "LIMIT 1");
            if (!r.hasNext()) {
                graph.execute("CREATE (sid: ID_GENERATOR {name:'" + generatorName + "', nextId: 0})");
            }
            tx.success();
        }
    }
//

}
