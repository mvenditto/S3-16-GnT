package com.unibo.s3.testbed.samples

import java.util

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.{Rectangle, Vector2}
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.kotcrab.vis.ui.widget.{VisSelectBox, VisTextButton, VisWindow}
import com.unibo.s3.main_system.communication.Messages.{ActMsg, GenerateGraphMsg, GenerateMapMsg, MapSettingsMsg}
import com.unibo.s3.main_system.communication.Messages._
import com.unibo.s3.main_system.communication.SystemManager
import com.unibo.s3.main_system.graph.GraphAdapter
import com.unibo.s3.main_system.rendering.{GeometryRendererImpl, GraphRenderingConfig}
import com.unibo.s3.main_system.util.GdxImplicits._
import com.unibo.s3.main_system.world.actors.{CreateBox, ResetWorld}
import com.unibo.s3.testbed.{BaseSample, Testbed}
import org.jgrapht.alg.NeighborIndex
import org.jgrapht.graph.DefaultEdge

import scala.concurrent.Await
import scala.concurrent.duration._

class GraphMapTest extends BaseSample {

  private[this] var graphActor: ActorRef = _
  private[this] var worldActor: ActorRef = _
  private[this] var mapActor: ActorRef = _
  private[this] var graph: Option[GraphAdapter[Vector2]] = None
  private[this] val renderer = new GeometryRendererImpl()

  private[this] val graphRenderingConfig = GraphRenderingConfig(Color.GREEN, Color.YELLOW, 1.0f)
  private[this] val mapFilePath = "maps/map.txt"

  private[this] var worldMap = List[Rectangle]()

  private def cacheMap() = {
    val map = Gdx.files.internal(mapFilePath)
    worldMap = map.readString().split("\n")
      .map(b => b.split(":").map(f => f.toFloat))
      .map(b => new Rectangle(b(0),b(1),b(2),b(3))).toList
    worldMap.foreach(b => worldActor ! CreateBox(new Vector2(b.x, b.y), new Vector2(b.width, b.height)))
  }

  private def askForGraph(): Unit = {
    implicit val timeout = Timeout(120 seconds)

    graph = None

    worldActor ! ResetWorld
    worldActor ! ActMsg(1)

    new Thread(new Runnable {
      override def run(): Unit = {
        val future = graphActor ? AskForGraphMsg
        val result = Await.result(future, timeout.duration)
          .asInstanceOf[SendGraphMsg].graph

        graph = Option(new GraphAdapter[Vector2] {
          override def getNeighbors(vertex: Vector2): util.Iterator[Vector2] = {
            new NeighborIndex[Vector2, DefaultEdge](result)
              .neighborsOf(vertex).iterator
          }

          override def getVertices: util.Iterator[Vector2] = result.vertexSet.iterator
        })
        cacheMap()
      }
    }).start()
  }

  override def init(owner: Testbed): Unit = {
    submodules :+= ActorSystemModule()
    super.init(owner)
  }

  override def initGui(pane: VisWindow): Unit = {

    pane.setResizable(true)

    val initBtn = new VisTextButton("Random map", "blue")
    initBtn.addListener(new ClickListener {
      override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
        super.clicked(event, x, y)
        mapActor ! GenerateMapMsg()
      }
    })

    val selectBox = new VisSelectBox[String]()
    selectBox.setItems(Gdx.files.internal("maps").list()
      .map(f => f.name())
      .filter(f => f.endsWith(".txt"))// && !f.equals("outputGraphActor.txt"))
      .toList.asGdxArray)

    val graphBtn = new VisTextButton("Generate Graph from File", "blue")
    graphBtn.addListener(new ClickListener {
      override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
        super.clicked(event, x, y)
        val f = Gdx.files.internal("maps/"+selectBox.getSelected)
        if (!f.name().equals("map.txt")) f.copyTo(Gdx.files.local(mapFilePath))
        askForGraph()
      }
    })

    pane.add[VisTextButton](initBtn).width(100).expandX().row()
    pane.add[VisSelectBox[String]](selectBox).fillX().expandX().pad(10, 0, 4, 0).row()
    pane.add[VisTextButton](graphBtn).expandX().padTop(10).row()
    pane.add().fillY().expandY()
  }

  override def setup(log: (String) => Unit): Unit = {
    super.setup(log)
    log("Storing actor refs..")
    worldActor = SystemManager.getInstance().getLocalActor("worldActor")
    mapActor = SystemManager.getInstance().getLocalActor("mapActor")
    graphActor = SystemManager.getInstance().getLocalActor("graphActor")

    mapActor ! MapSettingsMsg(60, 60)
    graphActor ! MapSettingsMsg(60, 60)

  }

  override def render(shapeRenderer: ShapeRenderer): Unit = {
    super.render(shapeRenderer)

    graph.foreach(g =>
      renderer.renderGraph(shapeRenderer, g, graphRenderingConfig))

    renderer.renderMap(shapeRenderer, worldMap)
  }

  override def cleanup(): Unit = super.cleanup()

  override def description: String =
    """System init test:
      |- Actors deployment
      |- Map generation
      |- Graph generation
    """.stripMargin

}
