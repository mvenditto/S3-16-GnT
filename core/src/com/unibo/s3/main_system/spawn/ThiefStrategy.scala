package com.unibo.s3.main_system.spawn

import com.badlogic.gdx.math.Vector2
import com.unibo.s3.main_system.game.{GameSettings, Wall}

import scala.util.Random

class ThiefStrategy extends SpawnStrategy {

  private[this] var width_shift: Int = _
  private[this] var height_shift: Int = _

  override def generateSpawnQuadrant(map: Array[Array[Int]]): Vector2 = {
    var x = 0
    var y = 0
    do{
      this.width_shift = map.length / 6
      this.height_shift = map(0).length / 6
      x = (map.length / 2) + Wall.WALL_THICKNESS
      if(Random.nextBoolean()) x = x + Random.nextInt(width_shift) else x = x - Random.nextInt(width_shift)
      y = (map(0).length / 2) + Wall.WALL_THICKNESS
      if(Random.nextBoolean()) y = y + Random.nextInt(height_shift) else y = y - Random.nextInt(height_shift)
    }while(!checkAllowedPosition(map, x, y))
    new Vector2(x, y)
  }

}

object ThiefStrategy {
  def apply(): ThiefStrategy = new ThiefStrategy()
}