package com.unibo.s3.main_system.communication

import akka.actor.Props

class AllActor(override val name: String) extends NamedActor {
  override def onReceive(message: Any): Unit = message match {
    case msg: String =>
      println("name: " + name + "| message: " + msg + " from: " + sender())
      sender().tell("response", getSelf())
    case _ => println("message unknown")
  }
}

object AllActor {
  def props(name: String): Props = Props(new AllActor(name))
}
