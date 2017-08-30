package com.unibo.s3.main_system.communication
import akka.actor.{ActorRef, Props, UntypedAbstractActor}
import com.badlogic.gdx.ai.steer.Steerable
import com.badlogic.gdx.math.Vector2
import com.unibo.s3.main_system.characters.BaseCharacter
import com.unibo.s3.main_system.communication.Messages.{ActMsg, AskNeighboursMsg, SendNeighboursMsg}
import com.unibo.s3.main_system.world.spatial.QuadTreeNode
import com.unibo.s3.main_system.communication.Messages.{AskNeighboursMsg, InitialSavingCharacter, SendNeighboursMsg}

import scala.collection.mutable

class QuadTreeActor extends UntypedAbstractActor {

  //map[BaseCharacter, ActorRef]
  var agentsTable : mutable.HashMap[BaseCharacter, ActorRef] = new mutable.HashMap[BaseCharacter, ActorRef]()

  override def onReceive(message: Any): Unit = message match {
    case msg: InitialSavingCharacter =>
      agentsTable.put(msg.newCharacter, msg.characterRef)



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