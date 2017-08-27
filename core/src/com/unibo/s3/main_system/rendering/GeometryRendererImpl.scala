package com.unibo.s3.main_system.rendering

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.math.{MathUtils, Polygon, Rectangle, Vector2}
import com.unibo.s3.main_system.characters.steer.MovableEntity
import com.unibo.s3.main_system.graph.GraphAdapter
import com.unibo.s3.main_system.util.ScaleUtils
import com.unibo.s3.main_system.util.ScaleUtils.{getPixelsPerMeter, metersToPixels}

import scala.collection.JavaConversions._

/**
 *
 * @author mvenditto
 * */
class GeometryRendererImpl extends GeometryRenderer[Vector2] {

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
    shapeRenderer.setColor(Color.RED)

    val rays = character.getRays
    val tmp = new Vector2()
    val tmp2 = new Vector2()
    for (ray <- rays) {
      tmp.set(ray.start)
      tmp.x = metersToPixels(tmp.x).toFloat
      tmp.y = metersToPixels(tmp.y).toFloat
      tmp2.set(ray.end)
      tmp2.x = metersToPixels(tmp2.x).toFloat
      tmp2.y = metersToPixels(tmp2.y).toFloat
      shapeRenderer.line(tmp, tmp2)
    }
    shapeRenderer.setColor(backupColor)
  }

  override def renderGraph(shapeRenderer: ShapeRenderer,
    graph: GraphAdapter[Vector2], config: GraphRenderingConfig ) {

    val edgeColor = config.edgeColor
    val vertexColor = config.vertexColor
    val scale = getPixelsPerMeter
    val vertexRadiusPixel = config.vertexRadius * scale
    val backupColor = shapeRenderer.getColor

    graph.getVertices.toIterable.foreach(v => {
      val scaledX = v.x * scale
      val scaledY = v.y * scale
      shapeRenderer.setColor(vertexColor)
      shapeRenderer.circle(scaledX, scaledY, vertexRadiusPixel)
      shapeRenderer.setColor(edgeColor)
      graph.getNeighbors(v).toIterable.foreach(n =>
        shapeRenderer.line(scaledX, scaledY, n.x * scale, n.y * scale))
    })
    shapeRenderer.setColor(backupColor)
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
