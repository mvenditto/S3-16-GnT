package com.unibo.s3.main_system.world.actors

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.badlogic.gdx.ai.utils.{Collision, Ray, RaycastCollisionDetector}
import com.badlogic.gdx.math.Vector2

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.language.postfixOps

class Box2dRayCastCollisionDetectorProxy(worldActor: ActorRef) extends RaycastCollisionDetector[Vector2]{

  implicit val timeout = Timeout(5 seconds)

  private def waitWorldResponse(future: Future[Any]): Any = {
    Await.result(future, timeout.duration)
  }

  override def findCollision(outputCollision: Collision[Vector2], inputRay: Ray[Vector2]): Boolean = {
    val future = worldActor ? RayCastCollisionQuery(inputRay)
    val result = waitWorldResponse(future).asInstanceOf[RayCastCollisionResponse]
    outputCollision.set(result.coll)
    result.collided
  }

  override def collides(ray: Ray[Vector2]): Boolean = {
    val future = worldActor ? RayCastCollidesQuery(ray)
    waitWorldResponse(future).asInstanceOf[Boolean]
  }
}
