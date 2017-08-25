package com.unibo.s3.main_system.communication
import akka.actor.{ActorRef, Props, UntypedAbstractActor}
import com.unibo.s3.main_system.characters.BaseCharacter
import com.unibo.s3.main_system.communication.Messages.{askNeighbourMsg, responseNeighbourMsg}

import scala.collection.mutable

class QuadTreeActor extends UntypedAbstractActor{

  //map[BaseCharacter, ActorRef]
  var agentsTable : mutable.HashMap[BaseCharacter, ActorRef] = _

  override def onReceive(message: Any): Unit = message match {
    case _: askNeighbourMsg =>
      //calcolo i vicini
      getSender().tell(responseNeighbourMsg(List(
        SystemManager.getInstance().getLocalActor("copOne"),
        SystemManager.getInstance().getLocalActor("copTwo"),
        SystemManager.getInstance().getLocalActor("copThree"))), getSelf())
    case _ => println("(quadTreeActor) message unknown:" + message)
  }
}

object QuadTreeActor {
  def props(): Props = Props(new QuadTreeActor())
}