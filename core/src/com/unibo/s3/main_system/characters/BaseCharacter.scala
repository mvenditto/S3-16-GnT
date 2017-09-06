package com.unibo.s3.main_system.characters

import java.util

import akka.actor.ActorRef
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.unibo.s3.main_system.characters.steer.{BaseMovableEntity, CustomLocation}
import org.jgrapht.UndirectedGraph
import org.jgrapht.alg.NeighborIndex
import org.jgrapht.graph.DefaultEdge

import scala.util.Random

trait Character{

  //add neighbours

  //refresh neighbours

  //get info

}

class BaseCharacter(vector2: Vector2, id : Int) extends BaseMovableEntity(vector2) with Character{

  private var color : Color = _

  private[this] var graph: UndirectedGraph[Vector2, DefaultEdge] = _ //char

  private var nNeighbours = 0 //guard

  private var currentNode : Vector2 = _
  var neighbours = List[ActorRef]() //char?

  var visited = List[Vector2]()
  private var index : NeighborIndex[Vector2,DefaultEdge] = _
  private var currentDestination : Vector2 = _


  def getId: Int = id

  override def setColor(color: Color): Unit = { this.color = color }

  override def getColor: Color = color

  def setGraph(g: UndirectedGraph[Vector2, DefaultEdge]): Unit = {
    this.graph = g
    currentNode = computeInitialNearestNode
    index = new NeighborIndex[Vector2, DefaultEdge](graph)
    currentDestination = selectRandomDestination
    System.out.println(log + "my destination is " + currentDestination)
    setNewDestination(currentDestination)
  }

  def addNeighbour(neighbour: ActorRef): Unit = {
    this.neighbours :+= neighbour
    this.nNeighbours += 1
  }

  def refreshNeighbours() : Unit = this.neighbours = List()

  def isNeighbour(possibleNeighbour : ActorRef) : Boolean = neighbours.contains(possibleNeighbour)

  def getInformations: scala.List[Vector2] = this.visited

  def updateGraph(colleagueList: List[Vector2]): Unit = {
    this.nNeighbours -= 1
    //update lista
    for (v <- colleagueList) {
      if (!visited.contains(v)) visited:+=v
    }
    if (nNeighbours == 0) chooseBehaviour()
  }


  private def setNewDestination(destination: Vector2) = { //setta destinazione
    this.setComplexSteeringBehavior.avoidCollisionsWithWorld.arriveTo(new CustomLocation(destination)).buildPriority(true)
  }


  private def computeInitialNearestNode = {
    var nearest = new Vector2
    var minDistance = Float.MaxValue
    import scala.collection.JavaConversions._
    for (v <- graph.vertexSet) {
      val distance = v.dst2(getPosition)
      if (distance < minDistance) {
        nearest = v
        minDistance = distance
      }
    }
    discoverNewVertex(nearest)
    nearest
  }

  private def selectRandomDestination = index.neighborListOf(currentNode).get(new Random().nextInt(index.neighborListOf(currentNode).size))

  private def discoverNewVertex(nearest: Vector2) = {
    this.visited :+= nearest
  }

  private def log = "Agent " + id + ": "


  def getCurrentNode: Vector2 = currentNode

  def getNeighbours: List[ActorRef] = neighbours


  def chooseBehaviour(): Unit = {
    this.currentNode = computeNearest
    if (currentNode == currentDestination) {
      System.out.println(log + "Destination " + currentDestination + " = " + currentNode + " achieved! Choose the next one")
      currentDestination = selectRandomDestination
    }
    //ora scelgo destinazione casuale tra i vicini, potenzialmente torno indietro
  }

  //should be private
  def computeNeighbours: util.List[Vector2] = index.neighborListOf(currentNode)


  //computo il mio nodo di riferimento
  def computeNearest: Vector2 = {
    var nearest = currentNode
    var minDistance = getPosition.dst2(new Vector2(nearest.x, nearest.y))
    val list = computeNeighbours
    import scala.collection.JavaConversions._
    for (v <- list) {
      val distance = v.dst2(getPosition)
      if (distance < minDistance) {
        nearest = v
        minDistance = distance
      }
    }
    if (currentNode ne nearest) discoverNewVertex(nearest)
    currentNode = nearest
    if (!computeNeighbours.contains(getCurrentDestination)) { //se ho cambiato nodo di riferimento e questo non è collegato alla destinazione la ricalcolo
      currentDestination = selectRandomDestination
    }
    nearest
  }


  def getCurrentDestination: Vector2 = currentDestination

}
/**todo refresh vicini**/