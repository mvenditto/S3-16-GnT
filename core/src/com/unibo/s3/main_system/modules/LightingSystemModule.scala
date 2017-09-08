package com.unibo.s3.main_system.modules

import akka.actor.{ActorRef, Props, UntypedAbstractActor}
import box2dLight.{ConeLight, PointLight, RayHandler}
import com.badlogic.gdx.{Gdx, InputMultiplexer}
import com.badlogic.gdx.graphics.{Color, GL20, OrthographicCamera}
import com.badlogic.gdx.math.{MathUtils, Vector2}
import com.badlogic.gdx.physics.box2d.World
import com.unibo.s3.Main
import com.unibo.s3.main_system.communication.Messages.{AskAllCharactersMsg, SendAllCharactersMsg}
import com.unibo.s3.main_system.communication.{GeneralActors, SystemManager}
import com.unibo.s3.main_system.util.ScaleUtils
import com.unibo.s3.main_system.world.actors.{RegisterAsWorldChangeObserver, WorldChangeMsg}

import scala.collection.mutable

case class AskIsPointAtShadow(p: Vector2)

class LightingSystemModule extends BasicModuleWithGui {

  private[this] var updatedWorld: Option[World] = None
  private[this] var rayHandler: RayHandler = _
  private[this] var cam: OrthographicCamera = _
  private[this] var worldObserverActor: ActorRef = _

  private[this] var torches = mutable.Map[Int, ConeLight]()

  private[this] val ambientLightColor = new Color(.1f, .1f, .1f, .1f)
  private[this] val brightWhite = new Color(1.0f, 1.0f, 1.0f, 1.0f)

  private class LightingActor extends UntypedAbstractActor {
    override def onReceive(msg: Any): Unit = msg match {
      case WorldChangeMsg(w) =>
        updatedWorld = Option(w)

      case AskIsPointAtShadow(p) =>
        sender ! rayHandler.pointAtShadow(p.x, p.y)

      case SendAllCharactersMsg(character) =>
        character.foreach(c => {
          val angle = (c.getOrientation * MathUtils.radiansToDegrees) + 90
          val id = c.getId

          if (torches.contains(id)) {
            val t = torches(id)
            t.setPosition(c.getPosition)
            t.setDirection(angle)
          } else {
            torches(id) = new ConeLight(rayHandler, 65, brightWhite, 15f,
              c.getPosition.x, c.getPosition.y, angle, 25f)
          }
        })
    }
  }

  override def init(owner: Main): Unit = {
    super.init(owner)
    cam = owner.getCamera()
    rayHandler = new RayHandler(
      null, Gdx.graphics.getWidth, Gdx.graphics.getHeight)
  }

  def setup(): Unit = {

    worldObserverActor =
      SystemManager.createActor(Props(new LightingActor), "worldObserver")

    SystemManager
      .getLocalGeneralActor(GeneralActors.WORLD_ACTOR)
      .tell(RegisterAsWorldChangeObserver, worldObserverActor)

    RayHandler.setGammaCorrection(false)
    RayHandler.useDiffuseLight(true)
    rayHandler.setBlur(true)
    rayHandler.setBlurNum(2)
    rayHandler.setShadows(true)
    rayHandler.setCulling(true)
    rayHandler.setAmbientLight(ambientLightColor)
    rayHandler.diffuseBlendFunc.set(GL20.GL_DST_COLOR, GL20.GL_SRC_COLOR)
  }

  override def attachInputProcessors(inputMultiplexer: InputMultiplexer): Unit = {
    super.attachInputProcessors(inputMultiplexer)
    inputMultiplexer.addProcessor(this)
  }

  override def update(dt: Float): Unit = {
    super.update(dt)
    if (updatedWorld.isDefined) {
      rayHandler.setWorld(updatedWorld.get)
      updatedWorld = None
    }

    SystemManager
      .getLocalGeneralActor(GeneralActors.QUAD_TREE_ACTOR)
      .tell(AskAllCharactersMsg, worldObserverActor)

  }

  override def touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean = {
    if (button == 1) {
      val mouseWorldPos = owner.screenToWorld(new Vector2(screenX, screenY))
      mouseWorldPos.scl(ScaleUtils.getMetersPerPixel)
      new PointLight(rayHandler, 64, brightWhite, 25, mouseWorldPos.x, mouseWorldPos.y)
    }
    false
  }

  override def customRender(): Unit = {
    rayHandler.setCombinedMatrix(
      cam.combined.cpy().scl(ScaleUtils.getPixelsPerMeter.toFloat), 0, 0,
      cam.viewportWidth, cam.viewportHeight)
    rayHandler.updateAndRender()
    rayHandler.useDefaultViewport()
  }

  override def cleanup(): Unit = {
    super.cleanup()
    rayHandler.dispose()
  }
}
