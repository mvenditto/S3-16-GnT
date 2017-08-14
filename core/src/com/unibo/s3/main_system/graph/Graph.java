package com.unibo.s3.main_system.graph;

import com.badlogic.gdx.ai.utils.RaycastCollisionDetector;
import com.badlogic.gdx.math.Vector2;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public interface Graph {

    void receivedDimension(int width, int height);

    Integer[] getGridDimension();

    void receivedMapfile(String name);

    String getMapFilename();

    Integer[][] getGrid();

    UndirectedGraph<Vector2, DefaultEdge> createGraph() throws Exception;

    UndirectedGraph<Vector2, DefaultEdge> getGraph();

    void setRaycastCollisionDetector(RaycastCollisionDetector<Vector2> collisionDetector);

}
