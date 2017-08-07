package daniele.comunication

import akka.actor.UntypedAbstractActor

trait NamedActor extends UntypedAbstractActor {
  val name: String
}
