package com.unibo.s3.main_system.communication

import akka.actor.{Props, Stash, UntypedAbstractActor}
import com.unibo.s3.main_system.characters.Thief.Thief
import com.unibo.s3.main_system.communication.Messages._
import com.unibo.s3.main_system.world.Exit
import com.unibo.s3.main_system.world.actors.{AskObjectOnSightLineMsg, ObjectOnSightLineMsg}

class ThiefActor(private[this] val thief: Thief) extends UntypedAbstractActor with Stash {
  var captureThreshold = 2f

  context.become(setGraph())

  private def canAct: Boolean = !thief.hasReachedExit && !thief.gotCaughtByGuard

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
      if (canAct) this.thief.chooseBehaviour()
      val wa = SystemManager.getLocalActor(GeneralActors.WORLD_ACTOR)
      wa ! AskObjectOnSightLineMsg(
        thief.getPosition, thief.getLinearVelocity, thief.getSightLineLength)

    case SendGuardsInProximityMsg(guards) =>
      if (canAct) thief.chooseTarget(guards)
      if (canAct) {
        val nearestGuard = thief.getTarget
        if (nearestGuard.isDefined) {
          val g = nearestGuard.get
          val dist =  g.getPosition.dst2(thief.getPosition)
          if (dist <= captureThreshold) {
            thief.setGotCaughtByGuard(true)
            SystemManager
              .getLocalActor(GeneralActors.GAME_ACTOR) ! ThiefCaughtMsg(thief, g)
          }
        }
      }

    case SendNeighboursMsg(_) =>

    case msg: SendGraphMsg =>
      this.thief.setGraph(msg.graph)

    case  ObjectOnSightLineMsg(bd) =>
      if(canAct && bd.exists(b => b.bodyType.contains(Exit))) {
        thief.setReachedExit(true)
        SystemManager.getLocalActor(GeneralActors.GAME_ACTOR) ! ThiefReachedExitMsg(thief)
      }

    case m => println("(thiefActor) message unknown:" + m)
  }



  def log() : String = "[CHARACTER " + thief.getId + "]: "
}

object ThiefActor {
  def props(thief: Thief): Props = Props(new ThiefActor(thief))
}
