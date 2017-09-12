package com.unibo.s3.main_system.characters

import java.util

import akka.actor.ActorRef
import com.badlogic.gdx.ai.steer.Steerable
import com.badlogic.gdx.ai.steer.proximities.FieldOfViewProximity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.{MathUtils, Vector2}
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

  private var currentNode : Option[Vector2] = Option[Vector2](new Vector2())
  private var previousNode : Option[Vector2] = Option[Vector2](new Vector2()) /**Nodo precedente**/
  private var neighbours = List[ActorRef]() //char?

  private var visited = List[Vector2]()
  private var index : NeighborIndex[Vector2,DefaultEdge] = _
  private var currentDestination : Option[Vector2] = None

  /*fov stuff*/
  private val fovAngle = 120f //degrees
  private val fovRadius = 5f
  private var coneOfView: FieldOfViewProximity[Vector2] = _

  /*init fov*/
  coneOfView  = new FieldOfViewProximity[Vector2](
    this, null, fovRadius, MathUtils.degreesToRadians * fovAngle)

  /*
  usage:
   import com.unibo.s3.main_system.util.GdxImplicits._ //for ..gdx.utils.Array -> Iterable[T]

   /*not filtered neighbors received from QuadTree*/
   val a: List[Steerable[Vector2]]().asGdxArray = ???
   // or
   val a2: com.badlogic.gdx.utils.Array[Steerable[Vector2]] = ???

   coneOfView.setAgents(a) //or a2
   val neighborsInFov = List[Steerable[Vector2]]()

   coneOfView.findNeighbors(new Proximity.ProximityCallback[Vector2] {
     override def reportNeighbor(n: Steerable[Vector2]): Boolean = {
       neighborsInFov :+= n
       true
      }
   })
  */


  def getId: Int = id

  override def setColor(color: Color): Unit = { this.color = color }

  override def getColor: Color = color

  def setGraph(g: UndirectedGraph[Vector2, DefaultEdge]): Unit = {
    this.graph = g
    currentNode = computeInitialNearestNode
    index = new NeighborIndex[Vector2, DefaultEdge](graph)
    currentDestination = selectRandomDestination
    System.out.println(log + "my destination is " + currentDestination)
    setNewDestination(currentDestination.get)
  }

  def addNeighbour(neighbour: ActorRef): Unit = {
    this.neighbours :+= neighbour
    this.nNeighbours += 1
  }

  def refreshNeighbours() : Unit = this.neighbours = List()

  def isNeighbour(possibleNeighbour : ActorRef) : Boolean = neighbours.contains(possibleNeighbour)

  def getInformation: List[Vector2] = this.visited

  def updateGraph(colleagueList: List[Vector2]): Unit = {
    this.nNeighbours -= 1
    //update lista
    for (v <- colleagueList) {
      if (!visited.contains(v)) visited:+=v
    }
    if (nNeighbours == 0) chooseBehaviour()
  }

  private def setNewDestination(destination: Vector2) = { //setta destinazione
    println(log + "Going to " + destination)
    this.setComplexSteeringBehavior.avoidCollisionsWithWorld.arriveTo(new CustomLocation(destination)).buildPriority(true)
  }

  private def computeInitialNearestNode = {
    var nearest = None: Option[Vector2]
    var minDistance = Float.MaxValue
    import scala.collection.JavaConversions._
    for (v <- graph.vertexSet) {
      val distance = v.dst2(getPosition)
      if (distance < minDistance) {
        nearest = Some[Vector2](v)
        minDistance = distance
      }
    }
    discoverNewVertex(nearest)
    nearest
  }

  private def selectRandomDestination = Option[Vector2](index.neighborListOf(currentNode.get).get(new Random().nextInt(index.neighborListOf(currentNode.get).size)))

  private def selectPriorityDestination : Option[Vector2] = {
    import scala.collection.JavaConversions._

    var list = index.neighborListOf(currentNode.get).toList
    var out : Option[Vector2] = None
    list = list.filter(node => !node.equals(previousNode.get))
    if(list.isEmpty){
      out = previousNode
    }else{
      out = Option[Vector2](scala.util.Random.shuffle(list.filter(node => !node.equals(previousNode.get))).get(0))
    }
   // println(log + "previous/current " + previousNode + " " + currentNode)
   // println(log + "OUT pre : " + list)
   // println(log + "OUT post : " + list.filter(node => !node.equals(previousNode.get)))

    out
  }

  private def discoverNewVertex(nearest: Option[Vector2]) = {
    this.visited :+= nearest.get
  }

  private def log = "Agent " + id + ": "

  def getCurrentNode: Option[Vector2] = currentNode

  def getNeighbours: List[ActorRef] = neighbours

  def chooseBehaviour(): Unit = {
    this.currentNode = computeNearest
    System.out.println("Choose behaviour, current: " + currentNode.get + " | previous: " + previousNode.get +" | destination: " + currentDestination.get)
    if (currentNode == currentDestination) {
      System.out.println()
      System.out.println()
      System.out.println()
      System.out.println(log + "Destination " + currentDestination + " = " + currentNode + " achieved! Choose the next one")
      System.out.println()
      System.out.println()
      System.out.println()
      currentDestination = selectRandomDestination
    }
    //ora scelgo destinazione casuale tra i vicini, potenzialmente torno indietro
  }

  //should be private
  def computeNeighbours: util.List[Vector2] = index.neighborListOf(currentNode.get)


  //computo il mio nodo di riferimento
  def computeNearest: Option[Vector2] = {
    var nearest = currentNode
    var minDistance = getPosition.dst2(new Vector2(nearest.get.x, nearest.get.y))
    val list = computeNeighbours
    import scala.collection.JavaConversions._
    for (v <- list) {
      val distance = v.dst2(getPosition)
      if (distance < minDistance) {
        nearest = Some[Vector2](v)
        minDistance = distance
      }
    }
    if (currentNode ne nearest){
      discoverNewVertex(nearest)
      System.out.println(log + "Cambio nodo di riferimento " + currentNode + " to " + nearest)
      previousNode = currentNode
      currentNode = nearest

      //currentDestination = selectRandomDestination
      currentDestination = selectPriorityDestination
      setNewDestination(currentDestination.get)
    }
    if (!computeNeighbours.contains(getCurrentDestination)) { //se ho cambiato nodo di riferimento e questo non è collegato alla destinazione la ricalcolo
      println(log + " current node NOT connected to destination")
      currentDestination = selectRandomDestination
    }
    nearest
  }


  def getCurrentDestination: Vector2 = currentDestination.getOrElse(new Vector2())

}