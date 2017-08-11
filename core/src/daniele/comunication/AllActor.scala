package daniele.comunication

import akka.actor.Props
import daniele.comunication.Messages.{IntMsg, StringMsg}

class AllActor(override val name: String) extends NamedActor {
  override def onReceive(message: Any): Unit = message match {
    case msg: StringMsg =>
      println("name: " + name + "| message: " + msg.v + " from: " + sender())
      sender().tell(StringMsg("response"), getSelf())
    case _ => println("message unknown")
  }
}

object AllActor {
  def props(name: String): Props = Props(new AllActor(name))
}
