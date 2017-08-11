package daniele.comunication

import akka.actor.Props

class StringActor(override val name: String) extends NamedActor {

  override def onReceive(message: Any): Unit = message match {
    case msg: String =>
      println("name: " + name + "| message: " + msg + " from: " + sender())
      sender().tell("response", getSelf())
    case _ => println("message unknown")
  }
}

object StringActor {
  def props(name: String): Props = Props(new StringActor(name))
}
