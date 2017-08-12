package com.unibo.s3.main_system.communication

import akka.actor.UntypedAbstractActor

trait NamedActor extends UntypedAbstractActor {
  val name: String
}
