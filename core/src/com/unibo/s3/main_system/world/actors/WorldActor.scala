package com.unibo.s3.main_system.world.actors

import akka.actor.{ActorRef, Props, UntypedAbstractActor}
import com.badlogic.gdx.ai.steer.Proximity.ProximityCallback
import com.badlogic.gdx.ai.steer.Steerable
import com.badlogic.gdx.ai.utils.{Collision, Ray}
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d._
import com.unibo.s3.main_system.characters.steer.collisions.{Box2dRaycastCollisionDetector, Box2dSquareAABBProximity}
import com.unibo.s3.main_system.communication.Messages.{ActMsg, MapElementMsg}
import com.unibo.s3.main_system.util.GntUtils
import com.unibo.s3.main_system.world.{BodyData, Exit, Hideout}
import com.unibo.s3.main_system.util.GdxImplicits._
import com.unibo.s3.main_system.util.Box2dImplicits._
import net.dermetfan.gdx.physics.box2d.WorldObserver

import scala.util.Try

case class RayCastCollidesQuery(ray: Ray[Vector2])
case class RayCastCollidesResponse(collides: Boolean)
case class RayCastCollisionQuery(ray: Ray[Vector2])
case class RayCastCollisionResponse(collided: Boolean, coll: Collision[Vector2])
case class ProximityQuery(subject: Steerable[Vector2], detectionRadius: Float)
case class ProximityQueryResponse(neighbors: Seq[Steerable[Vector2]])
case class DeleteBodyAt(x: Float, y: Float)
case class CreateBox(position: Vector2, size: Vector2, bodyData: Option[BodyData]=None)
case class ResetWorld()
case class GetAllBodies()
case class RegisterAsWorldChangeObserver()
case class WorldChangeMsg(b: Body)
case class AskWorldMask(w:Int, h:Int, cellWidth: Float)
case class AskObjectOnSightLineMsg(p: Vector2, lv: Vector2, rayLenght: Float)
case class ObjectOnSightLineMsg(bd: Iterable[BodyData])


class WorldActor(val world: World) extends UntypedAbstractActor {
  import WorldActor._

  private[this] val rayCastCollisionDetector = new Box2dRaycastCollisionDetector(world)
  private[this] val proximityDetector = new Box2dSquareAABBProximity(null, world, 2.0f)
  private[this] var bodiesToDelete: Seq[Body] = List()
  private[this] var worldChangeObservers = List[ActorRef]()
  private[this] val worldObserver = new WorldObserver()

  worldObserver.setListener(new WorldObserver.Listener.Adapter {
    override def created(body: Body): Unit = {
      worldChangeObservers.foreach(o => o ! WorldChangeMsg(body))
    }
  })

  private def act(dt: Float) = {
    if (bodiesToDelete.nonEmpty) {
      bodiesToDelete.foreach(b => world.destroyBody(b))
      bodiesToDelete = List()
    }
    world.step(dt, VelocityIterations, PositionIterations)
    worldObserver.update(world, dt)
  }

  private def getBodies: com.badlogic.gdx.utils.Array[Body] = {
    val bodies = new com.badlogic.gdx.utils.Array[Body]()
    world.getBodies(bodies)
    bodies
  }

  private def parseBodyData(s: String): Option[BodyData] = {
    val b = BodyData()
    s match {
      case "E" => b.bodyType = Option(Exit); Option(b)
      case "H" => b.bodyType = Option(Hideout); Option(b)
      case _ => None
    }
  }

  override def onReceive(message: Any): Unit = message match {

    case ActMsg(dt) => act(dt)

    case RayCastCollidesQuery(ray) =>
      sender() ! RayCastCollidesResponse(rayCastCollisionDetector.collides(ray))

    case RayCastCollisionQuery(ray) =>
      val outputCollision = new Collision[Vector2](new Vector2(0,0), new Vector2(0,0))
      val collided = rayCastCollisionDetector.findCollision(outputCollision, ray)
      sender ! RayCastCollisionResponse(collided, outputCollision)

    case DeleteBodyAt(x, y) =>
      world.QueryAABB(new QueryCallback {
        override def reportFixture(f: Fixture): Boolean = {bodiesToDelete :+= f.getBody; true}
      }, x - AABBWidth, y - AABBWidth, x + AABBWidth, y + AABBWidth)

    case AskObjectOnSightLineMsg(p, lv, rl) =>
      val oc = new Collision[Vector2](new Vector2(), new Vector2())
      val end = lv.cpy().nor().scl(rl).add(p)
      val collided = rayCastCollisionDetector.findCollision(oc, new Ray[Vector2](p, end))

      if (collided) {
        val cp = oc.point
        val data = world.bodiesAtPoint(cp)
          .map(c => Try(c.getUserData.asInstanceOf[BodyData]))
          .filter(c => c.isSuccess)
          .map(c => c.get)

        sender ! ObjectOnSightLineMsg(data)
      }

    case CreateBox(pos, size, bdata) =>
      val b = world.createBox(pos, size)
      bdata.foreach(bd => b.setUserData(bd))

    case GetAllBodies() => sender() ! getBodies

    case ProximityQuery(owner, radius) =>
      proximityDetector.setOwner(owner)
      var neighbors: Seq[Steerable[Vector2]] = List()
      proximityDetector.findNeighbors(new ProximityCallback[Vector2] {
        override def reportNeighbor(n: Steerable[Vector2]): Boolean = {neighbors :+= n; true}
      })
      sender() ! ProximityQueryResponse(neighbors)

    case MapElementMsg(line) =>

      val entry = GntUtils.parseMapEntry(line)
      val body = entry._1
      val bodyData = entry._2

      val newBody = world.createBox(new Vector2(body(0), body(1)), new Vector2(body(2), body(3)))
      bodyData.foreach(bodyData =>
          parseBodyData(bodyData).foreach(bd => newBody.setUserData(bd)))

    case ResetWorld =>
      getBodies.asScalaIterable.foreach( b => bodiesToDelete :+= b)

    case RegisterAsWorldChangeObserver =>
      worldChangeObservers :+= sender
      
  }
}

object WorldActor {

  private val AABBWidth = 0.1f
  private val VelocityIterations = 8
  private val PositionIterations = 3

  def props(world: World): Props = Props(new WorldActor(world))
}
