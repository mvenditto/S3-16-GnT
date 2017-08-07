package daniele.comunication

import akka.actor.Props
import daniele.comunication.Messages.IntMsg



class IntActor(override val name: String) extends NamedActor {

  override def onReceive(message: Any): Unit = message match {
    case msg: IntMsg =>
      println("name: " + name + "| value: " + msg.v + " from: " + sender())
      sender().tell(IntMsg(msg.v+1), getSelf())
    case _ => println("message unknown")
  }
}

object IntActor {
  def props(name: String): Props = Props(new IntActor(name))
}
