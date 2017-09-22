package com.unibo.s3.main_system.communication

import akka.actor.{Props, Stash, UntypedAbstractActor}
import com.unibo.s3.main_system.characters.{BaseCharacter, Guard}
import com.unibo.s3.main_system.characters.steer.behaviors.{Behaviors, Fugitive, Pursuer}
import com.unibo.s3.main_system.communication.Messages._

/**
  * Class used to manage a guard
  * @author Daniele Rosetti
  * @param guard Guard wrapped from actor
  */
class GuardActor(private[this] val guard: Guard) extends UntypedAbstractActor with Stash {

  private val captureThreshold = 8f

  private def notifyThiefCaught(thief: BaseCharacter): Unit =
    SystemManager.getLocalActor(GeneralActors.GAME_ACTOR) ! ThiefCaughtMsg(thief, guard)

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

      guard.getTarget match {
        case Some(Fugitive(thief)) =>
          Behaviors.onThiefCaught(thief, guard,
            captureThreshold, notifyThiefCaught(guard))
          thief.setPursuerTarget(Option(Pursuer(guard)))
        case _ =>
      }

    case _ =>
  }
}

/**
  * Companion object of GuardActor
  * @author Daniele Rosetti
  */
object GuardActor {
  def props(guard: Guard): Props = Props(new GuardActor(guard))
}

