package com.unibo.s3.testbed.modules

import akka.actor.{ActorRef, Props, UntypedAbstractActor}
import com.badlogic.gdx.ai.utils.RaycastCollisionDetector
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.math.Vector2
import com.unibo.s3.main_system.characters.steer.BaseMovableEntity
import com.unibo.s3.main_system.characters.steer.collisions.Box2dProxyDetectorsFactory
import com.unibo.s3.main_system.communication.{GeneralActors, SystemManager}
import com.unibo.s3.main_system.rendering.GeometryRendererImpl
import com.unibo.s3.main_system.util.ScaleUtils
import com.unibo.s3.main_system.world.BodyData
import com.unibo.s3.main_system.world.actors.{AskObjectOnSightLineMsg, CreateBox, ObjectOnSightLineMsg}
import com.unibo.s3.testbed.model.{BaseTestbedModule, Testbed}

class TestObjectOnSightLine extends BaseTestbedModule {

  var worldActor: ActorRef = _
  var receiverActor: ActorRef = _
  var cd: RaycastCollisionDetector[Vector2] = _
  var character = new BaseMovableEntity(new Vector2(0,0))
  var gr = GeometryRendererImpl()

  val b2d = new Box2dModule()
  var lookingAt: Option[Iterable[BodyData]] = None
  submodules :+= b2d

  private class ReceiverActor extends UntypedAbstractActor {
    override def onReceive(m: Any): Unit = m match {
      case ObjectOnSightLineMsg(d) =>
        lookingAt = Option(d)

      case _ => println("unknown")
    }
  }

  override def update(dt: Float): Unit = {
    super.update(dt)
    character.act(dt)
    worldActor.tell(AskObjectOnSightLineMsg(
      character.getPosition, character.getLinearVelocity, 5f),
      receiverActor)
  }

  private object ReceiverActor {
    def props(): Props = Props(new ReceiverActor())
  }

  override def init(owner: Testbed): Unit = {
    super.init(owner)
  }

  override def render(shapeRenderer: ShapeRenderer): Unit = {
    super.render(shapeRenderer)
    gr.renderCharacter(shapeRenderer, character)

    val col = shapeRenderer.getColor
    val t = shapeRenderer.getCurrentType

    val s = ScaleUtils.getPixelsPerMeter
    shapeRenderer.set(ShapeType.Filled)

    shapeRenderer.setColor(Color.RED)
    shapeRenderer.circle(-10 * s, 0, 1.0f * s)

    shapeRenderer.setColor(Color.BLUE)
    shapeRenderer.circle(10 * s, 0, 1.0f * s)

    shapeRenderer.setColor(Color.GREEN)
    shapeRenderer.circle(0, -10 * s, 1.0f * s)

    shapeRenderer.setColor(Color.YELLOW)
    shapeRenderer.circle(0, 10 * s, 1.0f * s)

    val end = character.getLinearVelocity.cpy()
      .nor()
      .scl(5f)
      .add(character.getPosition)
      .scl(s.toFloat)

    shapeRenderer.setColor(Color.WHITE)
    shapeRenderer.rectLine(character.getPosition.cpy().scl(s.toFloat), end, 4f)


    var x = 0f
    lookingAt.foreach(b => b.foreach(bd => {
      val c = bd.userData.asInstanceOf[Option[Color]]
      shapeRenderer.setColor(c.get)
      shapeRenderer.circle((x * 1.5f) * s, 25 * s, 1.0f * s)
      x += 1.5f
    }))
    lookingAt = None
    shapeRenderer.setColor(col)
    shapeRenderer.set(t)
  }

  override def setup(f: (String) => Unit): Unit = {
    super.setup(f)
    worldActor = SystemManager.getLocalActor(GeneralActors.WORLD_ACTOR)
    receiverActor = SystemManager.createActor(ReceiverActor.props(), "receiverActor")
    cd = new Box2dProxyDetectorsFactory(worldActor).newRaycastCollisionDetector()
    character.setColor(Color.GREEN)
    character.setCollisionDetector(cd)
    character.setComplexSteeringBehavior()
      .avoidCollisionsWithWorld()
      .wander()
      .buildPriority(true)
    worldActor ! CreateBox(new Vector2(-10, 0), new Vector2(2, 20), Option(BodyData(userData = Some(Color.RED))))
    worldActor ! CreateBox(new Vector2(10, 0), new Vector2(2, 20),Option(BodyData(userData = Some(Color.BLUE))))
    worldActor ! CreateBox(new Vector2(0, -10), new Vector2(20, 2),Option(BodyData(userData = Some(Color.GREEN))))
    worldActor ! CreateBox(new Vector2(0, 10), new Vector2(20, 2),Option(BodyData(userData = Some(Color.YELLOW))))
  }
}
