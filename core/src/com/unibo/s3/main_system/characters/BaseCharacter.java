package com.unibo.s3.main_system.characters;

import akka.actor.ActorRef;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.unibo.s3.main_system.characters.steer.BaseMovableEntity;
import com.unibo.s3.main_system.characters.steer.CustomLocation;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.NeighborIndex;
import org.jgrapht.graph.DefaultEdge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class BaseCharacter extends BaseMovableEntity implements Character {

    private Color color;
    private final int id;

    private UndirectedGraph<Vector2, DefaultEdge> graph;
    private int nNeighbours = 0;
    private Vector2 currentNode;
    private List<ActorRef> neighbours = new ArrayList<>();
    private List<Vector2> visited = new ArrayList<>();
    private NeighborIndex index;
    private Vector2 currentDestination;

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
        currentNode = computeInitialNearestNode();
        index = new NeighborIndex(graph);
        currentDestination = selectRandomDestination();
        System.out.println(log() + "my destination is " + currentDestination);
        setNewDestination(currentDestination);
    }

    public Vector2 getCurrentNode() {
        return currentNode;
    }

    public List<ActorRef> getNeighbours() {
        return neighbours;
    }

    public void setNeighboursList(List<ActorRef> neighbours){
        this.neighbours = neighbours;
    }


    public void addNeighbour(ActorRef neighbour){
        this.neighbours.add(neighbour);
        this.nNeighbours++;
    }

    public void chooseBehaviour(){

        this.currentNode = computeNearest();
        if (currentNode.equals(currentDestination)) {
            System.out.println(log() + "Destination " + currentDestination + " = " + currentNode + " achieved! Choose the next one");
            currentDestination = selectRandomDestination();
        }
        //ora scelgo destinazione casuale tra i vicini, potenzialmente torno indietro
    }

    public void setnNeighbours(int n){
        this.nNeighbours = n;
    }

    public Vector2 getCurrentDestination() {
        return currentDestination;
    }

    public List<Vector2> getInformations(){
        return this.visited;
    }

    public void updateGraph(List<Vector2> colleagueList){
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
                .arriveTo(new CustomLocation(destination))
                .buildPriority(true);
    }

    private void chooseNextDestination(){
        //scegli prossimo nodo tra i raggiungibili
    }

    public List<Vector2> computeNeighbours(){
      return index.neighborListOf(currentNode);
    }

    //computo il mio nodo di riferimento
    public Vector2 computeNearest(){
        Vector2 nearest = currentNode;
        float minDistance = getPosition().dst2(new Vector2(nearest.x, nearest.y));
        List<Vector2> list = computeNeighbours();
        for(Vector2 v : list){
            float distance = (v.dst2(getPosition()));
            if(distance < minDistance){
                nearest = v;
                minDistance = distance;
            }
        }
        if(currentNode != nearest){
            discoverNewVertex(nearest);
        }
        currentNode = nearest;
        if(!computeNeighbours().contains(getCurrentDestination())){ //se ho cambiato nodo di riferimento e questo non è collegato alla destinazione la ricalcolo
            currentDestination = selectRandomDestination();
        }
        return nearest;
    }

    private Vector2 computeInitialNearestNode(){
        Vector2 nearest = new Vector2();
        float minDistance = Float.MAX_VALUE;
        for(Vector2 v : graph.vertexSet()){
            float distance = (v.dst2(getPosition()));
            if(distance < minDistance){
                nearest = v;
                minDistance = distance;
            }
        }
        discoverNewVertex(nearest);
        return nearest;
    }

    private Vector2 selectRandomDestination(){
        return (Vector2) index.neighborListOf(currentNode).get(new Random().nextInt(index.neighborListOf(currentNode).size()));
    }

    private void discoverNewVertex(Vector2 nearest){
        visited.add(nearest);
    }

    private String log(){
        return "Agent " + id + ": ";
    }
}
