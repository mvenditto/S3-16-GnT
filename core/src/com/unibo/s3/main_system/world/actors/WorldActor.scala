package com.unibo.s3.main_system.world.actors

import akka.actor.{ActorRef, Props, UntypedAbstractActor}
import com.badlogic.gdx.ai.steer.Proximity.ProximityCallback
import com.badlogic.gdx.ai.steer.Steerable
import com.badlogic.gdx.ai.utils.{Collision, Ray}
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d._
import com.unibo.s3.main_system.characters.steer.collisions.{Box2dDetectorsFactory, Box2dRaycastCollisionDetector, Box2dSquareAABBProximity}
import com.unibo.s3.main_system.communication.Messages.{ActMsg, MapElementMsg}
import com.unibo.s3.main_system.util.GdxImplicits._
import com.unibo.s3.main_system.world.{BodyData, Exit, Hideout}
import net.dermetfan.gdx.physics.box2d.WorldObserver

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
case class RegisterAsWorldChangeObserver()
case class WorldChangeMsg(b: Body)

class WorldActor(val world: World) extends UntypedAbstractActor {

  private[this] val raycastCollisionDetector = new Box2dRaycastCollisionDetector(world)
  private[this] val proximityDetector = new Box2dSquareAABBProximity(null, world, 2.0f)
  private[this] var bodiesToDelete: Seq[Body] = List()
  private[this] val aabbWidth = 0.1f

  private[this] val velocityIters = 8
  private[this] val positionIters = 3

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
    world.step(dt, velocityIters, positionIters)
    worldObserver.update(world, dt)
  }

  private def createBox(position: Vector2, size: Vector2): Body = {
    val groundBodyDef = new BodyDef
    groundBodyDef.position.set(position)
    val groundBody = world.createBody(groundBodyDef)
    val groundBox = new PolygonShape
    groundBox.setAsBox(Math.abs(size.x / 2), Math.abs(size.y / 2))
    groundBody.createFixture(groundBox, 0.0f)
    groundBox.dispose()
    groundBody
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
      sender() ! RayCastCollidesResponse(raycastCollisionDetector.collides(ray))

    case RayCastCollisionQuery(ray) =>
      val outputCollision = new Collision[Vector2](new Vector2(0,0), new Vector2(0,0))
      val collided = raycastCollisionDetector.findCollision(outputCollision, ray)
      sender ! RayCastCollisionResponse(collided, outputCollision)

    case DeleteBodyAt(x, y) =>
      world.QueryAABB(new QueryCallback {
        override def reportFixture(f: Fixture): Boolean = {bodiesToDelete :+= f.getBody; true}
      }, x - aabbWidth, y - aabbWidth, x + aabbWidth, y + aabbWidth)

    case CreateBox(pos, size) => createBox(pos, size)

    case GetAllBodies() => sender() ! getBodies

    case ProximityQuery(owner, radius) =>
      proximityDetector.setOwner(owner)
      var neighbors: Seq[Steerable[Vector2]] = List()
      proximityDetector.findNeighbors(new ProximityCallback[Vector2] {
        override def reportNeighbor(n: Steerable[Vector2]): Boolean = {neighbors :+= n; true}
      })
      sender() ! ProximityQueryResponse(neighbors)

    case msg: MapElementMsg =>
      val toks = msg.line.split(":").toList

      var body = List[Float]()
      var extraData: Option[BodyData] = None

      for (i <- toks.indices) {
        if (i < 4) {
          body :+= toks(i).toFloat
        } else {
          extraData = parseBodyData(toks(i))
        }
      }

      val newBody = createBox(new Vector2(body.head, body(1)), new Vector2(body(2), body.last))
      if (extraData.isDefined) newBody.setUserData(extraData.get)

    case ResetWorld => getBodies.asScalaIterable.foreach( b => bodiesToDelete :+= b)

    case RegisterAsWorldChangeObserver => worldChangeObservers :+= sender
  }
}

object WorldActor {
  def props(world: World): Props = Props(new WorldActor(world))
}
