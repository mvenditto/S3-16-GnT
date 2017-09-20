package com.unibo.s3.main_system.characters

import java.util

import akka.actor.ActorRef
import com.badlogic.gdx.ai.steer.proximities.FieldOfViewProximity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.{MathUtils, Vector2}
import com.unibo.s3.main_system.characters.steer.{BaseMovableEntity, CustomLocation}
import org.jgrapht.UndirectedGraph
import org.jgrapht.alg.NeighborIndex
import org.jgrapht.graph.DefaultEdge

import scala.util.Random
import com.unibo.s3.main_system.util.GdxImplicits._

trait Character{

  /**initial graph setting*/
  def setGraph(g: UndirectedGraph[Vector2, DefaultEdge]): Unit

  /**adding a character to neighbours*/
  def addNeighbour(neighbour: ActorRef): Unit

  /**verify il a character is already neighbour (maybe not public)*/
  def isNeighbour(possibleNeighbour : ActorRef) : Boolean

  /**getting character infos (maybe guard exclusive)*/
  def getInformation: List[Vector2]

  /**graph update (maybe guard exclusive)*/
  def updateGraph(colleagueList: List[Vector2]): Unit

  /**getting sight line lenght (maybe thief exclusive)*/
  def getSightLineLength : Float
}

abstract class BaseCharacter(vector2: Vector2, id : Int) extends BaseMovableEntity(vector2) with Character{

  private var color : Color = _

  private[this] var graph: UndirectedGraph[Vector2, DefaultEdge] = _

  private var nNeighbours = 0

  private var currentNode : Option[Vector2] = Option[Vector2](new Vector2())
  private var previousNode : Option[Vector2] = Option[Vector2](new Vector2()) /**Nodo precedente**/
  private var neighbours = List[ActorRef]()

  private var visited = List[Vector2]()
  private var index : NeighborIndex[Vector2,DefaultEdge] = _
  private var currentDestination : Option[Vector2] = None

  private val sightLineLength : Float = 15

  /*fov stuff*/
  private val fovAngle = 120f //degrees
  private val fovRadius = 5f
  private var coneOfView: FieldOfViewProximity[Vector2] = _

  /*init fov*/
  coneOfView  = new FieldOfViewProximity[Vector2](
    this, null, fovRadius, MathUtils.degreesToRadians * fovAngle)

  private val randomGenerator = Random

  def getId: Int = id

  override def setColor(color: Color): Unit = { this.color = color }

  override def getColor: Color = color

  def setGraph(g: UndirectedGraph[Vector2, DefaultEdge]): Unit = {
    this.graph = g
    currentNode = computeInitialNearestNode
    index = new NeighborIndex[Vector2, DefaultEdge](graph)
    currentDestination = selectRandomDestination
    //System.out.println(log + "my destination is " + currentDestination)
    setNewDestination(currentDestination.get)
  }

  def addNeighbour(neighbour: ActorRef): Unit = {
    this.neighbours :+= neighbour
    this.nNeighbours += 1
  }

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

  def getSightLineLength : Float = this.sightLineLength

  def getFieldOfView = coneOfView

  private def setNewDestination(destination: Vector2) = { //setta destinazione
    //println(log + "Going to " + destination)
    this.setComplexSteeringBehavior.avoidCollisionsWithWorld.arriveTo(new CustomLocation(destination)).buildPriority(true)
  }

  private def refreshNeighbours() : Unit = this.neighbours = List()

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

  protected def selectRandomDestination = {
    if (index == null){
      Option(new Vector2())
    }else{
      Option[Vector2](index.neighborListOf(currentNode.get).get(new Random().nextInt(index.neighborListOf(currentNode.get).size)))
    }
  }

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

  def getCurrentNode: Option[Vector2] = currentNode

  def getNeighbours: List[ActorRef] = neighbours

  def chooseBehaviour(): Unit = {
    this.currentNode = computeNearestVertex
    ////System.out.println("Choose behaviour, current: " + currentNode.get + " | previous: " + previousNode.get +" | destination: " + currentDestination.get)
    if (currentNode == currentDestination) {

      ////System.out.println(log + "Destination " + currentDestination + " = " + currentNode + " achieved! Choose the next one")

      currentDestination = selectRandomDestination
    }
    //ora scelgo destinazione casuale tra i vicini, potenzialmente torno indietro
  }


  private def computeNeighbours: Option[util.List[Vector2]] = {
    import scala.collection.JavaConversions._
    if (index == null){
      Option(List())
    }else{
      Option(index.neighborListOf(currentNode.get))
    }
  }


