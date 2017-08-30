package com.unibo.s3.main_system.communication

import akka.actor.{Props, UntypedAbstractActor}
import com.unibo.s3.main_system.characters.BaseCharacter
import com.unibo.s3.main_system.communication.Messages.{ActMsg, AskNeighboursMsg, SendCopInfoMsg, SendNeighboursMsg}

class CharacterActor(val character: BaseCharacter) extends UntypedAbstractActor {

  //grafo in qualche struttura

  //BaseCharacter incapsulato

  override def onReceive(message: Any): Unit = message match {
    case _: ActMsg =>
      SystemManager.getInstance().getLocalActor("quadTreeActor").tell(AskNeighboursMsg(), getSelf())
    case msg: SendNeighboursMsg =>
      msg.neighbours.foreach(neighbour => neighbour.tell(SendCopInfoMsg(), getSelf()))
      //dico sotto i vicini
    case msg: SendCopInfoMsg =>
      println("cop: " + getSelf() + "| info from: " + getSender())
      //qui ho le info dell'altro poliziotto quindi poi posso fare quello che devo

    //case ricevo grafo iniziale => salvo grafo iniziale
    //case
    case _ => println("(characterActor) message unknown:" + message)
  }
}

object CharacterActor {
  def props(baseCharacter: BaseCharacter): Props = Props(new CharacterActor(baseCharacter))
}
