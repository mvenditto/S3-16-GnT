package daniele.comunication

import akka.actor.UntypedAbstractActor
import daniele.utils.IntMsg


class RemoteActor extends UntypedAbstractActor  {
  override def onReceive(message: Any): Unit = message match {
    case x: IntMsg =>
      println("remote: " + x.getVal  + " from: " + sender())
      x.inc()
      sender().tell(new IntMsg(x.getVal), getSelf())
    case _ => println("unknown message")
  }
}


