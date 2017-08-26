package com.unibo.s3.main_system.graph;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.unibo.s3.main_system.communication.SystemManager;
import com.unibo.s3.main_system.world.actors.WorldActor;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.NoSuchFileException;

import static java.lang.Thread.sleep;
import static org.junit.Assert.*;

public class GraphTest {
    //nome file C:\Users\Sara\Maps\map2
    private Graph graph;

    /*@Before
    public void init() {
        this.graph = new GraphImpl();
    }*/

    private void addWall(Integer[][] grid, int xMin, int xMax, int yMin, int yMax) {
        for(int i = xMin; i <= xMax && i < grid.length; i++) {
            for(int j = yMin; j <= yMax && j < grid[0].length; j++) {
                grid[i][j] = 1;
            }
        }
    }

    /*@Test(expected = IOException.class)
    public void checkFileException() {
        this.graph.receivedMapfile("BOIATE");
    }*/

    /*@Test
    public void checkMatrix() {
        this.graph.receivedDimension(15,9);
        assertArrayEquals(this.graph.getGridDimension(), new Integer[]{15,9});
        this.graph.receivedMapfile("C:\\Users\\Sara\\Maps\\test");
        assertEquals(this.graph.getMapFilename(), "C:\\Users\\Sara\\Maps\\test");
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Integer[][] grid = new Integer[15][9];
        addWall(grid, 0, 5, 4, 9);
        addWall(grid, 13, 15, 2, 8);
        addWall(grid, 7, 15, 0, 2);
        addWall(grid, 11, 15, 8, 9);
        addWall(grid, 5, 10, 4, 6);

        assertArrayEquals(this.graph.getGrid(), grid);
    }*/

    @Test
    public void checkNewGraph() {
        SystemManager.getInstance().createSystem("TestSystem", null);
        SystemManager.getInstance().createActor(WorldActor.props(new World(new Vector2(0, 0), true)), "worldActor");

        UndirectedGraph<Vector2, DefaultEdge> objectGraph = null;
        try {
            objectGraph = GraphGenerator.createGraph("C:\\Users\\Sara\\Maps\\test");
        } catch (Exception e) {
            fail("EXCEPTION!!! " + e.getMessage());
        }
    }

    /*@Test
    public void checkGraphCreation() {

        SystemManager.getInstance().createSystem("TestSystem", null);
        SystemManager.getInstance().createActor(WorldActor.props(new World(new Vector2(0, 0), true)), "worldActor");

        checkMatrix();
        UndirectedGraph<Vector2, DefaultEdge> objectGraph = null;
        try {
            objectGraph = GraphGenerator.createGraph("C:\\Users\\Sara\\Maps\\test");
        } catch (Exception e) {
            fail("EXCEPTION!!! " + e.getMessage());
        }

        UndirectedGraph<Vector2, DefaultEdge> myGraph = new SimpleGraph<>(DefaultEdge.class);
        Vector2 v1 = new Vector2(3f,3f);
        myGraph.addVertex(v1);
        Vector2 v2 = new Vector2(7f,3f);
        myGraph.addVertex(v2);
        Vector2 v3 = new Vector2(11f,3f);
        myGraph.addVertex(v3);
        Vector2 v4 = new Vector2(11f,7f);
        myGraph.addVertex(v4);
        Vector2 v5 = new Vector2(7f,7f);
        myGraph.addVertex(v5);

        myGraph.addEdge(v1, v2);
        myGraph.addEdge(v2, v3);
        myGraph.addEdge(v3, v4);

        assertEquals(objectGraph, myGraph);
    }*/

}
