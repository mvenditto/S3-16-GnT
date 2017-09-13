package com.unibo.s3.main_system.spawn

import com.badlogic.gdx.math.Vector2

import scala.util.Random

class ThiefStrategy extends SpawnStrategy {

  private[this] var width_shift: Int = _
  private[this] var height_shift: Int = _

  override def generateSpawnQuadrant(map: Array[Array[Int]]): Vector2 = {
    this.width_shift = map.length / 3
    this.height_shift = map(0).length / 3
    val x = Random.nextInt(map.length / 2) + this.width_shift
    val y = Random.nextInt(map(0).length / 2) + this.height_shift
    new Vector2(x, y)
  }

}

object ThiefStrategy {
  def apply(): ThiefStrategy = new ThiefStrategy()
}