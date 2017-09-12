package com.unibo.s3.main_system.rendering

import com.badlogic.gdx.graphics.{Camera, Color}
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode
import com.badlogic.gdx.graphics.g2d._
import com.badlogic.gdx.math.{MathUtils, Vector2}
import com.unibo.s3.main_system.characters.steer.MovableEntity
import com.unibo.s3.main_system.util.{GraphicsUtils, ScaleUtils}

import scala.collection.mutable

class SpriteRenderer {
  import SpriteRenderer._

  private var batch: SpriteBatch = _
  private val guardAnimation = mutable.Map[String, Animation[TextureRegion]]()

  private var guardAtlas: TextureAtlas = _

  private val runThreshold = 1.0f
  private val idleThreshold = 0.01f
  private var stateTime = 0f
  private val freq = 0.066f //15 fps
  private var tmpSprite: Sprite = _

  def init(): Unit = {
    batch = new SpriteBatch()
    guardAtlas = new TextureAtlas(guardAtlasFile)
    tmpSprite = new Sprite(GraphicsUtils.textureFromColor(8,8,Color.WHITE))
  }

  def update(dt: Float): Unit = stateTime += dt

  def render(c: MovableEntity[Vector2], cam: Camera): Unit = {
    val a = getCurrentAnimation(c)
    val body = a._1
    val feet = a._2

    batch.setProjectionMatrix(cam.combined)
    batch.begin()
    getSpriteFor(c, feet).draw(batch)
    getSpriteFor(c, body).draw(batch)
    batch.end()
  }

  def updateAndRender(dt: Float, c: MovableEntity[Vector2], cam: Camera): Unit = {
    render(c, cam)
    update(dt)
  }

  private def getSpriteFor(c: MovableEntity[Vector2], a: Animation[TextureRegion]): Sprite = {
    val pos = c.getPosition
    val s = ScaleUtils.getPixelsPerMeter
    val rotation = (c.getOrientation * MathUtils.radDeg) + 90

    val sp = new Sprite(a.getKeyFrame(stateTime))
    sp.setScale(guardScale)
    sp.setPosition(
      (pos.x * s) - sp.getWidth / 2, (pos.y * s) - sp.getHeight / 2)
    sp.setRotation(rotation)
    sp
  }

  private def getAndCacheAnimation(s: String): Animation[TextureRegion] = {
    guardAnimation.getOrElseUpdate(s,
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
}

object SpriteRenderer {

  private val guardAtlasFile = "sprites/guard.atlas"
  private val guardIdle  = "guard-idle_flashlight"
  private val guardMove  = "guard-move_flashlight"
  private val guardFeetIdle = "guard-idle"
  private val guardFeetWalk = "guard-walk"
  private val guardFeetRun = "guard-run"
  private val guardScale = 1.0f / 3.0f

  def apply(): SpriteRenderer = new SpriteRenderer()

}
