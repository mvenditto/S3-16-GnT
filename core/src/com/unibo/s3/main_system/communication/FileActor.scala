package com.unibo.s3.main_system.communication

import akka.actor.Props
import com.unibo.s3.main_system.communication.Messages.FileMsg

class FileActor(override val name: String) extends NamedActor {

  override def onReceive(message: Any): Unit = message match {
    case msg: FileMsg =>
      println("name: " + name + "| file line: " + msg.line + " from: " + sender())
    case _ => println("message unknown")
  }
}

object FileActor {
  def props(name: String): Props = Props(new FileActor(name))
}
