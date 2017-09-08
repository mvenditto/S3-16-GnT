package com.unibo.s3.testbed.modules

import akka.actor.{ActorRef, Props}
import akka.pattern.Patterns
import akka.util.Timeout
import com.badlogic.gdx.graphics.g2d.{BitmapFont, SpriteBatch}
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.{Color, Texture}
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.{Body, CircleShape, PolygonShape, World}
import com.badlogic.gdx.{Gdx, Input, InputMultiplexer}
import com.kotcrab.vis.ui.widget.VisWindow
import com.typesafe.config.ConfigFactory
import com.unibo.s3.InputProcessorAdapter
import com.unibo.s3.main_system.communication.Messages.{ActMsg, MapElementMsg}
import com.unibo.s3.main_system.communication.{GeneralActors, SystemManager}
import com.unibo.s3.main_system.util.GdxImplicits._
import com.unibo.s3.main_system.util.ScaleUtils.{getMetersPerPixel, getPixelsPerMeter, metersToPixels, pixelsToMeters}
import com.unibo.s3.main_system.world.actors._
import com.unibo.s3.testbed.model.{BaseTestbedModule, Testbed}

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import com.unibo.s3.main_system.util.Box2dImplicits._
import com.unibo.s3.main_system.world.{BodyData, Exit}
import org.junit.internal.runners.statements.Fail

import scala.util.{Success, Try}

class Box2dModule extends BaseTestbedModule with InputProcessorAdapter {

  private var world: World = _
  protected var worldActor: ActorRef = _

  private var bodyEditorEnabled = false
  private var topLeft: Option[Vector2] = None
  private var delta: Option[Vector2] = None

  private var mouseDragged = false

  private var textBatch: SpriteBatch = _
  private var font: BitmapFont = _

  def getWorld: World = world

  def getWorldActorRef: ActorRef = worldActor

  override def update(dt: Float): Unit = {
    super.update(dt)
    worldActor ! ActMsg(dt)
  }

  override def init(owner: Testbed): Unit = {
    super.init(owner)
    textBatch = new SpriteBatch()
    font = new BitmapFont
    font.getRegion.getTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
    font.getData.setScale(1.5f)
  }

  override def setup(log: String => Unit): Unit = {
    log("Initializing world")
    world = new World(new Vector2(0, 0), true)
    log("Starting actor system")
    SystemManager.createSystem("System", null)
    SystemManager.createGeneralActor(Props.create(classOf[WorldActor], world), GeneralActors.WORLD_ACTOR)
    worldActor = SystemManager.getLocalGeneralActor(GeneralActors.WORLD_ACTOR)
  }

  override def cleanup(): Unit = {
    super.cleanup()
    world.dispose()
    SystemManager.shutdownSystem()
  }

  override def attachInputProcessors(inputMultiplexer: InputMultiplexer): Unit = {
    super.attachInputProcessors(inputMultiplexer)
    inputMultiplexer.addProcessor(this)
  }

  override def initGui(menuTable: VisWindow): Unit = {}

  def loadWorld(name: String): Unit = {
    val w = Gdx.files.internal("maps/" + name)
    w.readString().split("\n").foreach(l => worldActor ! MapElementMsg(l))
  }

  def saveWorld(name: String): Unit = {
    val w = Gdx.files.local("maps/" + name)
    val s = StringBuilder.newBuilder
    getAllBodies().asScalaIterable.foreach(b => {
      val c = b.getWorldCenter
      val size = b.size2
      s append c.x
      s append ":"
      s append c.y
      s append ":"
      s append size.x+":"+size.y
      s append "\n"
    })
    w.writeString(s.toString(), false)
  }

  def resetWorld() = worldActor ! ResetWorld

  override def keyUp(keycode: Int): Boolean = {
    if (keycode == Input.Keys.G) {
      bodyEditorEnabled = !bodyEditorEnabled
    }
    false
  }

  override def touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean = {
    var click = new Vector2(screenX, screenY)
    click = owner.screenToWorld(click).scl(getMetersPerPixel)
    topLeft = Option(click)
    false
  }

