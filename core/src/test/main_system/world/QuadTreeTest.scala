package world

import akka.actor.{ActorRef, Props, UntypedAbstractActor}
import akka.pattern.ask
import com.badlogic.gdx.math.{MathUtils, Vector2}
import com.badlogic.gdx.physics.box2d.World
import com.unibo.s3.main_system.characters.BaseCharacter
import com.unibo.s3.main_system.characters.Guard.Guard
import com.unibo.s3.main_system.communication.GeneralActors._
import com.unibo.s3.main_system.communication.Messages._
import com.unibo.s3.main_system.communication.{AskNeighboursWithinFovMsg, QuadTreeActor, SystemManager}
import com.unibo.s3.main_system.world.actors.WorldActor
import com.unibo.s3.main_system.world.spatial.Bounds
import main_system.GdxDependencies
import org.junit.runner.RunWith
import org.junit.{Before, Test}

@RunWith(classOf[GdxDependencies])
class QuadTreeTest extends BaseTestInvolvingActors("QuadTreeTestSystem") {

  private[this] var quadTreeActor: ActorRef = _
  private[this] var dummyCharacter: BaseCharacter = _
  private[this] var dummyActor: ActorRef = _
  private[this] val bounds = Bounds(0, 0, 60, 60)

  initActorSystem()

  private case class LatestResult()
  private class DummyActor() extends UntypedAbstractActor {
    override def onReceive(m: Any): Unit = m match {case _ => ()}
  }

  private def randomPointInBounds(b: Bounds): Vector2 =
    new Vector2(MathUtils.random(b.x, b.w), MathUtils.random(b.y, b.h))


  override def deployActors(): Unit = {
    quadTreeActor = system.actorOf(
      QuadTreeActor.props())
    quadTreeActor ! MapSettingsMsg(bounds.w.toInt, bounds.h.toInt)

    List(GAME_ACTOR, LIGHTING_SYSTEM_ACTOR)
      .foreach(a =>SystemManager.createGeneralActor(Props(new DummyActor), a))

    SystemManager.createGeneralActor(
      WorldActor.props(new World(new Vector2(0,0), true)),WORLD_ACTOR)

    dummyActor = SystemManager.createActor(Props(new DummyActor), "dummyActor")
  }

  @Before def setUp(): Unit = {
    dummyCharacter = Guard(new Vector2(0, 0), 0)
  }

  @Test def testInsertEntity() = {
    quadTreeActor ! InitialSavingCharacterMsg(dummyCharacter, dummyActor)
    quadTreeActor ! RebuildQuadTreeMsg()
    quadTreeActor.tell(AskAllCharactersMsg, testActor)

    expectMsgPF() {
      case SendAllCharactersMsg(characters) =>
        assert(characters.exists(p => p.equals(dummyCharacter)))
      case _ => BadMatchFail()
    }
  }

  @Test def testNeighborsSearch() = {
    val randCharacterInBounds =
      for {
        id <- 0 to 15
        c = Guard(randomPointInBounds(bounds), id)
      } yield c

    randCharacterInBounds.foreach(c =>
      quadTreeActor ! InitialSavingCharacterMsg(c, dummyActor))

    quadTreeActor ! RebuildQuadTreeMsg()

    quadTreeActor tell(AskNeighboursMsg(randCharacterInBounds.head, Option(bounds.w)), testActor)
    expectMsgPF() {
      case SendNeighboursMsg(n) => assert(n.size == randCharacterInBounds.size)
      case _ => BadMatchFail()
    }
  }

  @Test def testNeighborsSearchWithinDefaultFov() = {
    val cx = bounds.w / 2f
    val cy = bounds.h / 2f
    val c0 = Guard(new Vector2(cx, cy), 0)
    c0.getLinearVelocity.set(0, 10) //make look upward
    val cr = c0.getFieldOfView.getRadius

    val behind = for(i <- 0 to 19) yield Guard(new Vector2(cx  + (i - 10), cy - (cr / 2f)), i + 10)
    val ahead = for(i <- 0 to 19) yield Guard(new Vector2(cx + (i - 10), cy + (cr / 2f)), (i + 10) + 10)

    (ahead ++ behind).foreach(c => quadTreeActor ! InitialSavingCharacterMsg(c, dummyActor))

    quadTreeActor ! InitialSavingCharacterMsg(c0, testActor)
    quadTreeActor ! RebuildQuadTreeMsg()
    quadTreeActor ! AskNeighboursWithinFovMsg(c0)

    expectMsgPF(){
      case SendNeighboursMsg(n) => println(n.size)
          assert(n.nonEmpty)
      case _ => BadMatchFail()
    }
  }
}
