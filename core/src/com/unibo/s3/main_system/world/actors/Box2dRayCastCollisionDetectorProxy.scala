package com.unibo.s3.main_system.world.actors

import akka.util.Timeout
import com.badlogic.gdx.ai.utils.{Collision, Ray, RaycastCollisionDetector}
import com.badlogic.gdx.math.Vector2
import com.unibo.s3.main_system.world.actors.ActorRefOrSelection.{ActorRefOrSelectionHolder, _}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

/**
  * A raycast collision detector, that works interacting with [[WorldActor]],
  * instead of directly referencing a [[com.badlogic.gdx.physics.box2d.World]].
  * This anyway introduces latency and a collision could be missed on high load.
  * @see [[com.unibo.s3.main_system.characters.steer.collisions.Box2dRaycastCollisionDetector]]
  * @param worldActor a [[ActorRefOrSelection]] to the WorldActor
  */
class Box2dRayCastCollisionDetectorProxy(worldActor: ActorRefOrSelectionHolder) extends RaycastCollisionDetector[Vector2]{

  implicit val timeout = Timeout(5 seconds)

  private def waitWorldResponse(future: Future[Any]): Any = {
    Await.result(future, timeout.duration)
  }

  override def findCollision(outputCollision: Collision[Vector2], inputRay: Ray[Vector2]): Boolean = {
    val future = worldActor ? RayCastCollisionQuery(inputRay.start, inputRay.end)
    val result = waitWorldResponse(future).asInstanceOf[RayCastCollisionResponse]
    outputCollision.set(result.coll.point, result.coll.normal)
    result.collided
  }

  override def collides(ray: Ray[Vector2]): Boolean = {
    val future = worldActor ? RayCastCollidesQuery(ray.start, ray.end)
    waitWorldResponse(future).asInstanceOf[RayCastCollidesResponse].collides
  }
}