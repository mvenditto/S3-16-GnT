package com.unibo.s3

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.unibo.s3.main_system.AbstractMainApplication
import com.unibo.s3.main_system.modules._
import com.unibo.s3.main_system.util.ScaleUtils._

class Main extends AbstractMainApplication {
  private var modules = List[BasicModule]()
  private var inputMultiplexer: InputMultiplexer = _

  override def create(): Unit = {
    super.create()
    inputMultiplexer = new InputMultiplexer
    addModules()

    modules.foreach(m => {
      m.init(this)
      m.attachInputProcessors(inputMultiplexer)
    })

    inputMultiplexer.addProcessor(this)
    Gdx.input.setInputProcessor(inputMultiplexer)
  }

  private def addModules() = {

    val b = new BootstrapModule({
      case BootstrapOk(actors) => println(actors)
      case BootstrapFailed(err) => println(err)
    })
    b.enable(false)
    modules :+= b

    val cm = new MenuModule
    cm.enable(true)
    modules :+= cm
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

  override protected def doRender(): Unit = {
    renderAxis(shapeRenderer)
    val enabledModules = modules.filter(m => m.isEnabled)
    enabledModules.foreach(m => m.render(shapeRenderer))
    enabledModules.foreach(m => m.renderGui())
  }

  override protected def doUpdate(delta: Float): Unit =
    modules.filter(m => m.isEnabled).foreach(m => m.update(delta))

  override def resize(newWidth: Int, newHeight: Int): Unit = {
    super.resize(newWidth, newHeight)
    modules.foreach(m => m.resize(newWidth, newHeight))
  }
}