  override def touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean = {
  if (bodyEditorEnabled) {
    if (mouseDragged) {
      topLeft.foreach(tl => {
        delta.foreach(d => {
          val center = tl.cpy.add(d).scl(0.5f)
          worldActor.tell(CreateBox(center, new Vector2(Math.abs(tl.x - d.x),
            Math.abs(tl.y - d.y))), null)
        })
      })
    }
    if (button == 1) {
      var click = new Vector2(Gdx.input.getX, Gdx.input.getY)
      click = owner.screenToWorld(click).scl(getMetersPerPixel)
      worldActor.tell(DeleteBodyAt(click.x, click.y), null)
    }
  }
  topLeft = None
  delta = None
  mouseDragged = false
  false
}

  override def touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean = {
    var click  = new Vector2(screenX, screenY)
    click = owner.screenToWorld(click)
    topLeft.foreach(_ => delta = Option(click.cpy.scl(getMetersPerPixel)))
    mouseDragged = true
    false
  }

  private def getAllBodies(): com.badlogic.gdx.utils.Array[Body] = {
    val timeout = new Timeout(Duration.create(5, "seconds"))
    val future = Patterns.ask(worldActor, GetAllBodies(), timeout)
    try {
      Await.result(future, timeout.duration).asInstanceOf[com.badlogic.gdx.utils.Array[Body]]
    } catch {
      case e: Exception =>
        e.printStackTrace()
        new com.badlogic.gdx.utils.Array[Body]()
    }
  }

  override def render(shapeRenderer: ShapeRenderer): Unit = {
    super.render(shapeRenderer)

    val bodies = getAllBodies()
    for (i <- 0 until bodies.size) {
      renderBox(shapeRenderer, bodies.get(i), false)
    }

    if (bodyEditorEnabled && topLeft.isDefined && delta.isDefined) {
      shapeRenderer.setColor(Color.GREEN)
      //shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
      val tmp = topLeft.get.cpy().scl(getPixelsPerMeter.toFloat)
      val tmp2 = delta.get.cpy().scl(getPixelsPerMeter.toFloat)
      shapeRenderer.rect(tmp.x, tmp.y, tmp2.x - tmp.x, tmp2.y - tmp.y)

      val cx = tmp.x + ((tmp2.x - tmp.x) / 2)
      val cy = tmp.y + ((tmp2.y - tmp.y) / 2)
      shapeRenderer.circle(cx, cy, 10)

      val wTextPos = owner.worldToScreen(new Vector2(cx, (cy + (tmp2.y - tmp.y) / 2) - 30))
      val hTextPos = owner.worldToScreen(new Vector2((cx + (tmp2.x - tmp.x) / 2) + 30, cy))

      if (textBatch != null) {
        textBatch.begin()
        font.draw(textBatch, "" + pixelsToMeters((tmp2.x - tmp.x).toInt).round, wTextPos.x, wTextPos.y)
        font.draw(textBatch, "" + Math.abs(pixelsToMeters((tmp2.y - tmp.y).toInt).round), hTextPos.x, hTextPos.y)
        textBatch.end()
      }
    }
  }

  private def renderBox (renderer: ShapeRenderer, b: Body, underMouse: Boolean): Unit = {
    val poly = b.getFixtureList.get(0).getShape.asInstanceOf[PolygonShape]
    val vertices = new Array[Float](poly.getVertexCount * 2)
    var j = 0

    for(i <- 0 until poly.getVertexCount) {
      val v = new Vector2
      poly.getVertex(i, v)
      val worldVertex = b.getWorldPoint(v)
      vertices(j) = metersToPixels(worldVertex.x).toFloat
      vertices(j + 1) = metersToPixels(worldVertex.y).toFloat
      j += 2
    }

    val c = renderer.getColor
    if (underMouse) renderer.setColor(Color.CYAN)
    else renderer.setColor(Color.GRAY)

    val v0 = new Vector2(vertices(0), vertices(1))
    val t = b.size2
    val s = getPixelsPerMeter

    renderer.setAutoShapeType(true)
    renderer.set(ShapeRenderer.ShapeType.Filled)
    renderer.rect(v0.x, v0.y, t.x * s, t.y * s)
    renderer.set(ShapeRenderer.ShapeType.Line)
    renderer.setColor(Color.BLACK)
    renderer.rect(v0.x, v0.y, t.x * s, t.y * s)
    renderer.setAutoShapeType(false)
    renderer.setColor(c)
  }

}
