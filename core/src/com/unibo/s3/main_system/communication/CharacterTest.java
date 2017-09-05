package com.unibo.s3.main_system.communication;

import com.badlogic.gdx.math.Vector2;
import com.unibo.s3.main_system.characters.BaseCharacter;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.NeighborIndex;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.junit.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class CharacterTest {
    private BaseCharacter character = new BaseCharacter(new Vector2(1,1), 0);
    private UndirectedGraph<Vector2, DefaultEdge> testGraph = new SimpleGraph<>(DefaultEdge.class);
    private Vector2 v1 = new Vector2(3f,3f);
    private Vector2 v2 = new Vector2(7f,3f);
    private Vector2 v3 = new Vector2(11f,3f);
    private Vector2 v4 = new Vector2(11f,7f);
    private Vector2 v5 = new Vector2(7f,7f);

    @Before
    public void init(){
        testGraph.addVertex(v1);
        testGraph.addVertex(v2);
        testGraph.addVertex(v3);
        testGraph.addVertex(v4);
        testGraph.addVertex(v5);

        testGraph.addEdge(v1, v2);
        testGraph.addEdge(v1, v5);
        testGraph.addEdge(v2, v3);
        testGraph.addEdge(v3, v4);
        testGraph.addEdge(v5, v4);
        testGraph.addEdge(v2, v5);

        character.setGraph(testGraph);
        //character.computeNearest();
    }

    @Test
    public void testCharacter(){
        System.out.println("Initial position: " + character.getPosition());
        System.out.println("Graph: " + testGraph.toString());
        assertTrue(character.computeNearest() == v1);
        //System.out.println("Nearest vertex is " + character.computeNearest());
        character.getPosition().add(5, 5);
        assertFalse(character.computeNearest() == v1);
        assertTrue(character.computeNearest() == v5);
        character.computeNeighbours();
    }

    @Test
    public void testConnectedVertices(){
        List<Vector2> l = character.computeNeighbours();
        //System.out.println("Neighbours: " + l.toString());
        assertEquals(l, Arrays.asList(v2, v5));
        character.getPosition().add(5, 5);
        //System.out.println(character.getPosition());
        character.computeNearest();
        l.clear();
        l = character.computeNeighbours();
        //System.out.println("Neighbours: " + l.toString());
        assertEquals(l, Arrays.asList(v1, v4, v2));
        assertFalse(l == Arrays.asList(v1, v2));
    }

    @Test
    public void testDiscover(){
//        assertEquals(character.getInformations(),Arrays.asList(v1));
 //       character.getPosition().add(5, 5);
  //      character.computeNearest();
   //     assertEquals(character.getInformations(),Arrays.asList(v1, v5));
   //     assertFalse(character.getInformations().contains(v3));
    }

    @Test
    public void testInformationExchange(){
        BaseCharacter secondCharacter = new BaseCharacter(new Vector2(7, 8), 1);
        secondCharacter.setGraph(testGraph);
        //System.out.println(secondCharacter.computeNeighbours());
        //System.out.println(secondCharacter.getCurrentNode());
        secondCharacter.computeNearest();
        assertEquals(secondCharacter.computeNearest(), v5);
        secondCharacter.getPosition().add(0, -4);
        secondCharacter.computeNearest();
        assertEquals(secondCharacter.computeNearest(), v2);
//        assertEquals(secondCharacter.getInformations(), Arrays.asList(v5, v2));
        character.computeNearest();
        character.getPosition().add(6, 2);
        assertEquals(character.computeNearest(), v2);
  //      assertEquals(character.getInformations(), Arrays.asList(v1, v2));

        //information exchange
        character.updateGraph(secondCharacter.getInformations());
        secondCharacter.updateGraph(character.getInformations());
    //    assertEquals(character.getInformations(), Arrays.asList(v1, v2, v5));
      //  assertEquals(secondCharacter.getInformations(), Arrays.asList(v5, v2, v1));
    }
    /**todo correggi test con util.list**/

    @Test
    public void testNeighbours(){
        NeighborIndex index = new NeighborIndex(testGraph);
        assertEquals(index.neighborListOf(v1), Arrays.asList(v2, v5));
    }

    @Test
    public void testDestination(){
        Random random = new Random();
        for(int i = 0; i < 20; i++) {
            assertTrue(character.computeNeighbours().contains(character.getCurrentDestination()));
            character.getPosition().add(random.nextInt(5) - 1, random.nextInt(5) - 1);
            character.chooseBehaviour();
            System.out.println("position: " + character.getPosition());
            System.out.println("node: " + character.getCurrentNode() + " / neigh: " + character.computeNeighbours());
            System.out.println("destination: " + character.getCurrentDestination());
            System.out.println("");
            assertTrue(character.computeNeighbours().contains(character.getCurrentDestination()));
        }
        for(int i = 0; i < 20; i++) {
            assertTrue(character.computeNeighbours().contains(character.getCurrentDestination()));
            character.getPosition().add(random.nextInt(2) - 4, random.nextInt(2) - 4);
            character.chooseBehaviour();
            System.out.println("position: " + character.getPosition());
            System.out.println("node: " + character.getCurrentNode() + " / neigh: " + character.computeNeighbours());
            System.out.println("destination: " + character.getCurrentDestination());
            System.out.println("");
            assertTrue(character.computeNeighbours().contains(character.getCurrentDestination()));
        }
    }

}
