package com.unibo.s3.main_system.tests;

import akka.actor.ActorRef;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.unibo.s3.main_system.communication.GeneralActors;
import com.unibo.s3.main_system.communication.SystemManager;
import com.unibo.s3.main_system.communication.Messages.MapElementMsg;
import com.unibo.s3.main_system.graph.GraphGenerator;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.NeighborIndex;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import scala.tools.nsc.doc.model.Def;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphTest implements TestClass {
    private String mapFilename = "maps/test.txt";

    private void log(String msg) {
        //System.out.println("[GRAPH TEST] " + msg);
    }

    @Override
    public Map<String, Boolean> doTests() {
        Map<String, Boolean> results = new HashMap<>();
        results.put("Test graph generation", testGraphGeneration());
        return results;
    }

    private Boolean testGraphGeneration() {
        sendMapToWord();
        UndirectedGraph<Vector2, DefaultEdge> graph = null;
        graph = GraphGenerator.createGraph(15,9,mapFilename);
        NeighborIndex<Vector2, DefaultEdge> neighborGraph = new NeighborIndex<>(graph);
        log("Graph created = " + graph.toString());
        UndirectedGraph<Vector2, DefaultEdge> expected = createExpectedGraph();
        NeighborIndex<Vector2, DefaultEdge> neighborExpected = new NeighborIndex<>(expected);
        log("NUMERO VERTICI: generato = " + graph.vertexSet().size() + ", aspettato = " + expected.vertexSet().size());
        log("NUMERO ARCHI: generato = " + graph.edgeSet().size() + ", aspettato = " + expected.edgeSet().size());
        for(Vector2 vGen : graph.vertexSet()) {
            if(!expected.containsVertex(vGen)) {
                log("Il nodo " + vGen.toString() + " è nel grafo generato ma non in quello aspetatto");
                return false;
            }
            if (!neighborGraph.neighborsOf(vGen).equals(neighborExpected.neighborsOf(vGen))) {
                log("I vicini di " + vGen.toString() + " sono diversi");
                return false;
            };
        }
        for(Vector2 vGen : expected.vertexSet()) {
            if(!graph.containsVertex(vGen)) {
                log("Il nodo " + vGen.toString() + " è nel grafo aspettato ma non in quello generato");
                return false;
            }
        }
        return true;
    }

    private UndirectedGraph<Vector2, DefaultEdge> createExpectedGraph() {
        UndirectedGraph<Vector2, DefaultEdge> expectedGraph = new SimpleGraph<>(DefaultEdge.class);
        Vector2 v1 = new Vector2(3f,3f);
        expectedGraph.addVertex(v1);
        Vector2 v2 = new Vector2(6f,3f);
        expectedGraph.addVertex(v2);
        Vector2 v3 = new Vector2(9f,5f);
        expectedGraph.addVertex(v3);
        Vector2 v4 = new Vector2(13f,5f);
        expectedGraph.addVertex(v4);
        Vector2 v5 = new Vector2(12f,9f);
        expectedGraph.addVertex(v5);
        Vector2 v6 = new Vector2(9f,9f);
        expectedGraph.addVertex(v6);
        expectedGraph.addEdge(v1,v2);
        expectedGraph.addEdge(v2,v3);
        expectedGraph.addEdge(v3,v4);
        expectedGraph.addEdge(v4,v5);
        expectedGraph.addEdge(v5,v6);
        return expectedGraph;
    }

    private void sendMapToWord() {
        FileHandle file = Gdx.files.internal(mapFilename);
        String text = file.readString();
        String[] lines = text.split("\\n");
        for(String l : lines) {
            SystemManager.getLocalGeneralActor(GeneralActors.WORLD_ACTOR()).tell(new MapElementMsg(l), ActorRef.noSender());
        }
    }
}
