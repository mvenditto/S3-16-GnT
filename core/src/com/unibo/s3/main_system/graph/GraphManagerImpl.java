package com.unibo.s3.main_system.graph;

import com.badlogic.gdx.math.Vector2;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.NeighborIndex;
import org.jgrapht.graph.DefaultEdge;

import java.util.Iterator;


public class GraphManagerImpl implements GraphAdapter<Vector2>, GraphManager{
    private UndirectedGraph<Vector2, DefaultEdge> graph;


    @Override
    public Iterator<Vector2> getVertices() {
        return this.graph.vertexSet().iterator();
    }

    @Override
    public Iterator<Vector2> getNeighbors(Vector2 vertex) {
        NeighborIndex<Vector2, DefaultEdge> neighborIndex = new NeighborIndex<>(this.graph);
        return neighborIndex.neighborsOf(vertex).iterator();
    }


    @Override
    public UndirectedGraph<Vector2, DefaultEdge> getGraph() {
        return this.graph;
    }

    @Override
    public UndirectedGraph<Vector2, DefaultEdge> createGraph(String map) {
        this.graph = GraphGenerator.createGraph(map);
        return this.graph;
    }
}
