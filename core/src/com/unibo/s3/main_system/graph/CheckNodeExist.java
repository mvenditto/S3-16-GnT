package com.unibo.s3.main_system.graph;

import com.badlogic.gdx.math.Vector2;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class CheckNodeExist implements Callable<List<Vector2>> {
    private int x, y;
    private List<Vector2> coords;
    private UndirectedGraph<Vector2, DefaultEdge> graph;
    private int id;

    public CheckNodeExist(List<Vector2> cs, UndirectedGraph<Vector2, DefaultEdge> g, int id) {
        /*this.x = x;
        this.y = y;*/
        this.coords = cs;
        this.graph = g;
        this.id = id;
        System.out.println("Avviato CheckNodeExist numero " + id);
    }

    @Override
    public List<Vector2> call() throws Exception {
        List<Vector2> res = new ArrayList<>();
        this.coords.forEach(v -> {
            if(this.graph.containsVertex(v))
                res.add(v);
        });

        System.out.println("Finito " + id);
        return res;
    }
}
