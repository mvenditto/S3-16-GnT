package com.unibo.s3.main_system.util

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.physics.box2d.Body
import Box2dImplicits._

/**
  * An utility class containing map parsing methods.
  *
  * @author mvenditto
  */
object GntUtils {

  private[this] val newline = "\n"
  private[this] val separator = ":"

  type MapEntry = (Array[Float], Option[String])

  def parseMapEntry(entry: String): MapEntry = {
    val e = entry.split(separator)
    (e.slice(0, 4).map(v => v.toFloat),
      if (e.indices.contains(4)) Option(e(4)) else None)
  }

  def parseMap(map: String): Iterable[MapEntry] =
    map.split(newline).map(b => parseMapEntry(b))

  def parseMapToRectangles(map: String): Iterable[Rectangle] =
    parseMap(map)
      .map(b => b._1)
      .map(b => new Rectangle(b(0), b(1), b(2), b(3)))

  def parseBodiesToMap(bodies: Iterable[Body]): Iterable[Rectangle] = {
    bodies.map(b => {
      val p = b.getWorldCenter
      val s = b.size2
      new Rectangle(p.x, p.y, s.x, s.y)
    })
  }

  def parseMapToRectangles(map: FileHandle): Iterable[Rectangle] =
    parseMapToRectangles(map.readString())
}
