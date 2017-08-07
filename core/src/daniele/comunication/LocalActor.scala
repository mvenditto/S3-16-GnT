package daniele.comunication

import akka.actor.{Props, UntypedAbstractActor}
import daniele.comunication.Messages.{IntMsg, StringMsg}



class LocalActor(name: String) extends UntypedAbstractActor {

  override def onReceive(message: Any): Unit = message match {
    case msg: IntMsg =>
      println(name + ": " + msg.v + " from: " + sender())
      //x inc()
      sender().tell(IntMsg(msg.v+1), getSelf())
    case msg: StringMsg =>
      println("message: " + msg.v)
      sender().tell(StringMsg("received"), getSelf())
    case _ => println("message unknown")
  }
}

object LocalActor {
  def props(name: String): Props = Props(new LocalActor(name))
}
