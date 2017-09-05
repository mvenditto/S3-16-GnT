package com.unibo.s3.main_system.communication
import akka.actor.{ActorRef, Props, UntypedAbstractActor}

import com.unibo.s3.main_system.characters.BaseCharacter
import com.unibo.s3.main_system.communication.Messages.{AskNeighboursMsg, InitialSavingCharacterMsg, RebuildQuadTreeMsg, SendNeighboursMsg}
import com.unibo.s3.main_system.world.spatial.{Bounds, QuadTreeNode}

import com.unibo.s3.main_system.communication.Messages._

import scala.collection.immutable.HashMap

class QuadTreeActor extends UntypedAbstractActor {

  private[this] var agentsTable = new HashMap[BaseCharacter, ActorRef]()
  private[this] var bounds = Bounds(0, 0, 100, 100)
  private[this] var root = QuadTreeNode[BaseCharacter](Bounds(0, 0, 60, 60))
  private[this] val queryRadius = 5f

  override def onReceive(message: Any): Unit = message match {
    case MapSettingsMsg(w, h) =>
      bounds = Bounds(0, 0, w, h)

    case msg: InitialSavingCharacterMsg =>
      agentsTable += (msg.newCharacter -> msg.characterRef)

    case RebuildQuadTreeMsg() =>
      root = QuadTreeNode(bounds)
      agentsTable.keys.foreach(c => root.insert(c))

    case AskNeighboursMsg(character) =>
      val pos = character.getPosition.cpy().sub(queryRadius, queryRadius)
      val twiceQueryRadius= 2 * queryRadius
      val neighbours = root.rangeQuery(
        Bounds(pos.x, pos.y, twiceQueryRadius, twiceQueryRadius))
      sender ! SendNeighboursMsg(neighbours.map(c => agentsTable(c)).toList)

    case AskAllCharactersMsg =>
      sender ! SendAllCharactersMsg(agentsTable.keys)

    case _ => println("(quadTreeActor) message unknown:" + message)
  }
}

object QuadTreeActor {
  def props(): Props = Props(new QuadTreeActor())
}