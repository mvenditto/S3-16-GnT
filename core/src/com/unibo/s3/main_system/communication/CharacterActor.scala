package com.unibo.s3.main_system.communication

import akka.actor.{Props, UntypedAbstractActor}
import com.badlogic.gdx.math.Vector2
import com.unibo.s3.main_system.characters.BaseCharacter
import com.unibo.s3.main_system.communication.Messages._
import org.jgrapht.UndirectedGraph
import org.jgrapht.graph.DefaultEdge

import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConverters._

class CharacterActor(private[this] val character: BaseCharacter) extends UntypedAbstractActor {

  private[this] var graph: UndirectedGraph[Vector2, DefaultEdge] = _


  override def onReceive(message: Any): Unit = message match {
    case _: ActMsg =>
      SystemManager.getInstance().getLocalActor("quadTreeActor").tell(AskNeighboursMsg(this.character), getSelf())

    case msg: SendNeighboursMsg =>
      msg.neighbours.foreach(neighbour => neighbour.tell(SendCopInfoMsg(character.getInformations.toList), getSelf()))
      //msg.neighbours.foreach(neighbour => character.addNeighbour(neighbour))
      msg.neighbours.filter(neighbour => !character.getNeighbours.contains(neighbour)).foreach(neighbour => character.addNeighbour(neighbour))

    case msg: SendCopInfoMsg =>
      println("cop: " + getSelf() + "| info from: " + getSender() + ", visited vertices: " + msg.visitedVertices)
      character.updateGraph(msg.visitedVertices.asJava)
      println("cop: " + getSelf() + " known vertices: " + character.getInformations)
      //qui ho le info dell'altro poliziotto quindi poi posso fare quello che devo

    //case ricevo grafo iniziale => salvo grafo iniziale

    case msg: SendGraphMsg=>
      println("cop: " + getSelf() + "| received graph")
      character.setGraph(msg.graph)

    case _ => println("(characterActor) message unknown:" + message)
  }
}

object CharacterActor {
  def props(baseCharacter: BaseCharacter): Props = Props(new CharacterActor(baseCharacter))
}
