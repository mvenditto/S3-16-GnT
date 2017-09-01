package com.unibo.s3.main_system.graph;

import com.badlogic.gdx.ai.utils.Ray;
import com.badlogic.gdx.ai.utils.RaycastCollisionDetector;
import com.badlogic.gdx.math.Vector2;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.shortestpath.KShortestPaths;
import org.jgrapht.graph.DefaultEdge;

import java.util.List;
import java.util.concurrent.Callable;

public class ConcurrentAddEdges implements Callable<Void> {
    private final List<Vector2> nodes;
    private UndirectedGraph<Vector2, DefaultEdge> graph;
    private RaycastCollisionDetector<Vector2> collisionDetector;
    private int id;

    public ConcurrentAddEdges(List<Vector2> nodes, UndirectedGraph<Vector2, DefaultEdge> graph,
                              RaycastCollisionDetector<Vector2> collisionDetector, int id) {
        this.nodes = nodes;
        this.graph = graph;
        this.collisionDetector = collisionDetector;
        this.id = id;
    }

    private void log(String msg) {
        System.out.println("[Thread " + id + "] " + msg);
    }

    @Override
    public Void call() throws Exception {

        float maxDist = 7f;
        KShortestPaths<Vector2, DefaultEdge> ksp = new KShortestPaths<>(graph, 1);

        this.nodes.forEach(node -> {
            for(float x = node.x - maxDist; x <= node.x + maxDist; x++) {
                for(float y = node.y - maxDist; y <= node.y + maxDist; y++) {
                    Vector2 toCompare = new Vector2(x, y);
                    //log("Sto comparando " + node.toString() + " con " + toCompare.toString());
                    if(graph.containsVertex(toCompare)) {
                        if (!toCompare.equals(node) && ksp.getPaths(node, toCompare).size() == 0) {
                            //log(node.toString() + " non arriva a " + toCompare.toString());
                            if(checkEdgeRayCast(collisionDetector, node, toCompare, 0.5f, 16)) {
                                DefaultEdge edge = graph.addEdge(node, toCompare);
                                System.out.println("Secondi archi dal thread: aggiunto " + edge.toString());
                            }
                        }

                    }
                }
            }
        });
        log("Controllati tutti i nodi");

        return null;
    }

    private static boolean checkEdgeRayCast(RaycastCollisionDetector<Vector2> collisionDetector, Vector2 v1, Vector2 v2, float vertexRadius, int numRays) {

        final Vector2 tmp = new Vector2();
        final Vector2 tmp2 = new Vector2();
        final float step = 360.0f / numRays;

        for (float i = 0; i <= 360; i += step) {
            final double iRad = Math.toRadians(i);

            tmp.x = (float)(vertexRadius * Math.cos(iRad) + v2.x);
            tmp.y = (float)(vertexRadius * Math.sin(iRad) + v2.y);

            tmp2.x = (float)(vertexRadius * Math.cos(iRad) + v1.x);
            tmp2.y = (float)(vertexRadius * Math.sin(iRad) + v1.y);

            //log("Analisi: " + tmp2.toString() + " con " + tmp.toString());
            if (!collisionDetector.collides(new Ray<>(tmp2, tmp))) {
                //log(tmp2.toString() + " e " + tmp.toString() + " collidono");
                return true;
            }
        }

        return false;
    }
}
