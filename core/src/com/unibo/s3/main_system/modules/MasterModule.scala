package com.unibo.s3.main_system.modules

import akka.actor.{ActorRef, Props, UntypedAbstractActor}
import com.badlogic.gdx.graphics.Color._
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.{Color, OrthographicCamera}
import com.badlogic.gdx.math.{Rectangle, Vector2}
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.{Gdx, Input, InputMultiplexer}
import com.kotcrab.vis.ui.widget.{BusyBar, VisWindow}
import com.unibo.s3.Main
import com.unibo.s3.main_system.characters.BaseCharacter
import com.unibo.s3.main_system.communication.CharacterActors._
import com.unibo.s3.main_system.communication.GeneralActors.{apply => _, _}
import com.unibo.s3.main_system.communication.Messages._
import com.unibo.s3.main_system.communication.{GeneralActors, SystemManager}
import com.unibo.s3.main_system.game.GameSettings
import com.unibo.s3.main_system.graph.GraphAdapter
import com.unibo.s3.main_system.rendering._
import com.unibo.s3.main_system.util.ImplicitConversions._
import com.unibo.s3.main_system.util.{GntUtils, ScaleUtils}
import com.unibo.s3.main_system.world.actors.GetAllBodies
import com.unibo.s3.main_system.world.{BodyData, Exit}

class MasterModule extends BasicModuleWithGui with GameOverlay {
  import MasterModule._

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
        showPopupFadingTest(p, thiefReachedExitMsg, defaultPopupDuration, Color.RED)

      case ThiefCaughtMsg(t, g) =>
        gameEventsBuffer :+= message
        getActor(
          LIGHTING_SYSTEM_ACTOR) ! CreatePointLightAt(g.getPosition, Color.BLUE)
        val p = t.getPosition.cpy().scl(ScaleUtils.getPixelsPerMeter.toFloat)
        showPopupFadingTest(p, thiefCaughtMsg, defaultPopupDuration, Color.GREEN)

      case SendAllCharactersMsg(_characters) =>
        characters = Option(_characters)

      case SendGraphMsg(g) =>
        graph = Option(g)
        busyBarWindow.addAction(
          Actions.sequence(Actions.fadeOut(1.5f), Actions.run(new Runnable {
              override def run(): Unit = busyBarWindow.remove()})))
        cacheMap()
        worldActor ! GetAllBodies()

      case bodies: Iterable[Body] =>
        bodies.foreach(b => b.getUserData match {
          case bd: BodyData =>
            println("sizee2", bd)
            if (bd.bodyType.contains(Exit)) exitLocations :+= b.getWorldCenter
          case d =>
            println("sizee", d)
        })
    }
  }

  object GameActor {
    def props() : Props = Props(new GameActor())
  }

  private[this] var masterActor: ActorRef = _
  private[this] var worldActor: ActorRef = _
  private[this] var mapActor: ActorRef = _
  private[this] var graphActor: ActorRef = _
  private[this] var quadTreeActor: ActorRef = _
  private[this] var gameActor: ActorRef = _
  private[this] var spawnActor: ActorRef = _

  private[this] val renderer = GeometryRendererImpl()
  private[this] val spriteRenderer = SpriteRenderer()
  private[this] var worldMap = List[Rectangle]()
  private[this] var busyBarWindow: VisWindow = _
  private[this] var exitLocations = List[Vector2]()

  private def getActor(actor: GeneralActors.Value): ActorRef =
    SystemManager.getLocalActor(actor)

  private def cacheMap() = {
    worldMap = GntUtils.parseMapToRectangles(
      Gdx.files.internal(MapFilePath)).toList
  }

  override def init(owner: Main): Unit = {
    super.init(owner)
    busyBarWindow = new VisWindow("Generating map...")
    val bb = new BusyBar()
    bb.getStyle.height = 12
    bb.setWidth(gui.getWidth / 4f)
    busyBarWindow.add[BusyBar](bb).center().expandX().fillX().pad(4,0,4,0).row()
    busyBarWindow.pack()
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
    mapActor = getActor(GeneralActors.MAP_ACTOR)
    worldActor = getActor(GeneralActors.WORLD_ACTOR)
    quadTreeActor = getActor(GeneralActors.QUAD_TREE_ACTOR)
    graphActor = getActor(GeneralActors.GRAPH_ACTOR)
    gameActor = SystemManager.createActor(
      GameActor.props(), GeneralActors.GAME_ACTOR)
    spawnActor = getActor(GeneralActors.SPAWN_ACTOR)

    List(graphActor, quadTreeActor).foreach(a =>
      a ! MapSettingsMsg(w, h))

    mapActor ! MapSettingsMsg(w, h)

    mapActor ! GenerateMapMsg()
    graphActor tell(AskForGraphMsg, gameActor)

    spawnActor ! MapSettingsMsg(30, 30)
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

    graph.foreach(g =>
      renderer.renderGraph(shapeRenderer, g, DefaultGraphRenderingConfig))

    renderer.renderMap(shapeRenderer, worldMap)

    characters.foreach(characters =>
      characters.foreach(c => {
        spriteRenderer.render(c, owner.getCamera)
        renderer.renderCharacterDebugInfo(shapeRenderer, c)
      }))
  }

  override def attachInputProcessors(inputMultiplexer: InputMultiplexer): Unit = {
    super.attachInputProcessors(inputMultiplexer)
    inputMultiplexer.addProcessor(this)
  }

  override def touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean = {
    if (button != 1){
      spawnActor.tell(
        GenerateNewCharacterPositionMsg(GUARD), masterActor)
    }
    false
  }

  override def keyUp(keycode: Int): Boolean = {
    if(keycode == Input.Keys.T) {
      spawnActor.tell(
        GenerateNewCharacterPositionMsg(THIEF), masterActor)
    }
    false
  }

  override def renderGui(): Unit = {
    spriteRenderer.renderExits(exitLocations, owner.getCamera)
    super.renderGui()
  }
}

object MasterModule {
  private val DefaultGraphRenderingConfig = GraphRenderingConfig(GREEN, YELLOW, 0.5f)
  private val MapFilePath = "maps/map.txt"

  def apply: MasterModule = new MasterModule()
}
