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
    private ArrayList<Vector2> visited;

    public BaseCharacter(Vector2 position, int id) {
        super(position);
        //this.currentNode = computeNearest(this.graph.vertexSet());
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

        //mi servono i miei vicini
        //guardo il grafo e ci penso
        //aggiorno la destinazione
    }

    public void setnNeighbours(int n){
        this.nNeighbours = n;
    }

    private void updateGraph(ArrayList<Vector2> colleagueList){
        this.nNeighbours--;
        //update lista
        for(Vector2 v : colleagueList){
            if(!visited.contains(v)){
                visited.add(v);
            }
        }
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


    //computo il mio nodo di riferimento
    /**todo per ora tra tutti i nodi, modificare per cercare solo tra i vicini*/
    public Vector2 computeNearest(Set<Vector2> set){
        Vector2 nearest = new Vector2(-1000, -1000);
        float minDistance = getPosition().dst2(new Vector2(nearest.x, nearest.y));
        for(Vector2 v : set){
            float distance = (v.dst2(getPosition()));
            //System.out.println("Distance between " + character.getPosition() + " and " + v.x + "," + v.y + " is " + distance);
            if(distance < minDistance){
                nearest = v;
                minDistance = distance;
            }
        }
        return nearest;
    }
}
