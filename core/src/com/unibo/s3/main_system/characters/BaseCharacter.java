package com.unibo.s3.main_system.characters;

import akka.actor.ActorRef;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.unibo.s3.main_system.characters.steer.BaseMovableEntity;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.ArrayList;

public class BaseCharacter extends BaseMovableEntity implements Character {

    private Color color;
    private final int id;

    private UndirectedGraph<Vector2, DefaultEdge> graph;
    private int nNeighbours;
    private int currentNode;
    //lista vicini

    public BaseCharacter(Vector2 position, int id) {
        super(position);
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public Color getColor() {
        return color;
    }

    public void setGraph(UndirectedGraph<Vector2, DefaultEdge> g){
        this.graph = g;
    }

    public void doSomething(){
        //guardo il grafo e ci penso
        //aggiornare destinazione
        this.newLocation();
    }

    public void setnNeighbours(int n){
        this.nNeighbours = n;
    }

    //
    private void updateGraph(){
        this.nNeighbours--;
        //update lista
        if(nNeighbours == 0){
            doSomething();
        }
    }


}
