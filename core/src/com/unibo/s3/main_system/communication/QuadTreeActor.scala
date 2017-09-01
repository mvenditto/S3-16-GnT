package com.unibo.s3.main_system.communication
import akka.actor.{ActorRef, Props, UntypedAbstractActor}
import com.unibo.s3.main_system.characters.BaseCharacter
import com.unibo.s3.main_system.communication.Messages.{AskNeighboursMsg, InitialSavingCharacter, RebuildQuadTreeMsg, SendNeighboursMsg}
import com.unibo.s3.main_system.world.spatial.{Bounds, QuadTreeNode}
import com.unibo.s3.main_system.communication.Messages._

import scala.collection.immutable.HashMap

class QuadTreeActor extends UntypedAbstractActor {

  private[this] var agentsTable = new HashMap[BaseCharacter, ActorRef]()
  private[this] val agentsTable = mutable.Map[BaseCharacter, ActorRef]()
  private[this] var root = QuadTreeNode[BaseCharacter](Bounds(0, 0, 60, 60))

  override def onReceive(message: Any): Unit = message match {
    case msg: MapSettingsMsg =>

    case msg: InitialSavingCharacterMsg =>
      agentsTable += msg.newCharacter -> msg.characterRef

    case RebuildQuadTreeMsg(characters) =>
      root = QuadTreeNode(Bounds(0, 0, 60, 60))
      characters.foreach(c => root.insert(c))


    case _: AskNeighboursMsg =>
      //calcolo i vicini
      var neighbours = List[ActorRef]()
      agentsTable.values.foreach(cop => neighbours :+= cop)
      getSender().tell(SendNeighboursMsg(neighbours), getSelf())
    case _: RebuildQuadTreeMsg =>
      //ricostruire il quadTree, i character con le loro posizioni li ho nel campo agentstable
    case _ => println("(quadTreeActor) message unknown:" + message)
  }
}

object QuadTreeActor {
  def props(): Props = Props(new QuadTreeActor())
}