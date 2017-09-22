package com.unibo.s3.main_system.rendering

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.math.{MathUtils, Polygon, Rectangle, Vector2}
import com.unibo.s3.main_system.characters.BaseCharacter
import com.unibo.s3.main_system.characters.Guard
import com.unibo.s3.main_system.characters.Thief
import com.unibo.s3.main_system.characters.steer.MovableEntity
import com.unibo.s3.main_system.graph.GraphAdapter
import com.unibo.s3.main_system.util.ScaleUtils
import com.unibo.s3.main_system.util.ScaleUtils.getPixelsPerMeter

import scala.collection.mutable

/**
  * An implementation of [[GeometryRenderer]] trait.
  *
  * @author mvenditto
  * */
class GeometryRendererImpl extends GeometryRenderer[Vector2]{

  private def characterRenderRays(character: MovableEntity[Vector2], shapeRenderer: ShapeRenderer): Unit = {
    val s = ScaleUtils.getPixelsPerMeter
    shapeRenderer.set(ShapeType.Line)
    shapeRenderer.setColor(Color.RED)

    val rays = character.getRays
    val tmp = new Vector2()
    val tmp2 = new Vector2()
    for (ray <- rays) {
      tmp.set(ray.start).scl(s.toFloat)
      tmp2.set(ray.end).scl(s.toFloat)
      shapeRenderer.line(tmp, tmp2)
    }
  }


  private def characterRenderGraphInfo(bc: BaseCharacter,  shapeRenderer: ShapeRenderer): Unit = {
    val s = ScaleUtils.getPixelsPerMeter

    val renderNodeInfo = bc match {
      case t:Thief if t.gotCaughtByGuard || t.hasReachedExit => false
      case _ => true
    }

    val ss = s / 2.5f
    val pos = bc.getPosition
    val xs = pos.x * s
    val ys = pos.y * s

    if (renderNodeInfo) {
      shapeRenderer.set(ShapeType.Filled)
      shapeRenderer.setColor(Color.SKY)
      val targ = bc.getCurrentDestination
      val tx = targ.x * s
      val ty = targ.y * s
      shapeRenderer.rectLine(xs, ys, tx, ty, ss)
      shapeRenderer.circle(tx, ty, ss)
      shapeRenderer.setColor(Color.FIREBRICK)
      bc.getCurrentNode.foreach(f => {
        val targ = f
        val tx = targ.x * s
        val ty = targ.y * s
        shapeRenderer.rectLine(xs, ys, tx, ty, ss)
        shapeRenderer.circle(tx, ty, ss)
      })
    }
    shapeRenderer.setColor(Color.SKY)
    bc.getInformation.foreach(vn =>
      shapeRenderer.circle(vn.x * s, vn.y * s, ss))
  }

  private def characterRenderTarget(bc: BaseCharacter, shapeRenderer: ShapeRenderer): Unit = {
    bc match {
      case _: Guard => shapeRenderer.setColor(Color.VIOLET)
      case _: Thief => shapeRenderer.setColor(Color.ORANGE)
      case _ =>
    }

    val s = ScaleUtils.getPixelsPerMeter
    val pos = bc.getPosition
    val xs = pos.x * s
    val ys = pos.y * s
    val ss = s / 2.5f

    bc.getTarget.foreach(f => {
      val targ = f.get
      val tx = targ.getPosition.x * s
      val ty = targ.getPosition.y * s
      shapeRenderer.rectLine(xs, ys, tx, ty, ss)
    })
  }

  private def characterRenderConeOfView(bc: BaseCharacter, shapeRenderer: ShapeRenderer): Unit = {
    val s = ScaleUtils.getPixelsPerMeter
    val fovAngle = bc.getFieldOfView.getAngle * MathUtils.radDeg
    val fovRadius = bc.getFieldOfView.getRadius
    val pos = bc.getPosition
    val tint = bc match {
      case _: Guard => Color.CYAN
      case _: Thief => Color.RED
    }
    val xs = pos.x * s
    val ys = pos.y * s
    shapeRenderer.setColor(tint)
    shapeRenderer.arc(xs, ys, fovRadius * s,
      bc.getOrientation * MathUtils.radiansToDegrees - fovAngle / 2f + 90f,
      fovAngle)
  }

