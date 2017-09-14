package com.unibo.s3.main_system.modules

import akka.actor.{ActorRef, Props, UntypedAbstractActor}
import com.badlogic.gdx.graphics.Color._
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.{Rectangle, Vector2}
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.{Gdx, Input, InputMultiplexer}
import com.kotcrab.vis.ui.widget.{BusyBar, VisWindow}
import com.unibo.s3.Main
import com.unibo.s3.main_system.characters.BaseCharacter
import com.unibo.s3.main_system.communication.Messages._
import com.unibo.s3.main_system.communication.{CharacterActors, GeneralActors, SystemManager}
import com.unibo.s3.main_system.game.GameSettings
import com.unibo.s3.main_system.graph.GraphAdapter
import com.unibo.s3.main_system.rendering.{GeometryRendererImpl, GraphRenderingConfig, SpriteRenderer}
import com.unibo.s3.main_system.util.ImplicitConversions._
import com.unibo.s3.main_system.util.{GntUtils, ScaleUtils}

class MasterModule extends BasicModuleWithGui {
  import MasterModule._

  private[this] var graph: Option[GraphAdapter[Vector2]] = None
  private[this] var characters: Option[Iterable[BaseCharacter]] = None

  class DummyReceiverActor extends UntypedAbstractActor {

    override def onReceive(message: Any): Unit = message match {

      case i: Int => println(i)

      case SendAllCharactersMsg(_characters) =>
        characters = Option(_characters)

      case SendGraphMsg(g) =>
        graph = Option(g)
        busyBarWindow.addAction(
          Actions.sequence(
            Actions.fadeOut(1.5f), Actions.run(new Runnable {
              override def run(): Unit = busyBarWindow.remove()
            })))
        cacheMap()
    }
  }

  object DummyReceiverActor {
    def props() : Props = Props(new DummyReceiverActor())
  }

  private[this] var masterActor: ActorRef = _
  private[this] var worldActor: ActorRef = _
  private[this] var mapActor: ActorRef = _
  private[this] var graphActor: ActorRef = _
  private[this] var quadTreeActor: ActorRef = _
  private[this] var dummyReceiverActor: ActorRef = _
  private[this] var spawnActor: ActorRef = _

  private[this] val renderer = GeometryRendererImpl()
  private[this] val spriteRenderer = SpriteRenderer()
  private[this] var worldMap = List[Rectangle]()
  private[this] var busyBarWindow: VisWindow = _

  private def getActor(actor: GeneralActors.Value): ActorRef =
    SystemManager.getLocalGeneralActor(actor)

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
    dummyReceiverActor = SystemManager
      .createActor(DummyReceiverActor.props(), "graphReceiver")
    spawnActor = getActor(GeneralActors.SPAWN_ACTOR)

    List(graphActor, quadTreeActor).foreach(a =>
      a ! MapSettingsMsg(w, h))

    mapActor ! MapSettingsMsg(w, h)

    mapActor ! GenerateMapMsg()
    graphActor tell(AskForGraphMsg, dummyReceiverActor)

    spawnActor ! MapSettingsMsg(30, 30)
  }

  override def update(dt: Float): Unit = {
    super.update(dt)
    masterActor ! ActMsg(dt)
    spriteRenderer.update(dt)
  }

  override def cleanup(): Unit = {
    super.cleanup()
    SystemManager.shutdownSystem()
    spriteRenderer.dispose()
  }

  override def render(shapeRenderer: ShapeRenderer): Unit = {
    super.render(shapeRenderer)

    //spriteRenderer.renderFloor(64, 64, owner.getCamera)

    graph.foreach(g =>
      renderer.renderGraph(shapeRenderer, g, DefaultGraphRenderingConfig))

    renderer.renderMap(shapeRenderer, worldMap)

    /*
    characters.foreach(characters =>
      characters.foreach(c => renderer.renderCharacter(shapeRenderer, c)))*/

    characters.foreach(characters =>
      characters.foreach(c => spriteRenderer.render(c, owner.getCamera)))
  }

  override def attachInputProcessors(inputMultiplexer: InputMultiplexer): Unit = {
    super.attachInputProcessors(inputMultiplexer)
    inputMultiplexer.addProcessor(this)
  }

  override def touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean = {
    //need to be fixed, out of sync, new characters shows only after next added.
    quadTreeActor tell(AskAllCharactersMsg, dummyReceiverActor)
    false
  }

  override def touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean = {
    if (button != 1){
      val mouseWorldPos = owner.screenToWorld(new Vector2(screenX, screenY))
      mouseWorldPos.scl(ScaleUtils.getMetersPerPixel)
      spawnActor ! GenerateNewCharacterPositionMsg(CharacterActors.GUARD)
      //masterActor ! CreateCharacterMsg(mouseWorldPos)
    }
    false
  }

  override def keyUp(keycode: Int): Boolean = {
    if(keycode == Input.Keys.T) {
      spawnActor ! GenerateNewCharacterPositionMsg(CharacterActors.THIEF)
    }
    false
  }
}

object MasterModule {
  private val DefaultGraphRenderingConfig = GraphRenderingConfig(GREEN, YELLOW, 0.5f)
  private val MapFilePath = "maps/map.txt"

  def apply: MasterModule = new MasterModule()
}
