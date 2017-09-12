package com.unibo.s3.main_system.modules

import akka.actor.{ActorRef, Props, UntypedAbstractActor}
import box2dLight.{ConeLight, PointLight, RayHandler}
import com.badlogic.gdx.{Gdx, InputMultiplexer, Preferences}
import com.badlogic.gdx.graphics.{Color, GL20, OrthographicCamera}
import com.badlogic.gdx.math.{MathUtils, Vector2}
import com.badlogic.gdx.physics.box2d.World
import com.unibo.s3.Main
import com.unibo.s3.main_system.util.GntMathUtils.keepInRange
import com.unibo.s3.main_system.characters.BaseCharacter
import com.unibo.s3.main_system.communication.Messages.{AskAllCharactersMsg, SendAllCharactersMsg}
import com.unibo.s3.main_system.communication.{GeneralActors, SystemManager}
import com.unibo.s3.main_system.util.Box2dImplicits._
import com.unibo.s3.main_system.util.ScaleUtils
import com.unibo.s3.main_system.world.actors.{RegisterAsWorldChangeObserver, WorldChangeMsg}

import scala.collection.mutable

case class AskIsPointAtShadow(p: Vector2)

case class LightingSystemConfig(
  enableLightingSystem: Boolean,
  enableDiffuseLight: Boolean,
  enableCulling: Boolean,
  enableGammaCorrection: Boolean,
  enableBlur: Boolean,
  enableShadows: Boolean,
  lightQualityModiefier: Float,
  blurLevel: Int,
  blendingFunc: Int
  )

class LightingSystemModule extends BasicModuleWithGui {
  import LightingSystemModule._

  private[this] var rayHandler: RayHandler = _
  private[this] var cam: OrthographicCamera = _
  private[this] var worldObserverActor: ActorRef = _

  private[this] val torches = mutable.Map[Int, ConeLight]()
  private[this] var charactersUpdate: Option[Iterable[BaseCharacter]] = None
  private[this] var worldShadowCopy: World = _
  private[this] var ambientLightIntensity = 0.2f

  private class LightingActor extends UntypedAbstractActor {
    override def onReceive(msg: Any): Unit = msg match {
      /*
      Sync with 'world' held by WorldActor, this is needed because:
       - don't want to expose 'world' from WorldActor,
       - RayHandler uses RayCasting and we can't use the 'proxy' version
       that works with WorldActor.
      */
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
    cam = owner.getCamera
    worldShadowCopy = new World(new Vector2(0,0), true)
    rayHandler = new RayHandler(worldShadowCopy)
  }

  def setup(): Unit = {

    worldObserverActor =
      SystemManager.createActor(Props(new LightingActor), "worldObserver")

    SystemManager
      .getLocalGeneralActor(GeneralActors.WORLD_ACTOR)
      .tell(RegisterAsWorldChangeObserver, worldObserverActor)

    val c = loadConfigFromPreferences(owner.getPrefs)
    val lqm = c.lightQualityModiefier

    rayHandler.setWorld(worldShadowCopy)
    RayHandler.setGammaCorrection(c.enableGammaCorrection)
    RayHandler.useDiffuseLight(c.enableDiffuseLight)

    rayHandler.resizeFBO(
      (Gdx.graphics.getWidth * lqm).toInt,
      (Gdx.graphics.getHeight * lqm).toInt)

    rayHandler.setBlur(c.enableBlur)
    rayHandler.setBlurNum(c.blurLevel)
    rayHandler.setShadows(c.enableShadows)
    rayHandler.setCulling(c.enableCulling)
    rayHandler.setAmbientLight(WhiteAmbientLightColor)

    val blendFunc = c.blendingFunc match {
      case 1 => (GL20.GL_DST_COLOR, GL20.GL_ZERO)
      case 2 => (GL20.GL_DST_COLOR, GL20.GL_SRC_COLOR)
      case 3 => (GL20.GL_SRC_COLOR, GL20.GL_DST_COLOR)
    }
    rayHandler.diffuseBlendFunc.set(blendFunc._1, blendFunc._2)
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
        val lv = c.getLinearVelocity.cpy().nor().scl(1.0f).add(c.getPosition)
        if (torches.contains(id)) {
          val t = torches(id)
          t.setPosition(lv)
          t.setDirection(angle)
        } else {
          torches(id) = new ConeLight(
            rayHandler, TorchRaysNum, BrightWhiteColor, TorchDistance,
            lv.x, lv.y, angle, TorchDegrees)
        }
      })
    }
    charactersUpdate = None
  }

  override def update(dt: Float): Unit = {
    super.update(dt)
    //worldShadowCopy.step(dt, 8, 3)

    SystemManager
      .getLocalGeneralActor(GeneralActors.QUAD_TREE_ACTOR)
      .tell(AskAllCharactersMsg, worldObserverActor)

    updateTorches()
  }

  override def touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean = {
    if (button == 1) {
      val mouseWorldPos = owner.screenToWorld(new Vector2(screenX, screenY))
      mouseWorldPos.scl(ScaleUtils.getMetersPerPixel)
      new PointLight(
        rayHandler, PointLightRaysNum, SoftWhiteColor,
        PointLightRadius, mouseWorldPos.x, mouseWorldPos.y)
    }
    false
  }

  override def scrolled(amount: Int): Boolean = {
    ambientLightIntensity = keepInRange(ambientLightIntensity + (0.1f * amount), 0.0f, 1.0f)
    val al = ambientLightIntensity
    rayHandler.setAmbientLight(al, al, al, al)
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

object LightingSystemModule {
  private val WhiteAmbientLightColor = new Color(.2f, .2f, .2f, .2f)
  private val BrightWhiteColor = new Color(1.0f, 1.0f, 1.0f, 1.0f)
  private val SoftWhiteColor = new Color(1.0f, 1.0f, 1.0f, 0.7f)

  private val TorchRaysNum = 64
  private val TorchDistance = 15f
  private val TorchDegrees = 25f

  private val PointLightRaysNum = 32
  private val PointLightRadius = 25f

  private val LightQualityModifier = "ls_light_quality"
  private val LightSystemEnabled = "ls_enabled"
  private val DiffuseLightEnabled = "ls_diffuse_light_enabled"
  private val BlurEnabled = "ls_blur_enabled"
  private val BlurLevel = "ls_blur_level"
  private val CullingEnabled = "ls_culling_enabled"
  private val GammaCorrectionEnabled = "ls_gamma_correction_enabled"
  private val BlendingFunction = "ls_blend_func"
  private val EnableShadows = "ls_enable_shadows"

  def apply: LightingSystemModule = new LightingSystemModule()

  def loadConfigFromPreferences(p: Preferences): LightingSystemConfig = {
    LightingSystemConfig(
      p.getBoolean(LightSystemEnabled, true),
      p.getBoolean(DiffuseLightEnabled, true),
      p.getBoolean(CullingEnabled, false),
      p.getBoolean(GammaCorrectionEnabled, true),
      p.getBoolean(BlurEnabled, true),
      p.getBoolean(EnableShadows, true),
      keepInRange(p.getFloat(LightQualityModifier, 0.25f), 0.25f, 4f),
      keepInRange(p.getInteger(BlurLevel, 2).toFloat, 1f, 4f).toInt,
      keepInRange(p.getInteger(BlendingFunction, 1).toFloat, 0f, 3f).toInt
    )
  }
}
