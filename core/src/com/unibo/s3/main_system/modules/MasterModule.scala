package com.unibo.s3.main_system.modules

import akka.actor.{ActorRef, ActorSelection, Props, UntypedAbstractActor}
import com.badlogic.gdx.graphics.Color._
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.{Color, OrthographicCamera}
import com.badlogic.gdx.math.{Rectangle, Vector2}
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.{Input, InputMultiplexer}
import com.kotcrab.vis.ui.widget.{BusyBar, VisLabel, VisWindow}
import com.unibo.s3.Main
import com.unibo.s3.main_system.characters.{BaseCharacter, Thief}
import com.unibo.s3.main_system.communication.CharacterActors._
import com.unibo.s3.main_system.communication.GeneralActors.{apply => _, _}
import com.unibo.s3.main_system.communication.Messages._
import com.unibo.s3.main_system.communication.{GeneralActors, SystemManager}
import com.unibo.s3.main_system.game.{AkkaSettings, GameSettings}
import com.unibo.s3.main_system.graph.GraphAdapter
import com.unibo.s3.main_system.rendering._
import com.unibo.s3.main_system.util.ImplicitConversions._
import com.unibo.s3.main_system.util.{GntUtils, ScaleUtils}
import com.unibo.s3.main_system.world.actors.GetAllBodies
import com.unibo.s3.main_system.world.{BodyData, Exit}

class MasterModule extends BasicModuleWithGui with GameOverlay {
  import MasterModule._

  private[this] var debugRendering = false
  private[this] var graph: Option[GraphAdapter[Vector2]] = None
  private[this] var characters: Option[Iterable[BaseCharacter]] = None
  private[this] val thiefCaughtMsg = "Thief got caught!"
  private[this] val thiefReachedExitMsg = "Thief reached exit!"
  private[this] val defaultPopupDuration = 3f
  private[this] var gameEventsBuffer = List[Any]()

  class GameActor extends UntypedAbstractActor {

    override def onReceive(message: Any): Unit = message match {

      case ThiefReachedExitMsg(t) =>
        gameEventsBuffer :+= message
        getActor(
          LIGHTING_SYSTEM_ACTOR) ! CreatePointLightAt(t.getPosition, Color.RED)
        val p = t.getPosition.cpy().scl(ScaleUtils.getPixelsPerMeter.toFloat)
        showPopupFadingText(p, thiefReachedExitMsg, defaultPopupDuration, Color.RED)
        evalGameState()

      case ThiefCaughtMsg(t, g) =>
        gameEventsBuffer :+= message
        getActor(
          LIGHTING_SYSTEM_ACTOR) ! CreatePointLightAt(g.getPosition, Color.BLUE)
        val p = t.getPosition.cpy().scl(ScaleUtils.getPixelsPerMeter.toFloat)
        showPopupFadingText(p, thiefCaughtMsg, defaultPopupDuration, Color.GREEN)
        evalGameState()

      case SendAllCharactersMsg(_characters) =>
        characters = Option(_characters)

      case ToggleViewDebug(d) =>
        debugRendering = d

      case SendGraphMsg(g) =>
        graph = Option(g)
        busyBarWindow.addAction(
          Actions.sequence(Actions.fadeOut(1.5f), Actions.run(new Runnable {
            override def run(): Unit = {
                busyBarWindow.remove()
                loadingFinished = true
                lightingActor ! ToggleLightingSystem(loadingFinished)
              }
          })))
        worldActor ! GetAllBodies()

      case bodies: Iterable[Body] =>
        bodies.foreach(b => b.getUserData match {
          case bd: BodyData =>
            if (bd.bodyType.contains(Exit)) exitLocations :+= b.getWorldCenter
          case _ => ()
        })
        cacheMap(bodies)
    }
  }

  object GameActor {
    def props() : Props = Props(new GameActor())
  }

  private[this] var masterActor: ActorRef = _
  private[this] var worldActor: ActorRef = _
  private[this] var mapActor: ActorSelection = _
  private[this] var graphActor: ActorSelection = _
  private[this] var quadTreeActor: ActorRef = _
  private[this] var gameActor: ActorRef = _
  private[this] var spawnActor: ActorSelection = _
  private[this] var lightingActor: ActorRef = _

  private[this] val renderer = new GeometryRendererImpl() with GraphCache
  private[this] val spriteRenderer = SpriteRenderer()
  private[this] var worldMap = List[Rectangle]()
  private[this] var busyBarWindow: VisWindow = _
  private[this] var endGameDialog: VisWindow = _
  private[this] var gameOver = false
  private[this] var loadingFinished = false
  private[this] var exitLocations = List[Vector2]()

  private def getActor(actor: GeneralActors.Value): ActorRef =
    SystemManager.getLocalActor(actor)

  private def getRemoteActor(actor: String): ActorSelection =
    SystemManager.getRemoteActor(AkkaSettings.ComputeSystem, "/user/", actor)

