package daniele.comunication

import akka.actor.Props
import daniele.comunication.Messages.StringMsg

class StringActor(override val name: String) extends NamedActor {

  override def onReceive(message: Any): Unit = message match {
    case msg: StringMsg =>
      println("name: " + name + "| message: " + msg.v + " from: " + sender())
      sender().tell(StringMsg("response"), getSelf())
    case _ => println("message unknown")
  }
}

object StringActor {
  def props(name: String): Props = Props(new StringActor(name))
}
