package com.unibo.s3.main_system.world.actors

import akka.actor.{ActorRef, Props, UntypedAbstractActor}
import com.badlogic.gdx.ai.steer.Proximity.ProximityCallback
import com.badlogic.gdx.ai.steer.Steerable
import com.badlogic.gdx.ai.utils.{Collision, Ray}
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d._
import com.unibo.s3.main_system.characters.BaseCharacter
import com.unibo.s3.main_system.characters.steer.collisions.Box2dRaycastCollisionDetector
import com.unibo.s3.main_system.characters.steer.collisions.gdx.{Box2dSquareAABBProximity, Box2dSteeringEntity}
import com.unibo.s3.main_system.communication.Messages.{ActMsg, MapElementMsg}
import com.unibo.s3.main_system.util.Box2dImplicits._
import com.unibo.s3.main_system.util.GntUtils
import com.unibo.s3.main_system.world.{BodyData, Exit, Hideout}
import net.dermetfan.gdx.physics.box2d.WorldObserver

case class RayCastCollidesQuery(rayStart: Vector2, rayEnd: Vector2)
case class RayCastCollidesResponse(collides: Boolean)
case class RayCastCollisionQuery(rayStart: Vector2, rayEnd: Vector2)
case class RayCastCollisionResponse(collided: Boolean, coll: CollisionHolder)
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
case class FilterReachableByRay(op: Vector2, n: Iterable[Vector2], reqId: (Long, Int))
case class SendFilterReachableByRay(f: Iterable[Boolean], reqId: (Long, Int))

/**
  * An actor managing a [[com.badlogic.gdx.physics.box2d.World]].
  * @param world a [[World]] to be managed by this actor.
  *
  * @author mvenditto
  */
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
    //world.step(dt, VelocityIterations, PositionIterations)
    //worldObserver.update(world, dt)
  }

  private def parseBodyData(s: String, body: Body): Option[BodyData] = {
    val b = BodyData()
    s match {
      case "E" =>
        b.bodyType = Option(Exit)
        b.userData = Option(body.getWorldCenter.cpy)
        Option(b)
      case "H" => b.bodyType = Option(Hideout); Option(b)
      case _ => None
    }
  }

  override def onReceive(message: Any): Unit = message match {

    case ActMsg(dt) => act(dt)

    case ResetWorld() =>
      world.bodies.foreach( b => bodiesToDelete :+= b)

    case RegisterAsWorldChangeObserver =>
      worldChangeObservers :+= sender

    case RayCastCollidesQuery(start, end) =>
      sender() ! RayCastCollidesResponse(
        rayCastCollisionDetector.collides(new Ray(start, end)))

    case RayCastCollisionQuery(start, end) =>
      val outputCollision = new Collision[Vector2](new Vector2(0,0), new Vector2(0,0))
      val ray = new Ray(start, end)
      val collided = rayCastCollisionDetector.findCollision(outputCollision, ray)
      sender ! RayCastCollisionResponse(collided, CollisionHolder.of(outputCollision))

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
          .map(b => b.getUserData)
          .filter(b => b != null)
          .map { case b:BodyData => b}
        sender ! ObjectOnSightLineMsg(data)
      }

    case CreateBox(pos, size, bodyData) =>
      val b = world.createBox(pos, size)
      bodyData.foreach(bd => b.setUserData(bd))
      worldObserver.getListener.created(b)

    case FilterReachableByRay(op, n, reqId) =>
      val ray = new Ray[Vector2](n.head, n.head)
      sender ! SendFilterReachableByRay(
        n.map(p => {
          ray.start = op; ray.end = p
          !rayCastCollisionDetector.collides(ray)
        }), reqId
      )

    case GetAllBodies() => sender() ! world.bodies

    case ProximityQuery(owner, radius) =>
      proximityDetector.setOwner(owner)
      proximityDetector.setDetectionRadius(radius)
      var neighbors: Seq[Steerable[Vector2]] = List()
      proximityDetector.findNeighbors(new ProximityCallback[Vector2] {
        override def reportNeighbor(n: Steerable[Vector2]): Boolean = {
          n match {
            case b: Box2dSteeringEntity => b.getBody.getUserData match {
              case bd: BodyData if bd.bodyType.exists(bt => bt.equals(Hideout)) =>
                neighbors :+= n
                true
              case _ => false
            }
            case _ => false
          }
        }
      })
      sender() ! ProximityQueryResponse(neighbors)

    case MapElementMsg(line) =>

      val entry = GntUtils.parseMapEntry(line)
      val body = entry._1
      val bodyData = entry._2

      val newBody = world.createBox(new Vector2(body(0) - 0.3f, body(1) - 0.3f), new Vector2(body(2), body(3)))
      bodyData.foreach(bodyData =>
        parseBodyData(bodyData, newBody).foreach(bd => newBody.setUserData(bd)))
      worldObserver.getListener.created(newBody)
  }
}

object WorldActor {

  private val AABBWidth = 0.1f
  private val VelocityIterations = 8
  private val PositionIterations = 3

  def props(world: World): Props = Props(new WorldActor(world))
}
