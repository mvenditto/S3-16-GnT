package com.unibo.s3

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.{Gdx, InputMultiplexer, Preferences}
import com.kotcrab.vis.ui.VisUI
import com.unibo.s3.main_system.AbstractMainApplication
import com.unibo.s3.main_system.communication.Messages.ToggleViewDebug
import com.unibo.s3.main_system.communication.{GeneralActors, SystemManager}
import com.unibo.s3.main_system.game._
import com.unibo.s3.main_system.modules._

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
      case Start(guardsNum, thievesNum, simulation, mapDimension, mazeTypeMap) =>
        var mapType: MapType = null
        mazeTypeMap match {
          case true => mapType = Maze
          case _ => mapType = Rooms
        }
        settings = Option(GameSettings(guardsNumber = guardsNum,
          thievesNumber = thievesNum, mapSize = mapDimension, mapType = mapType))
        bootstrapModule.enable(true)
      case Pause(pause) => paused = pause
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
        lighting.setup()
        master.initGame(settings.get)
        master.enable(true)
        lighting.enable(true)
        removeModule(bootstrapModule)
    })
    bootstrapModule.enable(false)


    modules :+= cm
    modules :+= bootstrapModule
    modules :+= master
    modules :+= lighting
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