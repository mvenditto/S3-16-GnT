package com.unibo.s3.main_system.modules

import akka.actor.{ActorRef, Props, UntypedAbstractActor}
import box2dLight.{ConeLight, PointLight, RayHandler}
import com.badlogic.gdx.{Gdx, InputMultiplexer}
import com.badlogic.gdx.graphics.{Color, GL20, OrthographicCamera}
import com.badlogic.gdx.math.{MathUtils, Vector2}
import com.badlogic.gdx.physics.box2d.{Body, World}
import com.unibo.s3.Main
import com.unibo.s3.main_system.characters.BaseCharacter
import com.unibo.s3.main_system.communication.Messages.{AskAllCharactersMsg, SendAllCharactersMsg}
import com.unibo.s3.main_system.communication.{GeneralActors, SystemManager}
import com.unibo.s3.main_system.util.ScaleUtils
import com.unibo.s3.main_system.world.actors.{RegisterAsWorldChangeObserver, WorldChangeMsg}
import com.unibo.s3.main_system.util.Box2dImplicits._

import scala.collection.mutable

case class AskIsPointAtShadow(p: Vector2)

class LightingSystemModule extends BasicModuleWithGui {

  private[this] var updatedWorld: Option[World] = None
  private[this] var rayHandler: RayHandler = _
  private[this] var cam: OrthographicCamera = _
  private[this] var worldObserverActor: ActorRef = _

  private[this] var torches = mutable.Map[Int, ConeLight]()
  private[this] var charactersUpdate: Option[Iterable[BaseCharacter]] = None

  private[this] val ambientLightColor = new Color(.1f, .1f, .1f, .1f)
  private[this] val brightWhite = new Color(1.0f, 1.0f, 1.0f, 1.0f)
  private[this] var worldShadowCopy: World = _


  private class LightingActor extends UntypedAbstractActor {
    override def onReceive(msg: Any): Unit = msg match {
      case WorldChangeMsg(bd) =>
        worldShadowCopy.createBox(bd.getWorldCenter, bd.size2)

      case AskIsPointAtShadow(p) =>
        sender ! rayHandler.pointAtShadow(p.x, p.y)

      case SendAllCharactersMsg(character) =>
        charactersUpdate = Option(character)
    }
  }

  override def init(owner: Main): Unit = {
    super.init(owner)
    cam = owner.getCamera()
    worldShadowCopy = new World(new Vector2(0,0), true)
    rayHandler = new RayHandler(
      worldShadowCopy, Gdx.graphics.getWidth/4, Gdx.graphics.getHeight/4)
  }

  def setup(): Unit = {

    worldObserverActor =
      SystemManager.createActor(Props(new LightingActor), "worldObserver")

    SystemManager
      .getLocalGeneralActor(GeneralActors.WORLD_ACTOR)
      .tell(RegisterAsWorldChangeObserver, worldObserverActor)

    rayHandler.setWorld(worldShadowCopy)
    RayHandler.setGammaCorrection(true)//false
    RayHandler.useDiffuseLight(true)
    rayHandler.setBlur(true)
    rayHandler.setBlurNum(1)
    rayHandler.setShadows(true)
    rayHandler.setCulling(false)
    rayHandler.setAmbientLight(ambientLightColor)
    rayHandler.diffuseBlendFunc.set(GL20.GL_DST_COLOR, GL20.GL_SRC_COLOR)
  }

  override def attachInputProcessors(inputMultiplexer: InputMultiplexer): Unit = {
    super.attachInputProcessors(inputMultiplexer)
    inputMultiplexer.addProcessor(this)
  }

  private def updateTorches() = {
    if (charactersUpdate.isDefined) {
      val characters = charactersUpdate.get
      characters.foreach(c => {
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
    charactersUpdate = None
  }

  override def update(dt: Float): Unit = {
    super.update(dt)
    worldShadowCopy.step(dt, 8, 3)

    SystemManager
      .getLocalGeneralActor(GeneralActors.QUAD_TREE_ACTOR)
      .tell(AskAllCharactersMsg, worldObserverActor)

    updateTorches()
  }

  override def touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean = {
    if (button == 1) {
      val mouseWorldPos = owner.screenToWorld(new Vector2(screenX, screenY))
      mouseWorldPos.scl(ScaleUtils.getMetersPerPixel)
      new PointLight(rayHandler, 32, brightWhite, 25, mouseWorldPos.x, mouseWorldPos.y)
    }
    false
  }

  override def resize(newWidth: Int, newHeight: Int): Unit = {
    super.resize(newWidth, newHeight)
    /*rayHandler.useCustomViewport(cam.position.x.toInt, cam.position.y.toInt,
      cam.viewportWidth.toInt, cam.viewportHeight.toInt)*/
  }

  override def customRender(): Unit = {
    val z = cam.zoom
    rayHandler.setCombinedMatrix(
      cam.combined.cpy().scl(ScaleUtils.getPixelsPerMeter.toFloat),
      cam.position.x, cam.position.y,
      cam.viewportWidth * z, cam.viewportHeight * z)
    rayHandler.updateAndRender()
    //rayHandler.useDefaultViewport()
  }

  override def cleanup(): Unit = {
    super.cleanup()
    rayHandler.dispose()
  }
}
