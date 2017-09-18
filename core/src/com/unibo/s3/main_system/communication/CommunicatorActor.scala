package com.unibo.s3.main_system.communication

import akka.actor.{Props, UntypedAbstractActor}
import com.unibo.s3.main_system.communication.Messages.SendIPMsg

class CommunicatorActor extends UntypedAbstractActor{
  override def onReceive(message: Any): Unit = message match {
    case msg:SendIPMsg => SystemManager.setIPForRemoting(msg.IP)
  }
}

object CommunicatorActor{
  def props(): Props = Props(new CommunicatorActor())
}
