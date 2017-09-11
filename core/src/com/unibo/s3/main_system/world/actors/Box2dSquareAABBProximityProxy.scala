package com.unibo.s3.main_system.world.actors

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import com.badlogic.gdx.ai.steer.{Proximity, Steerable}
import com.badlogic.gdx.math.Vector2

import scala.concurrent.Await

class Box2dSquareAABBProximityProxy (
  var owner: Steerable[Vector2],
  worldActor: ActorRef,
  detectionRadius: Float) extends Proximity[Vector2] {

  implicit val timeout = Timeout(5 seconds)

  override def getOwner: Steerable[Vector2] = owner

  override def setOwner(owner: Steerable[Vector2]): Unit = this.owner = owner

  override def findNeighbors(callback: Proximity.ProximityCallback[Vector2]): Int = {
    val future = worldActor ? ProximityQuery(owner, detectionRadius)
    val result = Await.result(future, timeout.duration).asInstanceOf[ProximityQueryResponse]
    result.neighbors.foreach(n => callback.reportNeighbor(n))
    result.neighbors.length
  }
}
