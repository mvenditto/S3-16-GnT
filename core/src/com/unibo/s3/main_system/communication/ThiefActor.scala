package com.unibo.s3.main_system.communication

import akka.actor.{Props, Stash, UntypedAbstractActor}
import com.unibo.s3.main_system.characters.Thief.Thief
import com.unibo.s3.main_system.communication.Messages.{ActMsg, SendGraphMsg, SendNeighboursMsg}
import com.unibo.s3.main_system.world.Exit
import com.unibo.s3.main_system.world.actors.{AskObjectOnSightLineMsg, ObjectOnSightLineMsg}

class ThiefActor(private[this] val thief: Thief) extends UntypedAbstractActor with Stash {

  /*
  override def onReceive(message: Any): Unit = message match {
    case msg: ActMsg =>
      this.thief.act(msg.dt)
      if (!thief.hasTarget) this.thief.chooseBehaviour()
      val wa = SystemManager.getLocalActor(GeneralActors.WORLD_ACTOR)
      wa ! AskObjectOnSightLineMsg(
        thief.getPosition, thief.getLinearVelocity, thief.getSightLineLength)

    case SendGuardsInProximityMsg(guards) => {
      thief.chooseTarget(guards)
    }

    case SendNeighboursMsg(_) =>

    case msg: SendGraphMsg =>
      this.thief.setGraph(msg.graph)

    case  ObjectOnSightLineMsg(bd) =>
      //bd.foreach(b => if (b.bodyType.contains(Exit)) println(log() + "Thief won!"))

    case _ => println("(thiefActor) message unknown:" + message)
  }*/

  context.become(setGraph())

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

  private def normalBehave(): Receive = {
    case msg: ActMsg =>
      this.thief.act(msg.dt)
      if (!thief.hasTarget) this.thief.chooseBehaviour()
      val wa = SystemManager.getLocalActor(GeneralActors.WORLD_ACTOR)
      wa ! AskObjectOnSightLineMsg(
        thief.getPosition, thief.getLinearVelocity, thief.getSightLineLength)

    case SendGuardsInProximityMsg(guards) =>
      thief.chooseTarget(guards)


    case SendNeighboursMsg(_) =>

    case msg: SendGraphMsg =>
      this.thief.setGraph(msg.graph)

    case  ObjectOnSightLineMsg(bd) =>
    //bd.foreach(b => if (b.bodyType.contains(Exit)) println(log() + "Thief won!"))
  }

  def log() : String = "[CHARACTER " + thief.getId + "]: "
}

object ThiefActor {
  def props(thief: Thief): Props = Props(new ThiefActor(thief))
}
