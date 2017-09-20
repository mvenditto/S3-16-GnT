package com.unibo.s3

import com.badlogic.gdx.{Gdx, InputMultiplexer, Preferences}
import com.badlogic.gdx.graphics.{Color, OrthographicCamera}
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.kotcrab.vis.ui.VisUI
import com.unibo.s3.main_system.AbstractMainApplication
import com.unibo.s3.main_system.communication.Messages.ToggleViewDebug
import com.unibo.s3.main_system.communication.{GeneralActors, SystemManager}
import com.unibo.s3.main_system.game.GameSettings
import com.unibo.s3.main_system.modules._
import com.unibo.s3.main_system.util.ScaleUtils._

class Main extends AbstractMainApplication {
  private var modules = List[BasicModule]()
  private var inputMultiplexer: InputMultiplexer = _

  private var bootstrapModule: BootstrapModule = _
  private[this] var cm: MenuModule = _
  private[this] val prefsName = "GntPreferences"
  private[this] var prefs: Preferences =  _

  override def create(): Unit = {
    super.create()
    prefs = Gdx.app.getPreferences(prefsName)
    inputMultiplexer = new InputMultiplexer
    VisUI.load()

    addModules()

    modules.foreach(m => {
      m.init(this)
      m.attachInputProcessors(inputMultiplexer)
    })

    inputMultiplexer.addProcessor(this)
    Gdx.input.setInputProcessor(inputMultiplexer)
  }

  private def addModules() = {

    val master = new MasterModule()
    master.enable(false)

    val lighting = new LightingSystemModule()
    lighting.enable(false)

    var settings: Option[GameSettings] = None
    val cm = new MenuModule({
      case Start(guardsNum, thiefsNum, simulation, mapDimension, mazeTypeMap) =>
        settings = Option(GameSettings(mapSize = mapDimension))
        bootstrapModule.enable(true)
      case Pause(pause) =>
      case Stop() =>
      case ViewDebug(debug) =>
        val gameRef = SystemManager.getLocalActor(GeneralActors.GAME_ACTOR)
        gameRef ! ToggleViewDebug(debug)
    })
    cm.enable(true)

    bootstrapModule = new BootstrapModule({
      case BootstrapOk() =>

      case BootstrapFailed(err) =>
        println(err)

      case UserAck() if settings.isDefined =>
        master.initGame(settings.get)
        master.enable(true)
        lighting.setup()
        lighting.enable(true)
        removeModule(bootstrapModule)
    })
    bootstrapModule.enable(false)


    modules :+= cm
    modules :+= bootstrapModule
    modules :+= master
    modules :+= lighting
  }

  private def renderAxis(shapeRenderer: ShapeRenderer) = {
    val oldColor = shapeRenderer.getColor
    val worldCenter = new Vector2(0f, 0f)
    val metersRadius = 1f
    val pixelRadius = metersToPixels(metersRadius)
    val axisMetersLength = 50
    val axisPixelLenght = metersToPixels(axisMetersLength)
    /*draw world center*/
    shapeRenderer.setColor(Color.RED)
    shapeRenderer.circle(worldCenter.x, worldCenter.y, pixelRadius.toFloat)
    /*draw x axis*/
    shapeRenderer.setColor(Color.BLUE)
    shapeRenderer.line(-axisPixelLenght.toFloat, 0, axisPixelLenght.toFloat, 0)
    /*draw y axis*/
    shapeRenderer.setColor(Color.GREEN)
    shapeRenderer.line(0, -axisPixelLenght.toFloat, 0, axisPixelLenght.toFloat)
    shapeRenderer.setColor(oldColor)
    /*draw mouse WORLD vs SCREEN position*/
    val mouseScreenPos = new Vector2(Gdx.input.getX, Gdx.input.getY)
    /*scale by 1.0 / pixelPerMeter --> box2D coords*/
    val mouseWorldPos = screenToWorld(mouseScreenPos).cpy.scl(getMetersPerPixel)
    //shapeRenderer.circle(metersToPixels(mouseWorldPos.x),
    //        metersToPixels(mouseWorldPos.y), metersToPixels(3));
    textBatch.begin()
    /*Flip y !!!*/ font.setColor(Color.ORANGE)
    font.draw(textBatch, "screen: " + mouseScreenPos, Gdx.graphics.getWidth / 2, 30f)
    font.setColor(Color.YELLOW)
    font.draw(textBatch, "world: " + mouseWorldPos, Gdx.graphics.getWidth / 2, 15f)
    textBatch.end()
  }

  private def removeModule(m: BasicModule) = {

    Gdx.app.postRunnable(new Runnable {
      override def run(): Unit = {
        m.cleanup()
        m.enable(false)
        modules = modules.filter(mod => !mod.equals(m))
      }
    })
  }

  def getCamera: OrthographicCamera = cam

  def getPrefs: Preferences = prefs

  override protected def doRender(): Unit = {
    renderAxis(shapeRenderer)
    val enabledModules = modules.filter(m => m.isEnabled)
    enabledModules.foreach(m => m.render(shapeRenderer))
  }

  override def doCustomRender(): Unit = {
    val enabledModules = modules.filter(m => m.isEnabled)
    enabledModules.foreach(m =>m.customRender())
    enabledModules.foreach(m => m.renderGui())
  }

  override def doUpdate(delta: Float): Unit =
    modules.filter(m => m.isEnabled).foreach(m => m.update(delta))

  override def resize(newWidth: Int, newHeight: Int): Unit = {
    super.resize(newWidth, newHeight)
    modules.foreach(m => m.resize(newWidth, newHeight))
  }

}