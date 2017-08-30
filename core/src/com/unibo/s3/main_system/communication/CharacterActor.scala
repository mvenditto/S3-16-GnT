package com.unibo.s3.main_system.communication

import akka.actor.{ActorRef, Props, UntypedAbstractActor}
import com.unibo.s3.main_system.characters.BaseCharacter
import com.unibo.s3.main_system.communication.Messages._

import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConverters._

class CharacterActor(val character: BaseCharacter) extends UntypedAbstractActor {

  //grafo in qualche struttura

  //BaseCharacter incapsulato

  override def onReceive(message: Any): Unit = message match {
    case _: ActMsg =>
      SystemManager.getInstance().getLocalActor("quadTreeActor").tell(AskNeighboursMsg(), getSelf())
    case msg: SendNeighboursMsg =>
      msg.neighbours.foreach(neighbour => neighbour.tell(SendCopInfoMsg(character.getInformations), getSelf()))
      //msg.neighbours.foreach(neighbour => character.addNeighbour(neighbour))
      msg.neighbours.filter(neighbour => !character.getNeighbours.contains(neighbour)).foreach(neighbour => character.addNeighbour(neighbour))
      println("cop: " + getSelf() + "| I have " + msg.neighbours.size() + " neighbours: " + character.getNeighbours)
    case msg: SendCopInfoMsg =>
      println("cop: " + getSelf() + "| info from: " + getSender() + ", visited vertices: " + msg.visitedVertices)
      character.updateGraph(msg.visitedVertices)
      println("cop: " + getSelf() + " known vertices: " + character.getInformations)
      //qui ho le info dell'altro poliziotto quindi poi posso fare quello che devo
    case msg: SetupGraphMsg =>
      println("cop: " + getSelf() + "| received graph")
      character.setGraph(msg.graph)
    case _ => println("(characterActor) message unknown:" + message)
  }
}

object CharacterActor {
  def props(baseCharacter: BaseCharacter): Props = Props(new CharacterActor(baseCharacter))
}
