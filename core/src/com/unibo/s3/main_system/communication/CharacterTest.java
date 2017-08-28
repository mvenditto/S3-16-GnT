package com.unibo.s3.main_system.communication;

import com.badlogic.gdx.math.Vector2;
import com.unibo.s3.main_system.characters.BaseCharacter;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.junit.*;

import java.util.ArrayList;

import static org.junit.Assert.assertTrue;

public class CharacterTest {
    private BaseCharacter character = new BaseCharacter(new Vector2(1,1), 0);
    private UndirectedGraph<Vector2, DefaultEdge> myGraph = new SimpleGraph<>(DefaultEdge.class);
    private Vector2 v1 = new Vector2(3f,3f);
    private Vector2 v2 = new Vector2(7f,3f);
    private Vector2 v3 = new Vector2(11f,3f);
    private Vector2 v4 = new Vector2(11f,7f);
    private Vector2 v5 = new Vector2(7f,7f);

    @Before
    public void init(){
        myGraph.addVertex(v1);
        myGraph.addVertex(v2);
        myGraph.addVertex(v3);
        myGraph.addVertex(v4);
        myGraph.addVertex(v5);

        myGraph.addEdge(v1, v2);
        myGraph.addEdge(v1, v5);
        myGraph.addEdge(v2, v3);
        myGraph.addEdge(v3, v4);
        myGraph.addEdge(v5, v4);
    }

    @Test
    public void testCharacter(){

        System.out.println(character.getPosition());
        System.out.println(myGraph.toString());
       // System.out.println(myGraph.edgeSet().toString());

        System.out.println("Edges connected to " + v3);
       /* for(DefaultEdge d : myGraph.edgesOf(v3)){
            String[] sourceAndTarget = d.toString().split(":");
            String[] sourceXandY = sourceAndTarget[0].split(",");
            String[] targetXandY = sourceAndTarget[1].split(",");
            System.out.println(sourceXandY[0]);
            String[] finalX = sourceXandY[0].split("7");
            //float f = Float.parseFloat(sourceXandY[0]);
            System.out.println(finalX[0]);
           // System.out.println(sourceXandY[1]);
           // System.out.println(targetXandY[0]);
           // System.out.println(targetXandY[1]);
        }*/
        Vector2 currentPosition = character.getPosition();
        ArrayList<Vector2> achievableVertices = new ArrayList<>();
        achievableVertices.add(v1);
        achievableVertices.add(v2);
        achievableVertices.add(v3);
        achievableVertices.add(v4);
        achievableVertices.add(v5);


        System.out.println("Nearest vertex is " + computeNearest());
        System.out.println(character.getPosition().add(5, 5));
        System.out.println("Nearest vertex is " + computeNearest());
    }

    private Vector2 computeNearest(){
        Vector2 nearest = new Vector2(-1000, -1000);
        float minDistance = character.getPosition().dst2(new Vector2(nearest.x, nearest.y));
        for(Vector2 v : myGraph.vertexSet()){
            float distance = (v.dst2(character.getPosition()));
            System.out.println("Distance between " + character.getPosition() + " and " + v.x + "," + v.y + " is " + distance);
            if(distance < minDistance){
                nearest = v;
                minDistance = distance;
            }
        }
        return nearest;
    }

}
