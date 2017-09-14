package com.unibo.s3.main_system.communication

import akka.actor.{Props, UntypedAbstractActor}
import com.unibo.s3.main_system.characters.Thief.Thief
import com.unibo.s3.main_system.communication.Messages.{ActMsg, SendGraphMsg}
import com.unibo.s3.main_system.world.Exit
import com.unibo.s3.main_system.world.actors.{AskObjectOnSightLineMsg, ObjectOnSightLineMsg}

class ThiefActor(private[this] val thief: Thief) extends UntypedAbstractActor {


  override def onReceive(message: Any): Unit = message match {
    case msg: ActMsg =>
      this.thief.act(msg.dt)
      this.thief.chooseBehaviour()
      val wa = SystemManager.getLocalGeneralActor(GeneralActors.WORLD_ACTOR)
      wa ! AskObjectOnSightLineMsg(
        thief.getPosition, thief.getLinearVelocity, thief.getSightLineLength)

    case msg: SendGraphMsg =>
      this.thief.setGraph(msg.graph)

    case  ObjectOnSightLineMsg(bd) =>
      bd.foreach(b => if (b.bodyType.contains(Exit)) println("Thief won!"))

    case _ => println("(thiefActor) message unknown:" + message)
  }
}

object ThiefActor {
  def props(thief: Thief): Props = Props(new ThiefActor(thief))
}
