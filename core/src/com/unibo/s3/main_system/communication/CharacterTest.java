package com.unibo.s3.main_system.communication;

import com.badlogic.gdx.math.Vector2;
import com.unibo.s3.main_system.characters.BaseCharacter;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.junit.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class CharacterTest {
    private BaseCharacter character = new BaseCharacter(new Vector2(1,1), 0);
    private UndirectedGraph<Vector2, DefaultEdge> testGraph = new SimpleGraph<>(DefaultEdge.class);
    private Vector2 v1 = new Vector2(3f,3f);
    private Vector2 v2 = new Vector2(7f,3f);
    private Vector2 v3 = new Vector2(11f,3f);
    private Vector2 v4 = new Vector2(11f,7f);
    private Vector2 v5 = new Vector2(7f,7f);
    private Vector2 currentVertex = v1;

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
    }

    @Test
    public void testCharacter(){

        System.out.println("Initial position: " + character.getPosition());
        System.out.println("Graph: " + testGraph.toString());
       // System.out.println(testGraph.edgeSet().toString());

        System.out.println("Edges connected to " + v3);

        ArrayList<Vector2> achievableVertices = new ArrayList<>();
        achievableVertices.add(v1);
        achievableVertices.add(v2);
        achievableVertices.add(v3);
        achievableVertices.add(v4);
        achievableVertices.add(v5);

        Set<Vector2> set = testGraph.vertexSet();

        assertTrue(character.computeNearest(set) == v1);
        System.out.println("Nearest vertex is " + character.computeNearest(set));
        if(currentVertex != character.computeNearest(set)){
            System.out.println("Nearest vertex changed!");
        } else {
            System.out.println("Nearest vertex NOT changed!");
        }
        currentVertex = character.computeNearest(set);

        System.out.println(character.getPosition().add(5, 5));
        assertFalse(character.computeNearest(set) == v1);
        assertTrue(character.computeNearest(set) == v5);
        System.out.println("Nearest vertex is " + character.computeNearest(set));
        if(currentVertex != character.computeNearest(set)){
            System.out.println("Nearest vertex changed!");
        } else {
            System.out.println("Nearest vertex NOT changed!");
        }
        currentVertex = character.computeNearest(set);
        System.out.println(character.getCurrentNode());
        character.extractSourceAndTarget();
    }

    @Test
    public void testConnectedVertices(){
        character.setGraph(testGraph);
        List<Vector2> l = character.extractSourceAndTarget();
        System.out.println(l.toString());
        assertEquals(l, Arrays.asList(v2, v5));
        character.getPosition().add(5, 5);
        character.setGraph(testGraph);
        l.clear();
        l = character.extractSourceAndTarget();
        System.out.println(l.toString());
        assertEquals(l, Arrays.asList(v1, v4));
    }

    /*private Vector2 computeNearest(Set<Vector2> set){
        Vector2 nearest = new Vector2(-1000, -1000);
        float minDistance = character.getPosition().dst2(new Vector2(nearest.x, nearest.y));
        for(Vector2 v : set){
            float distance = (v.dst2(character.getPosition()));
            //System.out.println("Distance between " + character.getPosition() + " and " + v.x + "," + v.y + " is " + distance);
            if(distance < minDistance){
                nearest = v;
                minDistance = distance;
            }
        }
        return nearest;
    }*/

}
