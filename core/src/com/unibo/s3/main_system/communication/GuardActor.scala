package com.unibo.s3.main_system.communication

import akka.actor.{Props, Stash, UntypedAbstractActor}
import com.badlogic.gdx.math.Vector2
import com.unibo.s3.main_system.characters.BaseCharacter
import com.unibo.s3.main_system.characters.Guard.Guard
import com.unibo.s3.main_system.communication.Messages._
import org.jgrapht.UndirectedGraph
import org.jgrapht.graph.DefaultEdge

import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConverters._

class GuardActor(private[this] val guard: Guard) extends UntypedAbstractActor with Stash {

  private[this] var graph: UndirectedGraph[Vector2, DefaultEdge] = _

  /*
  override def onReceive(message: Any): Unit = message match {
    case ActMsg(dt) =>
      guard.act(dt)
      SystemManager.getLocalActor("quadTreeActor").tell(AskNeighboursWithFovMsg(this.guard), getSelf())
      //println(log() + "Act received")
      //println(log() + "Current node/destination: " + character.getCurrentNode.getOrElse("Not definied") + "," + character.getCurrentDestination)

      if(!guard.hasTarget) guard.chooseBehaviour()

    case msg: SendNeighboursMsg =>
      //refresha vicini
      //character.refreshNeighbours()

      //println(log + "My neighbours are: " + msg.neighbours.filter(neighbour => !neighbour.equals(getSelf())))

      msg.neighbours.filter(neighbour => !neighbour.equals(getSelf())).foreach(neighbour => guard.addNeighbour(neighbour))
      ///msg.neighbours.filter(neighbour => !neighbour.equals(getSelf())).foreach(neighbour => neighbour.tell(SendCopInfoMsg(character.getInformation), getSelf()))
      //msg.neighbours.filter(neighbour => !character.getNeighbours.contains(neighbour)).foreach(neighbour => character.addNeighbour(neighbour))
      //msg.neighbours.filter(neighbour => !neighbour.equals(getSelf())).foreach(neighbour => character.addNeighbour(neighbour))
      //verifica funzionamento
      //println(log() + "Neibours in list " + character.getNeighbours)
      //println(log() + "Current node/destination " + character.getCurrentNode + "/" + character.getCurrentDestination)
/*
    case msg: SendCopInfoMsg =>
      character.updateGraph(msg.visitedVertices)
*/
    case SendThievesInProximityMsg(thieves) =>
      //thieves.foreach(t => println(log() + "Ladro! " + t))
      guard.chooseTarget(thieves)


    case msg: SendGraphMsg =>
      //println(log() + "Initial graph received")
      //println("cop: " + getSelf() + "| received graph")
      guard.setGraph(msg.graph)

    case _ => //println("(guardActor) message unknown:" + message)
  }*/

  context.become(sendGraph())

  override def onReceive(message: Any): Unit = {}

  private def sendGraph(): Receive = {
    case msg: SendGraphMsg =>
      //println(log() + "Initial graph received")
      //println("cop: " + getSelf() + "| received graph")
      guard.setGraph(msg.graph)
      context.become(normalBehave())
      unstashAll()

    case _: ActMsg =>

    case _ => stash()
  }

  private def normalBehave(): Receive = {
    case ActMsg(dt) =>
      guard.act(dt)
      SystemManager.getLocalActor("quadTreeActor").tell(AskNeighboursWithFovMsg(this.guard), getSelf())
      //println(log() + "Act received")
      //println(log() + "Current node/destination: " + character.getCurrentNode.getOrElse("Not definied") + "," + character.getCurrentDestination)

      if(!guard.hasTarget) guard.chooseBehaviour()

    case msg: SendNeighboursMsg =>
      //refresha vicini
      //character.refreshNeighbours()

      //println(log + "My neighbours are: " + msg.neighbours.filter(neighbour => !neighbour.equals(getSelf())))

      msg.neighbours.filter(neighbour => !neighbour.equals(getSelf())).foreach(neighbour => guard.addNeighbour(neighbour))
    ///msg.neighbours.filter(neighbour => !neighbour.equals(getSelf())).foreach(neighbour => neighbour.tell(SendCopInfoMsg(character.getInformation), getSelf()))
    //msg.neighbours.filter(neighbour => !character.getNeighbours.contains(neighbour)).foreach(neighbour => character.addNeighbour(neighbour))
    //msg.neighbours.filter(neighbour => !neighbour.equals(getSelf())).foreach(neighbour => character.addNeighbour(neighbour))
    //verifica funzionamento
    //println(log() + "Neibours in list " + character.getNeighbours)
    //println(log() + "Current node/destination " + character.getCurrentNode + "/" + character.getCurrentDestination)
    /*
        case msg: SendCopInfoMsg =>
          character.updateGraph(msg.visitedVertices)
    */
    case SendThievesInProximityMsg(thieves) =>
      //thieves.foreach(t => println(log() + "Ladro! " + t))
      guard.chooseTarget(thieves)

    case _ => //println("(guardActor) message unknown:" + message)
  }

  def log() : String = "[CHARACTER " + guard.getId + "]: "
}

object GuardActor {
  def props(guard: Guard): Props = Props(new GuardActor(guard))
}

