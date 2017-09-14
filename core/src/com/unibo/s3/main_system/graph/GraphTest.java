package com.unibo.s3.main_system.graph;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.unibo.s3.main_system.communication.GeneralActors;
import com.unibo.s3.main_system.communication.SystemManager;
import com.unibo.s3.main_system.world.actors.WorldActor;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.Test;

import java.util.Vector;

import static org.junit.Assert.assertEquals;

public class GraphTest extends ApplicationAdapter {
    @Override
    public void create() {
        doTest();
    }

    private void log(String msg) {
        System.out.println("[GRAP TEST] " + msg);
    }

    public void doTest() {
        SystemManager.createSystem("System", null);
        SystemManager.createGeneralActor(WorldActor.props(new World(new Vector2(0, 0), true)), GeneralActors.WORLD_ACTOR());
        log("Actor system creato");

        UndirectedGraph<Vector2, DefaultEdge> graph = null;
        graph = GraphGenerator.createGraph(15,9,"maps/test.txt");
        log("Graph created = " + graph.toString());
        //assertEquals(graph, null);
    }
}
