package com.unibo.s3.main_system.characters;

import akka.actor.ActorRef;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.unibo.s3.main_system.characters.steer.BaseMovableEntity;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.NeighborIndex;
import org.jgrapht.graph.DefaultEdge;

import java.util.ArrayList;
import java.util.List;

public class BaseCharacter extends BaseMovableEntity implements Character {

    private Color color;
    private final int id;

    private UndirectedGraph<Vector2, DefaultEdge> graph;
    private int nNeighbours;
    private Vector2 currentNode;
    private ArrayList<ActorRef> neighbours;
    private List<Vector2> visited = new ArrayList<>();
    private Vector2 defaultVertex = new Vector2(-1000, -1000); //start utility vertex
    private NeighborIndex index;

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
        //graph.addVertex(defaultVertex);
        //currentNode = defaultVertex;
        currentNode = computeInitialNearestNode();
        index = new NeighborIndex(graph);
    }

    public Vector2 getCurrentNode() {
        return currentNode;
    }

    public void chooseBehaviour(){

        //mi servono i miei vicini
        //guardo il grafo e ci penso
        //aggiorno la destinazione
    }

    public void setnNeighbours(int n){
        this.nNeighbours = n;
    }

    public List<Vector2> getInformations(){
        return this.visited;
    }

    public void updateGraph(ArrayList<Vector2> colleagueList){
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

    private void chooseNextDestination(){
        //scegli prossimo nodo tra i raggiungibili
    }

    public List<Vector2> computeNeighbours(){
        //System.out.println("  computeNeighbours Call");
        //System.out.println("Current node: " + currentNode);
      /*  Set<DefaultEdge> edges = graph.edgesOf(currentNode);

        List<Vector2> connectedVertices = new ArrayList<>();

        for(DefaultEdge e : edges){
             if(graph.getEdgeSource(e) == currentNode){
                 connectedVertices.add(graph.getEdgeTarget(e));
             } else if (graph.getEdgeTarget(e) == currentNode){
                 connectedVertices.add(graph.getEdgeSource(e));
             }
        }
        //System.out.println("Neighbours of " + currentNode + ": " + connectedVertices.toString());
        return connectedVertices;*/
      return index.neighborListOf(currentNode);
    }

    //computo il mio nodo di riferimento
    public Vector2 computeNearest(){
       // System.out.println("  computeNearest Call");
        Vector2 nearest = currentNode;
        float minDistance = getPosition().dst2(new Vector2(nearest.x, nearest.y));
        List<Vector2> list = new ArrayList<>();
        if(currentNode.equals(defaultVertex)){
            list.addAll(graph.vertexSet());
        } else {
            list = computeNeighbours();
        }
        //System.out.println("2 Neighbours of " + currentNode + ": " + list.toString());
        for(Vector2 v : list){
            float distance = (v.dst2(getPosition()));
            //System.out.println("Distance between " + getPosition() + " and " + v.x + "," + v.y + " is " + distance);
            if(distance < minDistance){
                nearest = v;
                minDistance = distance;
            }
        }
        //System.out.println("Nearest is " + nearest);
        if(currentNode != nearest){
            discoverNewVertex(nearest);
        }
        currentNode = nearest;
        return nearest;
    }

    private Vector2 computeInitialNearestNode(){
        Vector2 nearest = new Vector2();
        float minDistance = Float.MAX_VALUE;
        for(Vector2 v : graph.vertexSet()){
            float distance = (v.dst2(getPosition()));
            //System.out.println("Distance between " + getPosition() + " and " + v.x + "," + v.y + " is " + distance);
            if(distance < minDistance){
                nearest = v;
                minDistance = distance;
            }
        }
        discoverNewVertex(nearest);
        return nearest;
    }

    private void discoverNewVertex(Vector2 nearest){
        //System.out.println("New vertex discovered: " + nearest);
        visited.add(nearest);
        //System.out.println("Visited: " + visited);
    }
}
