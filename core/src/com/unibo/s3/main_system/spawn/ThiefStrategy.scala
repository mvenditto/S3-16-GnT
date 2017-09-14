package com.unibo.s3.main_system.spawn

import com.badlogic.gdx.math.Vector2
import com.unibo.s3.main_system.map.AbstractMapGenerator

import scala.util.Random

class ThiefStrategy extends SpawnStrategy {

  private[this] var width_shift: Int = _
  private[this] var height_shift: Int = _

  override def generateSpawnQuadrant(map: Array[Array[Int]]): Vector2 = {
    this.width_shift = map.length / 5
    this.height_shift = map(0).length / 5
    val x = Random.nextInt(width_shift) + (map.length / 2) + AbstractMapGenerator.BASE_UNIT
    val y = Random.nextInt(height_shift) +  (map(0).length / 2) + AbstractMapGenerator.BASE_UNIT
    new Vector2(x, y)
  }

}

object ThiefStrategy {
  def apply(): ThiefStrategy = new ThiefStrategy()
}