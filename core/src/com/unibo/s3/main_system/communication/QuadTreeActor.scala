package com.unibo.s3.main_system.communication
import akka.actor.{ActorRef, Props, UntypedAbstractActor}
import com.badlogic.gdx.ai.steer.Steerable
import com.badlogic.gdx.math.Vector2
import com.unibo.s3.main_system.characters.BaseCharacter
import com.unibo.s3.main_system.communication.Messages.{ActMsg, AskNeighboursMsg, SendNeighboursMsg}
import com.unibo.s3.main_system.world.spatial.QuadTreeNode

import scala.collection.mutable

class QuadTreeActor extends UntypedAbstractActor {

  //map[BaseCharacter, ActorRef]
  var agentsTable : mutable.HashMap[BaseCharacter, ActorRef] = _

  override def onReceive(message: Any): Unit = message match {



    case _: AskNeighboursMsg =>
      //calcolo i vicini
      getSender().tell(SendNeighboursMsg(List(
        SystemManager.getInstance().getLocalActor("copOne"),
        SystemManager.getInstance().getLocalActor("copTwo"),
        SystemManager.getInstance().getLocalActor("copThree"))), getSelf())
    case _ => println("(quadTreeActor) message unknown:" + message)
  }
}

object QuadTreeActor {
  def props(): Props = Props(new QuadTreeActor())
}