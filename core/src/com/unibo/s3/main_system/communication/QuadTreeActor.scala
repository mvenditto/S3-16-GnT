package com.unibo.s3.main_system.communication
import akka.actor.{ActorRef, Props, UntypedAbstractActor}
import com.unibo.s3.main_system.characters.BaseCharacter
import com.unibo.s3.main_system.communication.Messages.{AskNeighboursMsg, InitialSavingCharacterMsg, MapSettingsMsg, SendNeighboursMsg}

import scala.collection.immutable.HashMap

class QuadTreeActor extends UntypedAbstractActor {

  private[this] var agentsTable = new HashMap[BaseCharacter, ActorRef]()

  override def onReceive(message: Any): Unit = message match {
    case msg: MapSettingsMsg =>

    case msg: InitialSavingCharacterMsg =>
      agentsTable += msg.newCharacter -> msg.characterRef



    case _: AskNeighboursMsg =>
      //calcolo i vicini
      var neighbours = List[ActorRef]()
      agentsTable.values.foreach(cop => neighbours :+= cop)
      getSender().tell(SendNeighboursMsg(neighbours), getSelf())
    case _ => println("(quadTreeActor) message unknown:" + message)
  }
}

object QuadTreeActor {
  def props(): Props = Props(new QuadTreeActor())
}