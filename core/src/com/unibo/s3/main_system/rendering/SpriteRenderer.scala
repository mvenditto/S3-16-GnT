package com.unibo.s3.main_system.rendering

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode
import com.badlogic.gdx.graphics.g2d._
import com.badlogic.gdx.graphics.{Camera, Color, Texture}
import com.badlogic.gdx.math.{MathUtils, Vector2}
import com.badlogic.gdx.utils.Disposable
import com.unibo.s3.main_system.characters.BaseCharacter
import com.unibo.s3.main_system.characters.steer.MovableEntity
import com.unibo.s3.main_system.game.{GameSettings, Wall}
import com.unibo.s3.main_system.util.ScaleUtils

import scala.collection.mutable

/**
  * This class manages the animation/rendering of characters sprites.
  *
  * @author mvenditto
  */
class SpriteRenderer extends Disposable {
  import SpriteRenderer._

  var batch: SpriteBatch = _
  private val animationsCache = mutable.Map[String, Animation[TextureRegion]]()
  private var guardAtlas: TextureAtlas = _
  private var stateTime = 0f
  private var floorTexture: TextureRegion = _
  private var font: BitmapFont = _
  private var debugDraw = false
  private var exitTexture: Texture = _
  private var exitSprite: TextureRegion = _

  private def getAndCacheAnimation(s: String): Animation[TextureRegion] = {
    animationsCache.getOrElseUpdate(s,
      new Animation[TextureRegion](freq, guardAtlas.findRegions(s), PlayMode.LOOP))
  }

  private def getCurrentAnimation(
    c: MovableEntity[Vector2]): (Animation[TextureRegion], Animation[TextureRegion]) = {

    val v = c.getLinearVelocity

    val a = v.len2 match {
      case x if x < idleThreshold => (guardIdle, guardFeetIdle)
      case x if x < runThreshold => (guardMove, guardFeetWalk)
      case x if x > runThreshold => (guardMove, guardFeetRun)
    }

    val body = getAndCacheAnimation(a._1)
    val feet = getAndCacheAnimation(a._2)

    (body, feet)
  }

  private def drawDebugInfo(c: MovableEntity[Vector2]) = {
    val s = ScaleUtils.getPixelsPerMeter
    c match {
      case bc: BaseCharacter =>
        font.draw(batch, "id: " + bc.getId.toString, c.getPosition.x * s, c.getPosition.y * s)}
  }

  def init(): Unit = {
    batch = new SpriteBatch()
    font = new BitmapFont()
    font.setColor(Color.YELLOW)
    font.getData.setScale(2f, 2f)
    guardAtlas = new TextureAtlas(guardAtlasFile)
    exitTexture = new Texture(Gdx.files.internal(exitSpriteFile))
    exitSprite = new TextureRegion(exitTexture, 0, 0, exitTexture.getWidth, exitTexture.getHeight)
  }

  def setDebugDraw(flag: Boolean): Unit = debugDraw = flag

  def update(dt: Float): Unit = stateTime += dt

  def render(c: MovableEntity[Vector2], cam: Camera): Unit = {
    val a = getCurrentAnimation(c)
    val body = a._1
    val feet = a._2

    batch.setProjectionMatrix(cam.combined)
    batch.begin()
    renderKeyFrame(c, feet)
    renderKeyFrame(c, body)
    if (debugDraw) drawDebugInfo(c)
    batch.end()
  }

  def renderFloor(w: Float, h: Float, cam: Camera): Unit = {
    if (floorTexture == null) {
      val f = new Texture(Gdx.files.internal(defaultFloor))
      val iw = f.getWidth
      val ih = f.getHeight
      val s = ScaleUtils.getPixelsPerMeter
      f.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat)
      floorTexture = new TextureRegion(f)
      floorTexture.setRegion(0, 0, (w * s) / iw, (h * s) / ih)
      f.dispose()
    }

    batch.setProjectionMatrix(cam.combined)
    batch.begin()
    batch.draw(floorTexture, 0, 0)
    batch.end()

  }


  def renderExits(exits: Iterable[Vector2], cam: Camera): Unit = {
    val s = ScaleUtils.getPixelsPerMeter
    val hw = exitSprite.getRegionWidth / 2
    val wt = Wall.WALL_THICKNESS * s
    batch.setProjectionMatrix(cam.combined)
    batch.begin()

    exits.foreach(e => batch.draw(exitSprite, (e.x * s) - wt / 2, (e.y * s) - wt / 2, wt, wt))
    batch.end()
  }

  def updateAndRender(dt: Float, c: MovableEntity[Vector2], cam: Camera): Unit = {
    render(c, cam)
    update(dt)
  }

  private def renderKeyFrame(c: MovableEntity[Vector2], a: Animation[TextureRegion]): Unit = {
    val tr = a.getKeyFrame(stateTime)
    val width = tr.getRegionWidth
    val height = tr.getRegionHeight
    val ox = width / 2f
    val oy = height / 2f

    val pos = c.getPosition
    val s = ScaleUtils.getPixelsPerMeter
    val rotation = c.getOrientation * MathUtils.radDeg

    /*
    - width and height are swapped on purpose, otherwise sprite has wrong aspect ratio.
    - last boolean parameter of batch.draw, rotates texture vertices by 90 degrees counter-clockwise.
    */

    batch.draw(tr, (pos.x * s) - ox, (pos.y * s) - oy, ox, oy,
      height, width, guardScale, guardScale, rotation, false)
  }

  override def dispose(): Unit = {
    batch.dispose()
    guardAtlas.dispose()
    font.dispose()
    exitTexture.dispose()
  }
}

object SpriteRenderer {

  private val guardAtlasFile = "sprites/guard.atlas"
  private val guardIdle  = "guard-idle_flashlight"
  private val guardMove  = "guard-move_flashlight"
  private val guardFeetIdle = "guard-idle"
  private val guardFeetWalk = "guard-walk"
  private val guardFeetRun = "guard-run"
  private val defaultFloor = "sprites/floor3.png"
  private val exitSpriteFile = "sprites/exit_icon.png"
  private val guardScale = 1.0f / 3.0f
  private val runThreshold = 1.0f
  private val idleThreshold = 0.01f
  private val freq = 0.066f //15 fps

  def apply(): SpriteRenderer = new SpriteRenderer()

}
