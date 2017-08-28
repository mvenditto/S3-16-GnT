package com.unibo.s3.main_system.characters;

import akka.actor.ActorRef;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.unibo.s3.main_system.characters.steer.BaseMovableEntity;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.ArrayList;
import java.util.Set;

public class BaseCharacter extends BaseMovableEntity implements Character {

    private Color color;
    private final int id;

    private UndirectedGraph<Vector2, DefaultEdge> graph;
    private int nNeighbours;
    private Vector2 currentNode;
    private ArrayList<ActorRef> neighbours;

    public BaseCharacter(Vector2 position, int id) {
        super(position);
        this.currentNode = new Vector2(position.cpy());
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

    public void chooseBehaviour(){

        Vector2 currentPosition = getPosition();

        Set<DefaultEdge> edges = graph.edgeSet();
        DefaultEdge edge = new DefaultEdge();


        ArrayList<DefaultEdge> connectedEdges = computeConnectedEdgers();
        //guardo il grafo e ci penso
        //aggiornare destinazione
        ArrayList<Vector2> achievableVertices = new ArrayList<>();
        Vector2 nearest = new Vector2(-1000, -1000);
        float minDistance = currentPosition.dst2(currentPosition/*del current node*/);
        for(Vector2 v : achievableVertices){
            if(v.dst2(currentPosition) < minDistance){
                //v è il più vicino
            }
        }
    }

    public void setnNeighbours(int n){
        this.nNeighbours = n;
    }

    //
    private void updateGraph(){
        this.nNeighbours--;
        //update lista
        if(nNeighbours == 0){
            chooseBehaviour();
        }
    }

    private void setNewDestination(Vector2 destination){
        //setta destinazione
        this.setComplexSteeringBehavior()
                .avoidCollisionsWithWorld()
               //? .arriveTo()
                .buildPriority(true);
    }

    private ArrayList<DefaultEdge> computeConnectedEdgers(){
        ArrayList<DefaultEdge> out = new ArrayList<>();


        return out;
    }
}
