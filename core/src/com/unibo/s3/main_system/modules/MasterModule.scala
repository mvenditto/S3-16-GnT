package com.unibo.s3.main_system.modules

import java.util

import akka.actor.{ActorRef, Props, UntypedAbstractActor}
import akka.pattern.ask
import akka.util.Timeout
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.{Rectangle, Vector2}
import com.badlogic.gdx.{Gdx, InputMultiplexer}
import com.unibo.s3.main_system.characters.BaseCharacter
import com.unibo.s3.main_system.communication.Messages._
import com.unibo.s3.main_system.communication.SystemManager
import com.unibo.s3.main_system.graph.GraphAdapter
import com.unibo.s3.main_system.rendering.{GeometryRendererImpl, GraphRenderingConfig}
import com.unibo.s3.main_system.util.ScaleUtils
import org.jgrapht.alg.NeighborIndex
import org.jgrapht.graph.DefaultEdge

import scala.concurrent.Await
import scala.concurrent.duration._


class MasterModule extends BasicModuleWithGui {

  private[this] var graph: Option[GraphAdapter[Vector2]] = None
  private[this] var characters: Option[Iterable[BaseCharacter]] = None

  class DummyReceiverActor extends UntypedAbstractActor {

    override def onReceive(message: Any): Unit = message match {

      case i: Int => println(i)

      case SendAllCharactersMsg(_characters) =>
        characters = Option(_characters)

      case SendGraphMsg(g) =>
        println("receive graph!")
        graph = Option(new GraphAdapter[Vector2] {
          override def getNeighbors(vertex: Vector2): util.Iterator[Vector2] = {
            new NeighborIndex[Vector2, DefaultEdge](g)
              .neighborsOf(vertex).iterator
          }
          override def getVertices: util.Iterator[Vector2] = g.vertexSet.iterator
        })
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

  private[this] val renderer = new GeometryRendererImpl()
  private[this] val graphRenderingConfig = GraphRenderingConfig(Color.GREEN, Color.YELLOW, 0.5f)
  private[this] val mapFilePath = "maps/map.txt"
  private[this] var worldMap = List[Rectangle]()

  private[this] var actorsMap: Option[Map[GameActors.Value, String]] = None

  private def getActor(actor: GameActors.Value): ActorRef =
    SystemManager.getInstance.getLocalActor(actorsMap.get(actor))

  private def cacheMap() = {
    val map = Gdx.files.internal(mapFilePath)

    worldMap = map.readString().split("\n")
      .map(b => b.split(":").map(f => f.toFloat))
      .map(b => new Rectangle(b(0),b(1),b(2),b(3))).toList
  }

  /*must be replaced with settings taken from init gui.*/
  def dummyInit(actorsMap: Map[GameActors.Value, String]): Unit = {
    this.actorsMap = Option(actorsMap)

    masterActor = getActor(GameActors.Master)
    mapActor = getActor(GameActors.Map)
    worldActor = getActor(GameActors.World)
    quadTreeActor = getActor(GameActors.QuadTree)
    graphActor = getActor(GameActors.Graph)
    dummyReceiverActor = SystemManager.getInstance()
      .createActor(DummyReceiverActor.props(), "graphReceiver")

    List(graphActor, quadTreeActor).foreach(a =>
      a ! MapSettingsMsg(60, 60))
    mapActor ! MapSettingsMsg(20, 20)

    mapActor ! GenerateMapMsg()
    graphActor tell(AskForGraphMsg, dummyReceiverActor)
  }

  override def update(dt: Float): Unit = {
    super.update(dt)
    masterActor ! ActMsg(dt)
  }

  override def cleanup(): Unit = {
    super.cleanup()
    SystemManager.getInstance().shutdownSystem()
  }

  override def render(shapeRenderer: ShapeRenderer): Unit = {
    super.render(shapeRenderer)
    graph.foreach(g => renderer.renderGraph(shapeRenderer, g, graphRenderingConfig))
    renderer.renderMap(shapeRenderer, worldMap)
    characters.foreach(characters =>
      characters.foreach(c => renderer.renderCharacter(shapeRenderer, c)))
  }

  override def attachInputProcessors(inputMultiplexer: InputMultiplexer): Unit = {
    super.attachInputProcessors(inputMultiplexer)
    inputMultiplexer.addProcessor(this)
  }

  override def touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean = {
    val mouseWorldPos = owner.screenToWorld(new Vector2(screenX, screenY))
    mouseWorldPos.scl(ScaleUtils.getMetersPerPixel)
    masterActor ! CreateCharacterMsg(mouseWorldPos)
    quadTreeActor tell(AskAllCharactersMsg, dummyReceiverActor)
    false
  }

}
