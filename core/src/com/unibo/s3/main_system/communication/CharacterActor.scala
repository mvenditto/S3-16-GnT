package com.unibo.s3.main_system.communication

import akka.actor.{Props, UntypedAbstractActor}
import com.badlogic.gdx.math.Vector2
import com.unibo.s3.main_system.characters.{BaseCharacter}
import com.unibo.s3.main_system.communication.Messages._
import org.jgrapht.UndirectedGraph
import org.jgrapht.graph.DefaultEdge

import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConverters._

class CharacterActor(private[this] val character: BaseCharacter) extends UntypedAbstractActor {

  private[this] var graph: UndirectedGraph[Vector2, DefaultEdge] = _

  override def onReceive(message: Any): Unit = message match {
    case ActMsg(dt) =>
      character.act(dt)
      SystemManager.getLocalActor("quadTreeActor").tell(AskNeighboursMsg(this.character), getSelf())
      println(log() + "Act received")
      println(log() + "Current node/destination: " + character.getCurrentNode.getOrElse("Not definied") + "," + character.getCurrentDestination)
      character.chooseBehaviour()
   /* case msg: SendNeighboursMsg =>
      //refresha vicini
      character.refreshNeighbours()
      println("My neighbours are: " + msg.neighbours.filter(neighbour => !neighbour.equals(getSelf())))
      msg.neighbours.filter(neighbour => !neighbour.equals(getSelf())).foreach(neighbour => neighbour.tell(SendCopInfoMsg(character.getInformation), getSelf()))
      //msg.neighbours.filter(neighbour => !character.getNeighbours.contains(neighbour)).foreach(neighbour => character.addNeighbour(neighbour))
      msg.neighbours.filter(neighbour => !neighbour.equals(getSelf())).foreach(neighbour => character.addNeighbour(neighbour))
      //verifica funzionamento
      println(log() + "Neibours in list " + character.getNeighbours)
      println(log() + "Current node/destination " + character.getCurrentNode + "/" + character.getCurrentDestination)

    case msg: SendCopInfoMsg =>
      println(log() + "Infos received")
      println("cop: " + getSelf() + "| info from: " + getSender() + ", visited vertices: " + msg.visitedVertices)
      character.updateGraph(msg.visitedVertices)
      println("cop: " + getSelf() + " known vertices: " + character.getInformation)
      //qui ho le info dell'altro poliziotto quindi poi posso fare quello che devo
*/
    case msg: SendGraphMsg=>
      System.out.println(log() + "Initial graph received")
      println("cop: " + getSelf() + "| received graph")
      character.setGraph(msg.graph)

    case _ => //println("(characterActor) message unknown:" + message)
  }

  def log() : String = "[CHARACTER " + character.getId + "]: "
}

object CharacterActor {
  def props(baseCharacter: BaseCharacter): Props = Props(new CharacterActor(baseCharacter))
}