  private def cacheMap(bodies: Iterable[Body]) = {
    worldMap = GntUtils.parseBodiesToMap(bodies).toList
  }

  private def evalGameState(): Unit = {
    characters.foreach(c => {
      val thieves =  c.collect {case t: Thief => t}
      val evadedThieves = thieves.count(t => t.hasReachedExit)
      val caughtThieves = thieves.count(t => t.gotCaughtByGuard)

      if (!gameOver && evadedThieves + caughtThieves == thieves.size) {
        endGameDialog.setModal(true)
        endGameDialog.getTitleLabel.setColor(Color.RED)
        endGameDialog.add(new VisLabel(evadedThieves + " thieves evaded.")).fillX()
        endGameDialog.row()
        endGameDialog.add(new VisLabel(caughtThieves + " thieves got caught by guards.")).fillX()
        endGameDialog.pack()
        endGameDialog.centerWindow()
        endGameDialog.fadeIn(1.5f)
        gui.addActor(endGameDialog)
        gameOver = true
      }
    })
  }
  override def init(owner: Main): Unit = {
    super.init(owner)
    busyBarWindow = new VisWindow("Generating map...")
    val bb = new BusyBar()
    bb.getStyle.height = 12
    bb.setWidth(gui.getWidth / 4f)
    busyBarWindow.add[BusyBar](bb).center().expandX().fillX().pad(4,0,4,0).row()
    busyBarWindow.pack()
    endGameDialog = new VisWindow("Game over!")
    gui.addActor(busyBarWindow)
    busyBarWindow.centerWindow()
    spriteRenderer.init()
    spriteRenderer.setDebugDraw(false)
  }

  def initGame(config: GameSettings): Unit = {

    val mapSize = config.mapSize
    val w = mapSize.x.toInt
    val h = mapSize.y.toInt

    masterActor = getActor(GeneralActors.MASTER_ACTOR)
    mapActor = getRemoteActor(GeneralActors.MAP_ACTOR.name)
    worldActor = getActor(GeneralActors.WORLD_ACTOR)
    quadTreeActor = getActor(GeneralActors.QUAD_TREE_ACTOR)
    graphActor = getRemoteActor(GeneralActors.GRAPH_ACTOR.name)
    gameActor = SystemManager.createActor(
      GameActor.props(), GeneralActors.GAME_ACTOR)
    lightingActor = getActor(GeneralActors.LIGHTING_SYSTEM_ACTOR)
    spawnActor = getRemoteActor(GeneralActors.SPAWN_ACTOR.name)

    graphActor ! GameSettingsMsg(config)
    quadTreeActor ! GameSettingsMsg(config)
    mapActor ! GameSettingsMsg(config)
    mapActor ! GenerateMapMsg()
    graphActor tell(AskForGraphMsg, gameActor)
    spawnActor ! GameSettingsMsg(config)
  }

  override def update(dt: Float): Unit = {
    super.update(dt)
    masterActor ! ActMsg(dt)
    spriteRenderer.update(dt)
    overlay.getCamera.position.set(owner.getCamera.position)
    overlay.getCamera.asInstanceOf[OrthographicCamera].zoom = owner.getCamera.zoom
    gameEventsBuffer.foreach(e => showGameEventToast(e))
    gameEventsBuffer = List()
  }

  override def cleanup(): Unit = {
    super.cleanup()
    SystemManager.shutdownSystem()
    spriteRenderer.dispose()
  }

  override def render(shapeRenderer: ShapeRenderer): Unit = {
    super.render(shapeRenderer)

    if (loadingFinished) {
      if (debugRendering)
        graph.foreach(g =>renderer.renderGraph(shapeRenderer, g, DefaultGraphRenderingConfig))

      renderer.renderMap(shapeRenderer, worldMap)

      characters.foreach(characters =>
        characters.foreach(c => {
          spriteRenderer.render(c, owner.getCamera)
          if (debugRendering) renderer.renderCharacterDebugInfo(shapeRenderer, c)
        }))

      spriteRenderer.renderExits(exitLocations, owner.getCamera)
    }
  }

  override def attachInputProcessors(inputMultiplexer: InputMultiplexer): Unit = {
    super.attachInputProcessors(inputMultiplexer)
    inputMultiplexer.addProcessor(this)
  }

  override def touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean = {
    if (button != 1 && debugRendering){
      spawnActor.tell(
        GenerateNewCharacterPositionMsg(1, GUARD), masterActor)
    }
    false
  }

  override def keyUp(keycode: Int): Boolean = {
    if(keycode == Input.Keys.T && debugRendering) {
      spawnActor.tell(
        GenerateNewCharacterPositionMsg(1, THIEF), masterActor)
    }
    false
  }

  override def renderGui(): Unit = {
    super.renderGui()
  }
}

object MasterModule {
  private val DefaultGraphRenderingConfig = GraphRenderingConfig(GREEN, YELLOW, 0.5f)
  def apply: MasterModule = new MasterModule()
}
