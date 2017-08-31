package com.unibo.s3.main_system.graph;

import com.badlogic.gdx.math.Vector2;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public interface GraphManager {
    UndirectedGraph<Vector2, DefaultEdge> getGraph();

    UndirectedGraph<Vector2, DefaultEdge> createGraph(String pathMap);
}
