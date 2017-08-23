package com.unibo.s3.testbed.testbed_modules.future

import akka.actor.{ActorRef, Props}
import akka.pattern.Patterns
import akka.util.Timeout
import com.badlogic.gdx.graphics.g2d.{BitmapFont, SpriteBatch}
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.{Color, Texture}
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.{Body, PolygonShape, World}
import com.badlogic.gdx.{Gdx, Input, InputMultiplexer}
import com.kotcrab.vis.ui.widget.VisWindow
import com.typesafe.config.ConfigFactory
import com.unibo.s3.InputProcessorAdapter
import com.unibo.s3.main_system.communication.SystemManager
import com.unibo.s3.main_system.rendering.ScaleUtils.{getMetersPerPixel, getPixelsPerMeter, metersToPixels, pixelsToMeters}
import com.unibo.s3.main_system.world.actors._
import com.unibo.s3.testbed.Testbed

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class ScalaBox2dModule extends SampleWithGui with InputProcessorAdapter {

  private var world: World = _
  private var worldActor: ActorRef = _

  private var bodyEditorEnabled = false
  private var topLeft: Option[Vector2] = None
  private var delta: Option[Vector2] = None

  private var mouseDragged = false

  private var textBatch: SpriteBatch = _
  private var font: BitmapFont = _

  override def update(dt: Float): Unit = {
    super.update(dt)
    worldActor ! Act(dt)
  }

  override def init(owner: Testbed): Unit = {
    super.init(owner)
    textBatch = new SpriteBatch()
    world = new World(new Vector2(0, 0), true)
    font = new BitmapFont
    font.getRegion.getTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
    font.getData.setScale(1.5f)

    val conf = "{\"akka\":{\"actor\":{\"provider\":\"akka.remote.RemoteActorRefProvider\"}," + "\"loglevel\":\"INFO\",\"remote\":{\"enabled-transports\":[\"akka.remote.netty.tcp\"]" + ",\"log-received-messages\":\"on\",\"log-sent-messages\":\"on\"" + ",\"netty\":{\"tcp\":{\"hostname\":\"" + "127.0.0.1" + "\",\"port\":5050}}}}}"
    val customConfig = ConfigFactory.parseString(conf)
    SystemManager.getInstance.createSystem("b2d", customConfig)
    SystemManager.getInstance.createActor(Props.create(classOf[WorldActor], world), "world")
    worldActor = SystemManager.getInstance.getLocalActor("world")
  }

  override def cleanup(): Unit = {
    super.cleanup()
    world.dispose()
    SystemManager.getInstance().shutdownSystem()
  }

  override def attachInputProcessors(inputMultiplexer: InputMultiplexer): Unit = {
    super.attachInputProcessors(inputMultiplexer)
    inputMultiplexer.addProcessor(this)
  }

  override def initGui(menuTable: VisWindow): Unit = {}

  override def description: String = "Box2d world module."

  private def resetWorld() = worldActor ! ResetWorld

  override def keyUp(keycode: Int): Boolean = {

    if (keycode == Input.Keys.G) {
      bodyEditorEnabled = !bodyEditorEnabled
    }
    if (keycode == Input.Keys.U) {
      enableGui(!guiEnabled)
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

  override def render(shapeRenderer: ShapeRenderer): Unit = {
    super.render(shapeRenderer)

    val timeout = new Timeout(Duration.create(5, "seconds"))
    val future = Patterns.ask(worldActor, new GetAllBodies, timeout)
    try {
      val bodies = Await.result(future, timeout.duration).asInstanceOf[com.badlogic.gdx.utils.Array[Body]]
      for (i <- 0 until bodies.size) {
        renderBox(shapeRenderer, bodies.get(i), false)
      }
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }

    if (bodyEditorEnabled && topLeft.isDefined && delta.isDefined) {
      shapeRenderer.setColor(Color.GREEN)
      //shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
      val tmp = topLeft.get.cpy().scl(getPixelsPerMeter)
      val tmp2 = delta.get.cpy().scl(getPixelsPerMeter)
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
      vertices(j) = metersToPixels(worldVertex.x)
      vertices(j + 1) = metersToPixels(worldVertex.y)
      j += 2
    }

    val c = renderer.getColor
    if (underMouse) renderer.setColor(Color.CYAN)
    else renderer.setColor(Color.GRAY)

    val v0 = new Vector2(vertices(0), vertices(1))
    val t = b.getUserData.asInstanceOf[String].split(":")
    val s = getPixelsPerMeter

    renderer.setAutoShapeType(true)
    renderer.set(ShapeRenderer.ShapeType.Filled)
    renderer.rect(v0.x, v0.y, t(0).toFloat * s, t(1).toFloat * s)
    renderer.set(ShapeRenderer.ShapeType.Line)
    renderer.setColor(Color.BLACK)
    renderer.rect(v0.x, v0.y, t(0).toFloat * s, t(1).toFloat * s)
    renderer.setAutoShapeType(false)
    renderer.setColor(c)
  }

}
