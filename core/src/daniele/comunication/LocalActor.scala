package daniele.comunication

import akka.actor.{Props, UntypedAbstractActor}
import daniele.utils.IntMsg


class LocalActor(name: String) extends UntypedAbstractActor {

  override def onReceive(message: Any): Unit = message match {
    case x: IntMsg =>
      println(name + ": " + x.getVal + " from: " + sender())
      x inc()
      sender().tell(new IntMsg(x.getVal), getSelf())
    case _ => println("message unknown")
  }
}

object LocalActor {
  def props(name: String): Props = Props(new LocalActor(name))
}
