package world

import akka.actor.{ActorRef, Props}
import com.badlogic.gdx.ai.utils.Ray
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.{MathUtils, Vector2}
import com.badlogic.gdx.physics.box2d.{Body, World}
import com.unibo.s3.main_system.characters.Guard
import com.unibo.s3.main_system.characters.steer.BaseMovableEntity
import com.unibo.s3.main_system.communication.Messages.{ActMsg, MapElementMsg}
import com.unibo.s3.main_system.util.Box2dImplicits._
import com.unibo.s3.main_system.world.actors._
import com.unibo.s3.main_system.world.{BodyData, BodyType, Exit}
import main_system.GdxDependencies
import org.junit.Assert.fail
import org.junit.runner.RunWith
import org.junit.{Before, Test}

/**
  * Test WorldActor interactions.
  * @author mvenditto
  */
@RunWith(classOf[GdxDependencies])
class WorldTest extends BaseTestInvolvingActors("WorldTestSystem") {

  private val world: World = new World(new Vector2(0, 0), true)
  private var worldActor: ActorRef = _

  initActorSystem()

  override def deployActors(): Unit = {
    worldActor = system.actorOf(Props(new WorldActor(world)))
  }

  @Before def resetWorld(): Unit = {
    world.bodies.foreach(b => world.destroyBody(b))
  }

  @Test def testAddBodyToWorld() = {
    worldActor ! CreateBox(new Vector2(0, 0), new Vector2(10, 10))
    worldActor ! GetAllBodies()
    expectMsgPF() {
      case b: Iterable[Body] => assert(b.size == 1)
      case _ => BadMatchFail()
    }
  }

  @Test def testResetWorld() = {
    worldActor ! CreateBox(new Vector2(0, 0), new Vector2(10, 10))
    worldActor ! ResetWorld()
    worldActor ! ActMsg(1f)
    worldActor ! GetAllBodies()
    expectMsgPF() {
      case b: Iterable[Body] => assert(b.isEmpty)
      case _ => BadMatchFail()
    }
  }

  @Test def testRayShouldCollide() = {
    val boxCenter = new Vector2(0, 0)
    worldActor ! CreateBox(boxCenter, new Vector2(10, 10))
    val ray = new Ray[Vector2](boxCenter.cpy().sub(0, 10), boxCenter)
    worldActor ! RayCastCollidesQuery(ray.start, ray.end)
    expectMsgPF() {
      case RayCastCollidesResponse(r) => assert(r)
      case _ => BadMatchFail()
    }
  }

  @Test def testRayShouldCollideAtPointAndNormal() = {
    val boxCenter = new Vector2(0, 0)
    worldActor ! CreateBox(boxCenter, new Vector2(10, 10))
    val ray = new Ray[Vector2](boxCenter.cpy().sub(0, 10), boxCenter)
    worldActor ! RayCastCollisionQuery(ray.start, ray.end)

    val expectedCollisionPoint = new Vector2(0.0f, -5.0f)
    val expectedCollisionNormal = new Vector2(0.0f, -1.0f)
    val epsilon = MathUtils.FLOAT_ROUNDING_ERROR

    expectMsgPF() {
      case RayCastCollisionResponse(hasCollided, cp) =>
        assert(hasCollided
          && cp.point.epsilonEquals(expectedCollisionPoint, epsilon)
          && cp.normal.epsilonEquals(expectedCollisionNormal, epsilon)
        )
      case _ => BadMatchFail()
    }
  }

  @Test def testRayShouldNotCollide() = {
    val boxCenter = new Vector2(0, 0)
    worldActor ! CreateBox(boxCenter, new Vector2(10, 10))
    val ray = new Ray[Vector2](boxCenter.cpy().sub(0, 10), boxCenter.cpy().add(11, 0))
    worldActor ! RayCastCollidesQuery(ray.start, ray.end)
    expectMsgPF() {
      case RayCastCollidesResponse(r) => assert(!r)
      case _ => BadMatchFail()
    }
  }

  @Test def testFilterOnlyVisiblePoints() = {
    val points = List(
      new Vector2(-15, 0), new Vector2(15, 0), new Vector2(0, 15))
    val expected = List(true, true, false)
    val observer = Guard(new Vector2(0, -15), 0)
    worldActor ! CreateBox(new Vector2(0, 0), new Vector2(10, 10))
    worldActor ! FilterReachableByRay(observer, points, (0, 0))

    expectMsgPF() {
      case SendFilterReachableByRay(mask, _) => assert(mask.equals(expected))
      case _ => BadMatchFail()
    }
  }

  @Test def testRetrieveBodyDataForObjectOnSightLine() = {
    val bd = BodyData(userData=Some(Color.RED))
    worldActor ! CreateBox(new Vector2(0, 0), new Vector2(10, 10), Option(bd))

    val pos = new Vector2(0, -10)
    val linearVelocity = new Vector2(0, 5) //heading upward
    val sightLineLength = 10f
    worldActor ! AskObjectOnSightLineMsg(pos, linearVelocity, sightLineLength)

    expectMsgPF() {
      case ObjectOnSightLineMsg(bds) => bds.foreach(b => b.userData match {
        case Some(c: Color) => assert(c == Color.RED)
        case _ => fail()
      })
      case _ => BadMatchFail()
    }
  }

  @Test def testMapMessageWithExitTag() = {
    worldActor ! MapElementMsg("0:0:10:10:E")

    val pos = new Vector2(0, -10)
    val linearVelocity = new Vector2(0, 5) //heading upward
    val sightLineLength = 10f
    worldActor ! AskObjectOnSightLineMsg(pos, linearVelocity, sightLineLength)

    expectMsgPF() {
      case ObjectOnSightLineMsg(bds) => bds.foreach(b => b.bodyType match {
        case Some(c: BodyType) => assert(c == Exit)
        case _ => fail()
      })
      case _ => BadMatchFail()
    }
  }

  @Test def testDeleteBodyAtPoint() = {
    val boxCenterA = new Vector2(0, 0)
    val boxCenterB = new Vector2(30, 30)
    val boxSize = new Vector2(10, 10)
    worldActor ! CreateBox(boxCenterA, boxSize)
    worldActor ! CreateBox(boxCenterB, boxSize)
    worldActor ! DeleteBodyAt(boxCenterA.x + 2, boxCenterA.y + 2)
    worldActor ! ActMsg(1f)
    worldActor ! GetAllBodies()

    expectMsgPF() {
      case b: Iterable[Body] => assert(b.size == 1)
      case _ => BadMatchFail()
    }
  }

  @Test def testShouldDetectedObjectsInProximity() = {
    val boxes = List(new Vector2(-10, 0), new Vector2(0, 0), new Vector2(8, 0))
    val boxSize = new Vector2(4, 4)
    val observer = new BaseMovableEntity(new Vector2(4f, 0))
    val radius = 2f
    boxes.foreach(boxCenter => worldActor ! CreateBox(boxCenter, boxSize))
    worldActor ! ProximityQuery(observer, radius)
    expectMsgPF() {
      case ProximityQueryResponse(b) =>
        val toPoints = b.map(n => n.getPosition)
        assert(toPoints.contains(boxes(1)) && toPoints.contains(boxes(2)))
      case _ => BadMatchFail()
    }
  }

}
