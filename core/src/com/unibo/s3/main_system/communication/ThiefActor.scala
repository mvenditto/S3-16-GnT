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

class ThiefActor(private[this] val thief: Thief) extends UntypedAbstractActor with Stash {
  private[this] val captureThreshold = 8f
  private[this] val exitReachedThreshold = (Wall.WALL_THICKNESS * 2) * 4f

  context.become(setGraph())

  private def canAct: Boolean = !thief.hasReachedExit && !thief.gotCaughtByGuard

  private def notifyThiefReachedExit(): Unit =
    SystemManager.getLocalActor(GeneralActors.GAME_ACTOR) ! ThiefReachedExitMsg(thief)

  private def notifyThiefCaught(g: BaseCharacter): Unit =
    SystemManager.getLocalActor(GeneralActors.GAME_ACTOR) ! ThiefCaughtMsg(thief, g)

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

        if (thief.hasTarget
          && (guards.isEmpty || !guards.exists(g => g.equals(thief.getTarget.get)))) {
          Behaviors.randomPatrol(thief)
          thief.setPursuerTarget(None)
        }

        val nearestGuard = BehaviorUtils.nearest[BaseCharacter](thief, guards).get
        Behaviors.evadeFromGuard(thief, nearestGuard)
        nearestGuard match {
          case guard: Guard =>
            Behaviors.onThiefCaught(thief, guard,
              captureThreshold, notifyThiefCaught(guard))
            thief.setPursuerTarget(Option(Pursuer(guard)))
          case _ =>
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

    case m => println("(thiefActor) message unknown:" + m)
  }



  def log() : String = "[CHARACTER " + thief.getId + "]: "
}

object ThiefActor {
  def props(thief: Thief): Props = Props(new ThiefActor(thief))
}
