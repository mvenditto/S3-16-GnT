package com.unibo.s3.main_system.communication

import akka.actor.{Props, UntypedAbstractActor}
import com.unibo.s3.main_system.characters.Thief.Thief

class ThiefActor(private[this] val thief: Thief) extends UntypedAbstractActor {

  override def onReceive(message: Any): Unit = message match {


    case _ => println("(thiefActor) message unknown:" + message)
  }
}

object ThiefActor {
  def props(thief: Thief): Props = Props(new ThiefActor(thief))
}