  override def renderCharacter(shapeRenderer: ShapeRenderer, character: MovableEntity[Vector2]): Unit = {
    val scale = getPixelsPerMeter
    val position = character.getPosition
    val size = 0.45f
    val backupColor = shapeRenderer.getColor
    val triangle = new Polygon(Array(
              scale * (position.x - size), position.y * scale,
              (position.x + size) * scale, position.y * scale,
              position.x * scale, (position.y + size) * scale))

    triangle.setOrigin(position.x * scale, position.y * scale)
    triangle.rotate(character.getOrientation * MathUtils.radiansToDegrees)

    val v = triangle.getTransformedVertices
    val v1 = new Vector2(v(0), v(1))
    val v2 = new Vector2(v(2), v(3))
    val v3 = new Vector2(v(4), v(5))

    val t = shapeRenderer.getCurrentType
    shapeRenderer.setColor(character.getColor)
    shapeRenderer.setAutoShapeType(true)
    shapeRenderer.set(ShapeRenderer.ShapeType.Filled)
    shapeRenderer.rectLine(v1, v2, 4)
    shapeRenderer.rectLine(v2, v3, 4)
    shapeRenderer.rectLine(v3, v1, 4)
    shapeRenderer.set(ShapeRenderer.ShapeType.Line)
    shapeRenderer.setColor(backupColor)
    shapeRenderer.set(t)
  }

  override def renderCharacterDebugInfo(shapeRenderer: ShapeRenderer, character: MovableEntity[Vector2]): Unit = {
    val backupColor = shapeRenderer.getColor
    val t = shapeRenderer.getCurrentType
    characterRenderRays(character, shapeRenderer)
    character match {
      case bc: BaseCharacter =>
        shapeRenderer.setAutoShapeType(true)
        characterRenderConeOfView(bc, shapeRenderer)
        characterRenderGraphInfo(bc, shapeRenderer)
        characterRenderTarget(bc, shapeRenderer)
      case _ =>
    }
    shapeRenderer.set(t)
    shapeRenderer.setColor(backupColor)
  }

  override def renderGraph(shapeRenderer: ShapeRenderer,
    graph: GraphAdapter[Vector2], config: GraphRenderingConfig) {

    val edgeColor = config.edgeColor
    val vertexColor = config.vertexColor
    val scale = getPixelsPerMeter
    val vertexRadiusPixel = config.vertexRadius * scale
    val backupColor = shapeRenderer.getColor
    val t = shapeRenderer.getCurrentType

    shapeRenderer.setAutoShapeType(true)
    graph.getVertices.foreach(v => {
      val scaledX = v.x * scale
      val scaledY = v.y * scale
      shapeRenderer.set(ShapeType.Line)
      shapeRenderer.setColor(vertexColor)
      shapeRenderer.circle(scaledX, scaledY, vertexRadiusPixel)
      shapeRenderer.setColor(edgeColor)
      graph.getNeighbors(v).foreach(n => {
        shapeRenderer.set(ShapeType.Filled)
        shapeRenderer.rectLine(scaledX, scaledY, n.x * scale, n.y * scale, 4f)
      })
    })
    shapeRenderer.setColor(backupColor)
    shapeRenderer.set(t)
  }

  override def renderMap(shapeRenderer: ShapeRenderer , map: Iterable[Rectangle]): Unit = {

    val c = shapeRenderer.getColor
    val t = shapeRenderer.getCurrentType
    val s = ScaleUtils.getPixelsPerMeter

    map.foreach(r => {
      val center = new Vector2()
      r.getCenter(center)
      center.sub(r.getWidth, r.getHeight)
      shapeRenderer.setAutoShapeType(true)
      shapeRenderer.set(ShapeType.Filled)
      shapeRenderer.setColor(Color.GRAY)
      shapeRenderer.rect(center.x * s, center.y * s, r.getWidth * s, r.getHeight * s)
      shapeRenderer.set(ShapeType.Line)
      shapeRenderer.setColor(Color.DARK_GRAY)
      shapeRenderer.rect(center.x * s, center.y * s, r.getWidth * s, r.getHeight * s)
      shapeRenderer.set(t)
      shapeRenderer.setColor(c)
    })
  }
}

object GeometryRendererImpl {
  def apply(): GeometryRendererImpl = new GeometryRendererImpl()
}
