package com.unibo.s3.main_system.characters

import java.util

import akka.actor.ActorRef
import com.badlogic.gdx.ai.steer.proximities.FieldOfViewProximity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.{MathUtils, Vector2}
import com.unibo.s3.main_system.characters.steer.behaviors._
import com.unibo.s3.main_system.characters.steer.{BaseMovableEntity, CustomLocation}
import org.jgrapht.UndirectedGraph
import org.jgrapht.alg.NeighborIndex
import org.jgrapht.graph.DefaultEdge

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

/**
  * Trait for a generic character
  */
trait Character {

  /**
    * Initial graph setup
    * @param g graph for setting
    */
  def setGraph(g: UndirectedGraph[Vector2, DefaultEdge]): Unit

  /**
    * Getter of character's known vertices
    * @return Vertices visited by the character
    */
  def getInformation: Iterable[Vector2]

  /**
    * Personal graph update with information from another character
    * @param colleagueList  Vertices visited by the other character
    */
  def updateGraph(colleagueList: Iterable[Vector2]): Unit

  /**
    * Getter for sight line length
    * @return The sight line length
    */
  def getSightLineLength : Float

  /**
    * Getter for the character's target
    * @return The character's target
    */
  def getTarget: Option[Target[BaseCharacter]]

}

abstract class BaseCharacter(vector2: Vector2, id : Int) extends BaseMovableEntity(vector2) with Character {
  private[this] var graph: UndirectedGraph[Vector2, DefaultEdge] = _

  private var currentNode : Option[Vector2] = Option[Vector2](new Vector2())
  private var previousNode : Option[Vector2] = Option[Vector2](new Vector2())

  private var visited = mutable.ArrayBuffer[Vector2]()
  private var visitedBuffer = mutable.ArrayBuffer[Vector2]()
  private var index : NeighborIndex[Vector2,DefaultEdge] = _
  private var currentDestination : Option[Vector2] = None

  private val sightLineLength : Float = 15f

  /*fov stuff*/
  private val fovAngle = 120f //degrees
  private val fovRadius = 5f
  private var coneOfView: FieldOfViewProximity[Vector2] = _

  /*init fov*/
  coneOfView  = new FieldOfViewProximity[Vector2](
    this, null, fovRadius, MathUtils.degreesToRadians * fovAngle)

  private val randomGenerator = Random

  def getId: Int = id

  def setGraph(g: UndirectedGraph[Vector2, DefaultEdge]): Unit = {
    this.graph = g
    currentNode = computeInitialNearestNode
    index = new NeighborIndex[Vector2, DefaultEdge](graph)
    currentDestination = selectRandomDestination()
    setNewDestination(currentDestination.get)
  }

  def getInformation: Iterable[Vector2] = this.visited

  def updateGraph(colleagueList: Iterable[Vector2]): Unit =
    visitedBuffer ++ colleagueList

  private def mergeCollegueGraph(): Unit = {
    visitedBuffer.foreach(v => if (!visited.contains(v)) visited += v)
    visitedBuffer.clear()
  }

  override def act(dt: Float): Unit = {
    super.act(dt)
    mergeCollegueGraph()
  }

  def getSightLineLength : Float = this.sightLineLength

  def getFieldOfView: FieldOfViewProximity[Vector2] = coneOfView

  private def setNewDestination(destination: Vector2) = {
    this.setComplexSteeringBehavior().avoidCollisionsWithWorld.arriveTo(new CustomLocation(destination)).buildPriority(true)
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

  def selectRandomDestination(): Option[Vector2] = {
    if (index != null && currentNode.isDefined) {
      val _neighbors = index.neighborListOf(currentNode.get).asScala
      val notVisited = _neighbors.filter(n => !visited.contains(n))

      val neighbors = if(notVisited.nonEmpty) notVisited else _neighbors

      val n = neighbors.size
      if (n == 1) neighbors.head
      if (n > 1) randomGenerator.nextInt(n)
    }
    currentNode
  }

  private def selectPriorityDestination() : Option[Vector2] = {

    var list = index.neighborListOf(currentNode.get).asScala
    var out : Option[Vector2] = None
    list = list.filter(node => !node.equals(previousNode.get))
    if(list.isEmpty){
      out = previousNode
    }else{
      val randIdx = MathUtils.random(0, list.size - 1)
      out = Some(list.filter(node => !node.equals(previousNode.get))(randIdx))
    }
    out
  }

  private def discoverNewVertex(nearest: Option[Vector2]) = {
    this.visited :+= nearest.get
  }

  def getCurrentNode: Option[Vector2] = currentNode

  def chooseBehaviour(): Unit = {
    this.currentNode = computeNearestVertex
  }

  private def computeNeighbours: Option[util.List[Vector2]] = {
    import scala.collection.JavaConversions._
    if (index == null){
      Option(List())
    }else{
      Option(index.neighborListOf(currentNode.get))
    }
  }

  private def computeNearestVertex: Option[Vector2] = {
    var nearest = currentNode
    var minDistance = getPosition.dst2(new Vector2(nearest.get.x, nearest.get.y))
    val list = computeNeighbours.get
    import scala.collection.JavaConversions._
    for (v <- list) {
      val distance = v.dst2(getPosition)
      if (distance < minDistance) {
        nearest = Some[Vector2](v)
        minDistance = distance
      }
    }

    (currentDestination, nearest) match {
      case (Some(cd), Some(n)) if cd.equals(n) =>
        discoverNewVertex(nearest)
        previousNode = currentNode
        currentNode = nearest
        currentDestination = selectRandomDestination()
        setNewDestination(currentDestination.get)
      case _ =>
    }

    if (!computeNeighbours.forall(p => p.contains(getCurrentDestination))) {
      currentDestination = selectPriorityDestination()
      setNewDestination(currentDestination.get)
    }
    nearest
  }

  def getCurrentDestination: Vector2 = currentDestination.getOrElse(new Vector2())

  override def equals(o: scala.Any): Boolean = {
    o match {
      case other: BaseCharacter => other.getId == id
      case _ => false
    }
  }
}

case class Guard(vector2: Vector2, id : Int) extends BaseCharacter(vector2, id){

  private[this] var fugitive: Option[Fugitive] = None

  override def getTarget: Option[Target[BaseCharacter]] = fugitive

  def hasTarget: Boolean = fugitive.isDefined

  def setFugitiveTarget(f: Option[Fugitive]): Unit = fugitive = f

}

case class Thief(vector2: Vector2, id : Int) extends BaseCharacter(vector2, id){

  private var pursuer: Option[Pursuer] = None

  private var hasReachedExit_ = false
  private var gotCaughtByGuard_ = false

  def gotCaughtByGuard: Boolean = gotCaughtByGuard_

  def hasReachedExit: Boolean = hasReachedExit_

  def setReachedExit(f: Boolean): Unit = hasReachedExit_ = f

  def setGotCaughtByGuard(f: Boolean): Unit = gotCaughtByGuard_ = f

  def hasTarget: Boolean = pursuer.isDefined

  def setPursuerTarget(p: Option[Pursuer]): Unit = pursuer = p

  override def getTarget: Option[Target[BaseCharacter]] = pursuer
}

