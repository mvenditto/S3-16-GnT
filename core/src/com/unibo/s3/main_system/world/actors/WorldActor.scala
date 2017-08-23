package com.unibo.s3.main_system.world.actors

import akka.actor.{Props, UntypedAbstractActor}
import com.badlogic.gdx.ai.steer.Proximity.ProximityCallback
import com.badlogic.gdx.ai.steer.Steerable
import com.badlogic.gdx.ai.utils.{Collision, Ray}
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d._
import com.unibo.s3.main_system.characters.steer.collisions.{Box2dDetectorsFactory, Box2dRaycastCollisionDetector, Box2dSquareAABBProximity}

case class Act(dt: Float)
case class RayCastCollidesQuery(ray: Ray[Vector2])
case class RayCastCollidesResponse(collides: Boolean)
case class RayCastCollisionQuery(ray: Ray[Vector2])
case class RayCastCollisionResponse(collided: Boolean, coll: Collision[Vector2])
case class ProximityQuery(subject: Steerable[Vector2], detectionRadius: Float)
case class ProximityQueryResponse(neghbors: Seq[Steerable[Vector2]])
case class DeleteBodyAt(x: Float, y: Float)
case class CreateBox(position: Vector2, size: Vector2)
case class ResetWorld()
case class GetAllBodies()

class WorldActor(val world: World) extends UntypedAbstractActor {

  private[this] val raycastCollisionDetector = new Box2dRaycastCollisionDetector(world)
  private[this] val proximityDetector = new Box2dSquareAABBProximity(null, world, 2.0f)
  private[this] var bodiesToDelete: Seq[Body] = List()
  private[this] val aabbWidth = 0.1f

  private[this] val velocityIters = 8
  private[this] val positionIters = 3

  private def act(dt: Float) = {
    if (bodiesToDelete.nonEmpty) {
      bodiesToDelete.foreach(b => world.destroyBody(b))
      bodiesToDelete = List()
    }
    world.step(dt, velocityIters, positionIters)
  }

  private def createBox(position: Vector2, size: Vector2) = {
    val groundBodyDef = new BodyDef
    groundBodyDef.position.set(position)
    val groundBody = world.createBody(groundBodyDef)
    val groundBox = new PolygonShape
    groundBox.setAsBox(Math.abs(size.x / 2), Math.abs(size.y / 2))
    groundBody.createFixture(groundBox, 0.0f)
    groundBody.setUserData(size.x + ":" + size.y)
    groundBox.dispose()
  }

  override def onReceive(message: Any): Unit = message match {

    case Act(dt) => act(dt)

    case RayCastCollidesQuery(ray) =>
      sender() ! RayCastCollidesResponse(raycastCollisionDetector.collides(ray))

    case RayCastCollisionQuery(ray) =>
      val outputCollision = new Collision[Vector2](new Vector2(0,0), new Vector2(0,0))
      val collided = raycastCollisionDetector.findCollision(outputCollision, ray)
      sender ! RayCastCollisionResponse(collided, outputCollision)

    case DeleteBodyAt(x, y) =>
      world.QueryAABB(new QueryCallback {
        override def reportFixture(f: Fixture): Boolean = {bodiesToDelete :+= f.getBody; true}
      }, x - aabbWidth, y - aabbWidth, x + aabbWidth, y + aabbWidth)

    case CreateBox(pos, size) => createBox(pos, size);

    case GetAllBodies() =>
      val bodies = new com.badlogic.gdx.utils.Array[Body]()
      world.getBodies(bodies)
      sender() ! bodies

    case ProximityQuery(owner, radius) =>
      proximityDetector.setOwner(owner)
      var neighbors: Seq[Steerable[Vector2]] = List()
      proximityDetector.findNeighbors(new ProximityCallback[Vector2] {
        override def reportNeighbor(n: Steerable[Vector2]): Boolean = {neighbors :+= n; true}
      })
      sender() ! ProximityQueryResponse(neighbors)
  }
}

object WorldActor {
  def props(world: World): Props = Props(new WorldActor(world))
}
