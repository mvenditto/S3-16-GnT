package com.unibo.s3.main_system.communication

import akka.actor.{Props, Stash, UntypedAbstractActor}
import com.badlogic.gdx.math.Vector2
import com.unibo.s3.main_system.characters.Guard
import com.unibo.s3.main_system.characters.steer.behaviors.Behaviors
import com.unibo.s3.main_system.communication.Messages._
import org.jgrapht.UndirectedGraph
import org.jgrapht.graph.DefaultEdge

class GuardActor(private[this] val guard: Guard) extends UntypedAbstractActor with Stash {

  private[this] var graph: UndirectedGraph[Vector2, DefaultEdge] = _

  context.become(sendGraph())

  override def onReceive(message: Any): Unit = {}

  private def sendGraph(): Receive = {
    case msg: SendGraphMsg =>
      guard.setGraph(msg.graph)
      context.become(normalBehave())
      unstashAll()

    case _: ActMsg =>

    case _ => stash()
  }

  private def normalBehave(): Receive = {
    case ActMsg(dt) =>
      guard.act(dt)
      val qt = SystemManager.getLocalActor(GeneralActors.QUAD_TREE_ACTOR)
      qt ! AskNeighboursWithinFovMsg(this.guard)
      qt ! AskNearbyGuardsMsg(guard)
      Behaviors.patrolIfHasNoTarget(guard)

    case SendGuardInfoMsg(visitedNodes) =>
      guard.updateGraph(visitedNodes)

    case SendNearbyGuardsMsg(neighbors) =>
      neighbors.foreach(n => n ! SendGuardInfoMsg(guard.getInformation))

    case SendThievesInProximityMsg(thieves) =>
      Behaviors.guardPursueThieves(guard, thieves)

    case _ =>
  }

  def log() : String = "[CHARACTER " + guard.getId + "]: "
}

object GuardActor {
  def props(guard: Guard): Props = Props(new GuardActor(guard))
}

