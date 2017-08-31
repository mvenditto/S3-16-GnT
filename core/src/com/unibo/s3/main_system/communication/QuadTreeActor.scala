package com.unibo.s3.main_system.communication
import akka.actor.{ActorRef, Props, UntypedAbstractActor}
import com.unibo.s3.main_system.characters.BaseCharacter
import com.unibo.s3.main_system.communication.Messages.{AskNeighboursMsg, InitialSavingCharacter, RebuildQuadTreeMsg, SendNeighboursMsg}
import com.unibo.s3.main_system.world.spatial.{Bounds, QuadTreeNode}

import scala.collection.mutable

class QuadTreeActor extends UntypedAbstractActor {

  private[this] val agentsTable = mutable.Map[BaseCharacter, ActorRef]()
  private[this] var root = QuadTreeNode[BaseCharacter](Bounds(0, 0, 60, 60))

  override def onReceive(message: Any): Unit = message match {
    case msg: InitialSavingCharacter =>
      agentsTable.put(msg.newCharacter, msg.characterRef)

    case RebuildQuadTreeMsg(characters) =>
      root = QuadTreeNode(Bounds(0, 0, 60, 60))
      characters.foreach(c => root.insert(c))

    case AskNeighboursMsg() =>
      /*
      val neighbors = root.rangeQuery(Bounds(0, 0, 10, 10))
        .map(c => agentsTable(c)).toList
      sender ! SendNeighboursMsg(neighbors)
      */
    case _ => println("(quadTreeActor) message unknown:" + message)
  }
}

object QuadTreeActor {
  def props(): Props = Props(new QuadTreeActor())
}