package daniele.comunication

import akka.actor.UntypedAbstractActor
import daniele.utils.IntMsg


class LocalActor extends UntypedAbstractActor {

  override def onReceive(message: Any): Unit = message match {
    case x: IntMsg =>
      println("local: " + x.getVal + " from: " + sender())
      x inc()
      sender().tell(new IntMsg(x.getVal), getSelf())
    case _ => println("message unknown")
  }
}
