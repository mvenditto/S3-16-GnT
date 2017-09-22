package com.unibo.s3.main_system.communication

import akka.actor.{Props, Stash, UntypedAbstractActor}
import com.badlogic.gdx.math.Vector2
import com.unibo.s3.main_system.characters.steer.behaviors.BehaviorUtils._
import com.unibo.s3.main_system.characters.steer.behaviors.{BehaviorUtils, Behaviors, Pursuer}
import com.unibo.s3.main_system.characters.{BaseCharacter, Guard, Thief}
import com.unibo.s3.main_system.communication.Messages._
import com.unibo.s3.main_system.game.Wall
import com.unibo.s3.main_system.world.Exit
import com.unibo.s3.main_system.world.actors.{AskObjectOnSightLineMsg, ObjectOnSightLineMsg}

/**
  * Class used to manage a thief
  * @author Daniele Rosetti
  * @param thief Thief wrapped from actor
  */
class ThiefActor(private[this] val thief: Thief) extends UntypedAbstractActor with Stash {
  private val exitReachedThreshold = (Wall.WALL_THICKNESS * 2) * 4f

  context.become(setGraph())

  private def canAct: Boolean = !thief.hasReachedExit && !thief.gotCaughtByGuard

  private def notifyThiefReachedExit(): Unit =
    SystemManager.getLocalActor(GeneralActors.GAME_ACTOR) ! ThiefReachedExitMsg(thief)

  override def onReceive(message: Any): Unit = {}

  private def setGraph(): Receive = {
    case msg: SendGraphMsg =>
      this.thief.setGraph(msg.graph)
      context.become(normalBehave())
      unstashAll()

    case _: ActMsg =>

    case _ =>
      stash()
  }

  private def normalBehave(): Receive =  {
    case msg: ActMsg =>
      this.thief.act(msg.dt)
      if (canAct) Behaviors.patrolIfHasNoTarget(thief) else thief.setPursuerTarget(None)
      val wa = SystemManager.getLocalActor(GeneralActors.WORLD_ACTOR)
      wa ! AskObjectOnSightLineMsg(
        thief.getPosition, thief.getLinearVelocity, thief.getSightLineLength)

    case SendGuardsInProximityMsg(guards) =>
      if (canAct) {

        if (thief.hasTarget) {

          val pursuerInRange =
            thief.getTarget.forall(t => t.get.getPosition.dst2(thief.getPosition) <= 15f)

          if (!pursuerInRange) {
            thief.setPursuerTarget(None)
            Behaviors.randomPatrol(thief)
          }
        }
        //val nearestGuard = BehaviorUtils.nearest[BaseCharacter](thief, guards).get

        if (guards.nonEmpty) {
          if (thief.getTarget.isEmpty) Behaviors.evadeFromGuard(thief, guards.head)
          else if (guards.head.getPosition.dst2(thief.getPosition)
            < thief.getTarget.get.get.getPosition.dst2(thief.getPosition)) {
            Behaviors.evadeFromGuard(thief, guards.head)
          }
        }
      }

    case SendNeighboursMsg(_) =>

    case msg: SendGraphMsg =>
      this.thief.setGraph(msg.graph)

    case  ObjectOnSightLineMsg(bd) =>
      val exits = bd.filter(b => b.bodyType.contains(Exit))
        .map(b => b.userData match {case Some(x: Vector2) => x})

      if(canAct && exits.nonEmpty) {
        val nearestExit = nearest(thief.getPosition, exits).get
        Behaviors.runToLocation(thief, nearestExit)
        Behaviors.onThiefExit(thief, nearestExit,
          exitReachedThreshold, notifyThiefReachedExit())
      }
  }
}

/**
  * Companion object of ThiefActor
  * @author Daniele Rosetti
  */
object ThiefActor {
  def props(thief: Thief): Props = Props(new ThiefActor(thief))
}
