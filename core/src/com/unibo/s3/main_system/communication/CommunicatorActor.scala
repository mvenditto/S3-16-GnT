package com.unibo.s3.main_system.communication

import java.net.InetAddress

import akka.actor.{Props, UntypedAbstractActor}
import com.unibo.s3.main_system.communication.Messages.{AskIPMsg, SendIPMsg}
import com.unibo.s3.main_system.game.AkkaSettings

class CommunicatorActor extends UntypedAbstractActor{

  override def onReceive(message: Any): Unit = message match {
    case _:AskIPMsg => val ref = SystemManager.getRemoteActor(AkkaSettings.ComputeSystem, "/user/",
      GeneralActors.COMMUNICATOR_ACTOR.name) ! SendIPMsg(InetAddress.getLocalHost.getHostAddress)
    case msg:SendIPMsg => SystemManager.setIPForRemoting(msg.IP, AkkaSettings.GUISystemPort)
    case _ => println("unknown msg: " + message)
  }
}

object CommunicatorActor{
  def props(): Props = Props(new CommunicatorActor())
}