  //computo il mio nodo di riferimento
  private def computeNearestVertex: Option[Vector2] = {
    var nearest = currentNode
    var minDistance = getPosition.dst2(new Vector2(nearest.get.x, nearest.get.y))
    var list = computeNeighbours.get
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
      ////System.out.println(log + "Cambio nodo di riferimento " + currentNode + " to " + nearest)
      previousNode = currentNode
      currentNode = nearest

      //currentDestination = selectRandomDestination
      currentDestination = selectPriorityDestination
      setNewDestination(currentDestination.get)
    }
    if (!computeNeighbours.contains(getCurrentDestination)) { //se ho cambiato nodo di riferimento e questo non è collegato alla destinazione la ricalcolo
      //println(log + " current node NOT connected to destination")
      currentDestination = selectRandomDestination
    }
    nearest
  }

  def getCurrentDestination: Vector2 = currentDestination.getOrElse(new Vector2())

  private def log = "Agent " + id + ": "

  override def equals(o: scala.Any): Boolean = {
    o match {
      case other: BaseCharacter => other.getId == id
      case _ => false
    }
  }
}

object Guard {
  def apply(vector2: Vector2, id: Int): Guard = Guard(vector2, id)

  case class Guard(vector2: Vector2, id : Int) extends BaseCharacter(vector2, id){

    private[this] var target: Option[BaseCharacter] = None

    def hasTarget: Boolean = target.isDefined

    private def getNearestTarget(possibleTargets: Iterable[BaseCharacter]): BaseCharacter = {
      val thisPos = getPosition
      possibleTargets.toList.sortWith(_.getPosition.dst2(thisPos) < _.getPosition.dst2(thisPos)).head
    }

    def chooseTarget(possibleTargets: Iterable[BaseCharacter]): Unit = {

      if (possibleTargets.isEmpty) {
        if (target.isDefined) {
        val patrol = setComplexSteeringBehavior()
          .avoidCollisionsWithWorld()
          selectRandomDestination.foreach(d => patrol.arriveTo(d))
          patrol.buildPriority(true)
          target = None
        }
      }

      if (target.isEmpty && possibleTargets.nonEmpty) {
        pursueTarget(getNearestTarget(possibleTargets))
      }

      if (target.isDefined && !possibleTargets.exists(t => t.getId == target.get.getId)) {
        if (possibleTargets.nonEmpty)
          pursueTarget(getNearestTarget(possibleTargets))
      }

    }

    private def pursueTarget(t: BaseCharacter): Unit = {

      val pursue = this.setComplexSteeringBehavior()
        .pursue(t)
        .arriveTo(getCurrentDestination)
        .buildBlended(Array(1.0f, 0.5f), false)

      this.setComplexSteeringBehavior()
        .avoidCollisionsWithWorld()
        .add(pursue)
        .buildPriority(true)

      target = Option(t)

    }

  }
}

object Thief {
  def apply(vector2: Vector2, id: Int): Thief = Thief(vector2, id)

  case class Thief(vector2: Vector2, id : Int) extends BaseCharacter(vector2, id){

    private var target: Option[BaseCharacter] = None

    def hasTarget: Boolean = target.isDefined

    def chooseTarget(possibleTargets: Iterable[BaseCharacter]): Unit = {


      if (possibleTargets.isEmpty) {
        if (target.isDefined) {
          val patrol = setComplexSteeringBehavior()
            .avoidCollisionsWithWorld()
          selectRandomDestination.foreach(d => patrol.arriveTo(d))
          patrol.buildPriority(true)
          target = None
        }
      }

      if (target.isEmpty && possibleTargets.nonEmpty) evadeTarget(possibleTargets.head)

      if (target.nonEmpty && possibleTargets.nonEmpty) {
        if (target.head.getPosition.dst2(getPosition) > possibleTargets.head.getPosition.dst2(getPosition)) {
          evadeTarget(possibleTargets.head)
        }
      }


    }

    private def evadeTarget(t: BaseCharacter): Unit = {

      val evade = this.setComplexSteeringBehavior()
        .evadeFrom(t)
        .arriveTo(getCurrentDestination)
        .buildBlended(Array(2.0f, 0.5f), false)

      this.setComplexSteeringBehavior()
        .avoidCollisionsWithWorld()
        .add(evade)
        .buildPriority(true)

      target = Option(t)
    }

  }
}
