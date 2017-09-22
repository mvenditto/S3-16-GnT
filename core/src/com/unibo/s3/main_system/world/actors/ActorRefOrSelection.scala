package com.unibo.s3.main_system.world.actors

import akka.actor.{ActorRef, ActorSelection}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Future

/**
  * This is a container for either an [[ActorRef]] or an [[ActorSelection]].
  * Used when a reference to an Akka actor is needed to send a message to it,
  * regardless the actor is remote or not.
  *
  * @author mvenditto
  */
object ActorRefOrSelection {

  type ActorRefOrSelectionHolder = Either[ActorRef, ActorSelection]

  implicit def packActorRef(ar: ActorRef): ActorRefOrSelectionHolder = Left(ar)
  implicit def packActorSelection(as: ActorSelection): ActorRefOrSelectionHolder = Right(as)

  implicit class AugmentedActorRefOrSelection(ars: ActorRefOrSelectionHolder) {
    implicit def ?(msg: Any)(implicit timeout: Timeout): Future[Any] =
      ars.fold(ar => ask(ar, msg), as => ask(as, msg))
  }
}
